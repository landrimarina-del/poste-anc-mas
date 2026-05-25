package it.poste.anc.workflow.engine;

import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
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

    public KogitoBpmEngineAdapter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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

        ProcessInstance<? extends Model> instance = process.createInstance(businessKey, model);
        instance.start();

        String instanceId = instance.id();
        log.info("Process '{}' started, instanceId='{}'", processKey, instanceId);
        return instanceId;
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
     * Claim task: no-op per task standalone (prefisso "manual-").
     * Per task Kogito (UserTask in processo), il claim è gestito dalla UI via Kogito Management Console.
     */
    @Override
    public void claimTask(String taskId, String username) {
        if (taskId != null && taskId.startsWith("manual-")) {
            log.debug("claimTask for standalone task '{}' by '{}' — no BPM action needed", taskId, username);
            return;
        }
        log.info("claimTask '{}' by '{}' — tracked at application level", taskId, username);
    }

    /**
     * Complete task: no-op per task standalone.
     * Per task Kogito prodotti da UserTask BPMN, il completamento avviene tramite
     * il processo stesso quando tutti i campi obbligatori sono valorizzati.
     */
    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        if (taskId != null && taskId.startsWith("manual-")) {
            log.debug("completeTask for standalone task '{}' — no BPM action needed", taskId);
            return;
        }
        log.info("completeTask '{}' with variables={} — tracked at application level", taskId, variables.keySet());
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
