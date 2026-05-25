package it.poste.anc.workflow.engine;

import java.time.Instant;
import java.util.Map;

/**
 * Porta di uscita verso il motore BPM.
 * Astrae le operazioni di processo e task in modo indipendente dall'engine specifico
 * (Flowable, Kogito, ecc.).
 */
public interface BpmEngineAdapter {

    /**
     * Avvia un'istanza di processo.
     *
     * @param processKey chiave del processo definita nel BPMN (es. "anc_pratica")
     * @param businessKey chiave di business (es. numPratica) per lookup univoco
     * @param variables variabili di processo iniziali
     * @return ID dell'istanza di processo creata
     */
    String startProcess(String processKey, String businessKey, Map<String, Object> variables);

    /**
     * Crea un user task standalone associato a un gruppo candidato.
     *
     * @param name nome del task
     * @param description descrizione
     * @param candidateGroup gruppo candidato (es. "GRUPPO_OPERATORE_ANC")
     * @param dueDate scadenza SLA
     * @return ID del task creato
     */
    String createUserTask(String name, String description, String candidateGroup, Instant dueDate);

    /**
     * Assegna un task a un utente specifico (claim).
     *
     * @param taskId ID del task
     * @param username username dell'assegnatario
     */
    void claimTask(String taskId, String username);

    /**
     * Completa un task, passando eventuali variabili di output.
     *
     * @param taskId ID del task
     * @param variables variabili di completamento
     */
    void completeTask(String taskId, Map<String, Object> variables);
}
