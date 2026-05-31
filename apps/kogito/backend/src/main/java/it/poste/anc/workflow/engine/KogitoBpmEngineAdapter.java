package it.poste.anc.workflow.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.kogito.Model;
import org.kie.kogito.auth.IdentityProviderFactory;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.uow.UnitOfWork;
import org.kie.kogito.uow.UnitOfWorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione di BpmEngineAdapter basata su Kogito.
 *
 * <p>Il motore Kogito genera a compile-time le classi di processo dai file BPMN
 * presenti in src/main/resources/processes/. Per ogni processo viene generata
 * un'implementazione di {@code org.kie.kogito.process.Process<T>} registrata
 * come Spring bean con nome corrispondente all'ID del processo BPMN.
 *
 * <p>Per i task standalone (non collegati a un'istanza di processo) — usati nel
 * meccanismo di recovery {@code syncOpenPracticesAsQueuedTasks} — generiamo un
 * UUID direttamente, dato che Kogito non supporta task fuori da un processo.
 */
@Component
public class KogitoBpmEngineAdapter implements BpmEngineAdapter {

    private static final Logger log = LoggerFactory.getLogger(KogitoBpmEngineAdapter.class);

    private final ApplicationContext applicationContext;
    private final UnitOfWorkManager unitOfWorkManager;
    private final RestTemplate restTemplate;
    private final String serviceUrl;

    @Value("${kogito.dataindex.http.url:http://kogito-data-index:8080}")
    private String dataIndexUrl;

    @Autowired
    private IdentityProviderFactory identityProviderFactory;

    public KogitoBpmEngineAdapter(ApplicationContext applicationContext,
                                   UnitOfWorkManager unitOfWorkManager,
                                   @Value("${kogito.service.url:http://localhost:8080}") String serviceUrl) {
        this.applicationContext = applicationContext;
        this.unitOfWorkManager = unitOfWorkManager;
        this.restTemplate = new RestTemplate();
        this.serviceUrl = serviceUrl;
    }

    /**
     * Avvia un'istanza di processo Kogito.
     *
     * <p>Il bean del processo viene cercato dinamicamente nell'ApplicationContext
     * usando il processKey come nome del bean. Kogito genera il bean con lo stesso
     * nome dell'ID BPMN (es. "anc_pratica").
     */
    @Override
    @SuppressWarnings("unchecked")
    public String startProcess(String processKey, String businessKey, Map<String, Object> variables) {
        log.info("Starting Kogito process '{}' businessKey='{}'", processKey, businessKey);

        Process<? extends Model> process = resolveProcess(processKey);

        Model model = process.createModel();
        Map<String, Object> modelVars = new HashMap<>(variables);
        model.fromMap(modelVars);

        UnitOfWork uow = unitOfWorkManager.newUnitOfWork();
        uow.start();
        try {
            ProcessInstance<? extends Model> instance = process.createInstance(businessKey, model);
            instance.start();
            uow.end();
            String instanceId = instance.id();
            log.info("Process '{}' started, instanceId='{}'", processKey, instanceId);
            return instanceId;
        } catch (Exception e) {
            uow.abort();
            throw e;
        }
    }

    /**
     * Crea un "task standalone" restituendo un UUID.
     *
     * <p>Kogito non supporta user task al di fuori di un processo, quindi per il
     * meccanismo di recovery (sync pratiche senza task) usiamo un UUID come
     * identificatore logico. Il ciclo di vita reale è tracciato dalla tabella
     * custom {@code task} nel DB ANC.
     */
    @Override
    public String createUserTask(String name, String description, String candidateGroup, Instant dueDate) {
        String taskId = "manual-" + UUID.randomUUID();
        log.info("Creating standalone task (recovery mode): name='{}' group='{}' taskId='{}'",
                name, candidateGroup, taskId);
        return taskId;
    }

    /**
     * Claim task: usa l'endpoint UserTasksResource (/usertasks/instance/{id}/transition).
     * Prima trova lo userTaskInstanceId cercando la task per externalReferenceId (workItemId),
     * poi esegue la transizione "claim" via UserTask lifecycle (DefaultUserTaskLifeCycle).
     * Per task standalone (prefisso "manual-") è un no-op.
     */
    @Override
    public void claimTask(String taskId, String username) {
        if (taskId == null || taskId.startsWith("manual-")) {
            log.debug("claimTask for standalone task '{}' by '{}' — no BPM action needed", taskId, username);
            return;
        }

        String[] parts = taskId.split("::", 2);
        if (parts.length != 2) {
            log.warn("claimTask: formato taskId '{}' non riconosciuto, skip", taskId);
            return;
        }
        String processInstanceId = parts[0];
        String workItemId = parts[1];

        try {
            String userTaskInstanceId = findUserTaskInstanceId(workItemId, username);
            if (userTaskInstanceId == null) {
                log.warn("claimTask: UserTask non trovata per workItemId '{}' processo '{}'", workItemId, processInstanceId);
                return;
            }

            String transitionUrl = serviceUrl + "/usertasks/instance/" + userTaskInstanceId
                    + "/transition?user=" + username + "&group=GRUPPO_OPERATORE_ANC";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> req = new HttpEntity<>("{\"transitionId\":\"claim\",\"data\":{}}", headers);
            restTemplate.postForEntity(transitionUrl, req, String.class);
            log.info("Task '{}' claimed da '{}' (userTaskId='{}') via UserTaskService", workItemId, username, userTaskInstanceId);
        } catch (Exception e) {
            log.warn("claimTask: errore propagazione claim a Kogito per task '{}' user '{}': {}",
                    workItemId, username, e.getMessage());
        }
    }

    /**
     * Porta il task in stato InProgress (transizione "start") — chiusura lato SD, in attesa ACK BPM.
     */
    @Override
    public void startTask(String taskId, String username) {
        if (taskId == null || taskId.startsWith("manual-")) {
            return;
        }
        String[] parts = taskId.split("::", 2);
        if (parts.length != 2) {
            log.warn("startTask: formato taskId '{}' non riconosciuto, skip", taskId);
            return;
        }
        String processInstanceId = parts[0];
        String workItemId = parts[1];
        try {
            String actorUser = (username != null) ? username : "system";
            String userTaskInstanceId = findUserTaskInstanceId(workItemId, actorUser);
            if (userTaskInstanceId == null) {
                log.warn("startTask: UserTask non trovata per workItemId '{}' processo '{}'", workItemId, processInstanceId);
                return;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> req = new HttpEntity<>("{\"transitionId\":\"start\",\"data\":{}}", headers);
            String transitionUrl = serviceUrl + "/usertasks/instance/" + userTaskInstanceId
                    + "/transition?user=" + actorUser + "&group=GRUPPO_OPERATORE_ANC";
            restTemplate.postForEntity(transitionUrl, req, String.class);
            log.info("Task '{}' avanzata a InProgress (chiusura SD) da '{}'", workItemId, actorUser);
        } catch (Exception e) {
            log.warn("startTask: errore transizione start per task '{}': {}", workItemId, e.getMessage());
        }
    }

    /**
     * Completa il WorkItem (UserTask) Kogito identificato da {@code taskId}.
     * Il formato atteso è "processInstanceId::taskId" prodotto da
     * {@link #getKogitoWorkItemId}.
     * Usa l'endpoint UserTasksResource con transitionId="complete".
     */
    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        if (taskId == null || taskId.startsWith("manual-")) {
            log.debug("completeTask skipped for standalone task '{}'", taskId);
            return;
        }

        String[] parts = taskId.split("::", 2);
        if (parts.length != 2) {
            log.warn("completeTask: formato taskId '{}' non riconosciuto, skip", taskId);
            return;
        }
        String processInstanceId = parts[0];
        String workItemId = parts[1];

        try {
            // Usa l'utente proprietario del task (passato come "closedBy") per la transizione
            String actorUser = (variables != null && variables.containsKey("closedBy"))
                    ? String.valueOf(variables.get("closedBy"))
                    : "system";

            // Cerca il userTaskInstanceId per il workItemId nell'istanza di processo
            String userTaskInstanceId = findUserTaskInstanceId(workItemId, actorUser);
            if (userTaskInstanceId == null) {
                log.warn("completeTask: UserTask non trovata per workItemId '{}' processo '{}'", workItemId, processInstanceId);
                throw new RuntimeException("UserTask non trovata per workItemId: " + workItemId);
            }

            ObjectMapper mapper = new ObjectMapper();
            String dataJson = variables != null && !variables.isEmpty()
                    ? mapper.writeValueAsString(variables)
                    : "{}";
            String body = "{\"transitionId\":\"complete\",\"data\":" + dataJson + "}";

            String transitionUrl = serviceUrl + "/usertasks/instance/" + userTaskInstanceId
                    + "/transition?user=" + actorUser + "&group=GRUPPO_OPERATORE_ANC";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> req = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(transitionUrl, req, String.class);
            log.info("Task '{}' completata (userTaskId='{}') nel processo '{}' via UserTaskService", workItemId, userTaskInstanceId, processInstanceId);
        } catch (Exception e) {
            log.error("Errore completamento task '{}' in processo '{}': {}",
                    workItemId, processInstanceId, e.getMessage());
            throw new RuntimeException("Errore completamento task Kogito: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera il primo WorkItem attivo per il processo indicato interrogando
     * direttamente l'engine Kogito via Java API (non Data Index).
     * Restituisce "processInstanceId::workItemId" oppure null se non trovato.
     */
    @Override
    @SuppressWarnings("unchecked")
    public String getKogitoWorkItemId(String processKey, String processInstanceId) {
        if (processInstanceId == null || processInstanceId.isBlank()) {
            log.debug("getKogitoWorkItemId: processInstanceId null, skip");
            return null;
        }

        try {
            Process<? extends Model> process = resolveProcess(processKey);
            Optional<? extends ProcessInstance<? extends Model>> instanceOpt =
                    process.instances().findById(processInstanceId);

            if (instanceOpt.isEmpty()) {
                log.warn("getKogitoWorkItemId: processo '{}' non trovato nel engine", processInstanceId);
                return null;
            }

            Collection<WorkItem> workItems = instanceOpt.get().workItems();
            if (workItems == null || workItems.isEmpty()) {
                log.warn("getKogitoWorkItemId: nessun work item attivo nel processo '{}'", processInstanceId);
                return null;
            }

            String workItemId = workItems.iterator().next().getId();
            log.debug("getKogitoWorkItemId: processo='{}' workItemId='{}'", processInstanceId, workItemId);
            return processInstanceId + "::" + workItemId;

        } catch (Exception e) {
            log.error("getKogitoWorkItemId: errore recupero work item per processo '{}': {}",
                    processInstanceId, e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Cerca lo userTaskInstanceId (UUID della UserTask Kogito) corrispondente
     * al workItemId dato, interrogando l'endpoint /usertasks/instance.
     * Se {@code username} è null, usa admin come identity per avere piena visibilità.
     */
    private String findUserTaskInstanceId(String workItemId, String username) {
        try {
            String user = (username != null) ? username : "system";
            String listUrl = serviceUrl + "/usertasks/instance?user=" + user + "&group=GRUPPO_OPERATORE_ANC";
            String listJson = restTemplate.getForObject(listUrl, String.class);
            if (listJson == null) return null;
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tasks = mapper.readTree(listJson);
            for (JsonNode task : tasks) {
                if (workItemId.equals(task.path("externalReferenceId").asText(null))) {
                    return task.path("id").asText(null);
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("findUserTaskInstanceId: errore ricerca task per workItemId '{}': {}", workItemId, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    private Process resolveProcess(String processKey) {
        try {
            return (Process<?>) applicationContext.getBean(processKey);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Kogito process bean not found for key '" + processKey + "'. " +
                    "Verifica che il BPMN sia presente in src/main/resources/processes/ " +
                    "e che il processo abbia id='" + processKey + "' nel BPMN.", e);
        }
    }
}
