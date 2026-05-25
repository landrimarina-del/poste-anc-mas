package it.poste.anc.workflow.engine;

import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.uow.UnitOfWork;
import org.kie.kogito.uow.UnitOfWorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final String dataIndexUrl;

    public KogitoBpmEngineAdapter(ApplicationContext applicationContext,
                                   Application kogitoApplication,
                                   @Value("${kogito.data-index-url:http://kogito-data-index:8080}") String dataIndexUrl) {
        this.applicationContext = applicationContext;
        this.unitOfWorkManager = kogitoApplication.unitOfWorkManager();
        this.restTemplate = new RestTemplate();
        this.dataIndexUrl = dataIndexUrl;
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
     * Claim task: propaga il claim a Kogito via REST API (phase=claim).
     * Questo aggiorna actualOwner nel Data Index e porta il task da Ready a Reserved.
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
            String url = String.format(
                    "http://localhost:8080/anc_pratica/%s/Lavorazione_Pratica/%s?phase=claim&user=%s",
                    processInstanceId, workItemId, username);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of(), headers);

            restTemplate.postForObject(url, request, Map.class);
            log.info("Task '{}' claimed da '{}' nel processo '{}'", workItemId, username, processInstanceId);

        } catch (Exception e) {
            // Il claim non è bloccante: logga warning ma non fallisce l'operazione applicativa
            log.warn("claimTask: errore propagazione claim a Kogito per task '{}' user '{}': {}",
                    workItemId, username, e.getMessage());
        }
    }

    /**
     * Completa il WorkItem (UserTask) Kogito identificato da {@code taskId}.
     * Il formato atteso è "processInstanceId::taskId" prodotto da
     * {@link #getKogitoWorkItemId}.
     * Usa l'API REST locale di Kogito per completare il task, bypassando
     * i check di SecurityPolicy Java (che variano per versione Kogito).
     */
    @Override
    @SuppressWarnings("unchecked")
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

        // Chiama l'API REST Kogito locale per completare il task.
        // Il BPMN usa ActorId="group:GRUPPO_OPERATORE_ANC", quindi il SecurityPolicy
        // deve usare user="group:GRUPPO_OPERATORE_ANC" per matchare l'ActorId del workItem.
        // POST /anc_pratica/{processId}/Lavorazione_Pratica/{taskId}?phase=complete&user=group:GRUPPO_OPERATORE_ANC
        try {
            String url = String.format(
                    "http://localhost:8080/anc_pratica/%s/Lavorazione_Pratica/%s?phase=complete&user=group:GRUPPO_OPERATORE_ANC",
                    processInstanceId, workItemId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(variables, headers);

            restTemplate.postForObject(url, request, Map.class);
            log.info("WorkItem '{}' completato nell'istanza processo '{}'", workItemId, processInstanceId);

        } catch (Exception e) {
            log.error("Errore completamento WorkItem '{}' in processo '{}': {}",
                    workItemId, processInstanceId, e.getMessage());
            throw new RuntimeException("Errore completamento task Kogito: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera il primo UserTask attivo per il processo indicato interrogando il
     * Data Index di Kogito via GraphQL.
     * Restituisce "processInstanceId::taskId" oppure null se non trovato.
     */
    @Override
    public String getKogitoWorkItemId(String processKey, String processInstanceId) {
        if (processInstanceId == null || processInstanceId.isBlank()) {
            log.debug("getKogitoWorkItemId: processInstanceId null, skip");
            return null;
        }

        try {
            String graphqlQuery = String.format(
                    "{\"query\": \"{ UserTaskInstances(where: { processInstanceId: { equal: \\\"%s\\\" } }) { id, state } }\"}",
                    processInstanceId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(graphqlQuery, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.postForObject(
                    dataIndexUrl + "/graphql", request, Map.class);

            if (body == null) {
                log.warn("getKogitoWorkItemId: risposta null dal Data Index per processo '{}'", processInstanceId);
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) data.get("UserTaskInstances");

            if (tasks == null || tasks.isEmpty()) {
                log.warn("getKogitoWorkItemId: nessun UserTask attivo in processo '{}' (Data Index)", processInstanceId);
                return null;
            }

            // Prendi il primo task in stato Ready
            String taskId = tasks.stream()
                    .filter(t -> "Ready".equals(t.get("state")) || "Reserved".equals(t.get("state")))
                    .map(t -> (String) t.get("id"))
                    .findFirst()
                    .orElse((String) tasks.get(0).get("id"));

            log.debug("getKogitoWorkItemId: processo='{}' taskId='{}'", processInstanceId, taskId);
            return processInstanceId + "::" + taskId;

        } catch (Exception e) {
            log.error("getKogitoWorkItemId: errore interrogazione Data Index per processo '{}': {}",
                    processInstanceId, e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

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
