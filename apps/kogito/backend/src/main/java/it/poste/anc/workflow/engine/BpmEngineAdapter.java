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
     * Porta il task in stato InProgress (chiusura lato SD, in attesa ACK BPM).
     *
     * @param taskId ID del task nel formato "processInstanceId::workItemId"
     * @param username utente che ha chiuso la pratica
     */
    void startTask(String taskId, String username);

    /**
     * Completa un task, passando eventuali variabili di output.
     *
     * @param taskId ID del task nel formato "processInstanceId::workItemId"
     * @param variables variabili di completamento
     */
    void completeTask(String taskId, Map<String, Object> variables);

    /**
     * Recupera l'ID del WorkItem (UserTask) attivo in un'istanza di processo Kogito.
     * Restituisce una stringa nel formato "processInstanceId::workItemId" da usare
     * come {@code kogito_task_id} nella tabella task.
     *
     * @param processKey chiave del processo BPMN
     * @param processInstanceId UUID dell'istanza di processo Kogito
     * @return stringa composita "processInstanceId::workItemId", oppure {@code null} se non trovato
     */
    String getKogitoWorkItemId(String processKey, String processInstanceId);
}
