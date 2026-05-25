package it.poste.anc.document.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.poste.anc.bpmgw.outbound.BpmOutboundService;
import it.poste.anc.document.api.IntakeCloseResponse;
import it.poste.anc.workflow.engine.BpmEngineAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class IntakePracticeCloseService {

    private static final Logger log = LoggerFactory.getLogger(IntakePracticeCloseService.class);

    private static final String GROUP_CODE = "GRUPPO_OPERATORE_ANC";
    private static final String STATE_IN_LAVORAZIONE = "IN_LAVORAZIONE";
    private static final String STATE_IN_ATTESA_ACK = "IN_ATTESA_CONFERMA_BPM";
    private static final String STATUS_BOZZA = "BOZZA";
    private static final String STATUS_RIAPERTA = "RIAPERTA";
    private static final String STATUS_CONSOLIDATA = "CONSOLIDATA";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final BpmEngineAdapter bpmEngineAdapter;
    private final BpmOutboundService bpmOutboundService;

    public IntakePracticeCloseService(JdbcTemplate jdbcTemplate,
                                      ObjectMapper objectMapper,
                                      BpmEngineAdapter bpmEngineAdapter,
                                      BpmOutboundService bpmOutboundService) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.bpmEngineAdapter = bpmEngineAdapter;
        this.bpmOutboundService = bpmOutboundService;
    }

    @Transactional
    public IntakeCloseResponse closePractice(Long practiceId, String actorUsername) {
        Long userId = findActiveUserId(actorUsername);
        ensureOperatorRole(userId);

        OwnedTask task = findOwnedTask(practiceId, userId);
        PracticeSnapshot practice = readPracticeSnapshot(practiceId);

        if (!STATE_IN_LAVORAZIONE.equals(practice.state())) {
            throw new DocumentOperationException(HttpStatus.CONFLICT, 4035,
                "Chiusura consentita solo per pratiche in stato IN_LAVORAZIONE");
        }

        validateChecklistForClose(practiceId, practice.documentType());
        OutcomeSnapshot outcome = readOutcome(practiceId);

        consolidateChecklist(practiceId, practice.documentType());
        deleteTask(task.taskId());
        completeKogitoTask(task.kogitoTaskId(), actorUsername);

        int updatedPractice = jdbcTemplate.update(
            "UPDATE practice SET stato = ? WHERE id = ? AND stato = ?",
                STATE_IN_ATTESA_ACK,
                practiceId,
            STATE_IN_LAVORAZIONE
        );
        if (updatedPractice == 0) {
            throw new DocumentOperationException(HttpStatus.CONFLICT, 4036,
                    "Transizione pratica non applicabile in modo concorrente");
        }

        String correlationId = "CLOSE_" + practiceId + "_" + System.currentTimeMillis();
        insertStateHistory(practiceId, actorUsername, correlationId, STATE_IN_LAVORAZIONE, STATE_IN_ATTESA_ACK,
                "Pratica chiusa lato SD, in attesa ACK BPM");

        String payloadJson = buildOutboundPayload(practice, outcome, correlationId);

        String statoFinale = sendOutboundSafely(payloadJson, actorUsername, practiceId, correlationId,
                normalizeOutcomeForBpm(outcome));

        return new IntakeCloseResponse(practiceId, statoFinale, correlationId);
    }

    /**
     * Invia l'esito a BPM tramite BpmOutboundService e finalizza la pratica in base alla risposta sincrona.
     * Ritorna lo stato finale della pratica: CHIUSA_OK, CHIUSA_KO oppure IN_ATTESA_CONFERMA_BPM (retry esauriti).
     */
    private String sendOutboundSafely(String payloadJson,
                                      String actorUsername,
                                      Long practiceId,
                                      String correlationId,
                                      String outcomeCode) {
        try {
            String responseBody = bpmOutboundService.sendOutcome(practiceId, outcomeCode, correlationId, payloadJson);

            boolean esito = parseEsitoFromResponse(responseBody);
            String finalState = esito ? "CHIUSA_OK" : "CHIUSA_KO";

            jdbcTemplate.update(
                    "UPDATE practice SET stato = ?, data_chiusura = CURRENT_TIMESTAMP(3) WHERE id = ?",
                    finalState, practiceId
            );
            insertStateHistory(practiceId, actorUsername, correlationId, STATE_IN_ATTESA_ACK, finalState,
                    "Pratica chiusa per esito sincrono BPM: " + finalState);
            jdbcTemplate.update(
                    "INSERT INTO audit_event (occurred_at, actor_username, event_type, practice_id, correlation_id, payload_json) "
                            + "VALUES (CURRENT_TIMESTAMP(3), ?, 'PRACTICE_FINALIZED_BPM', ?, ?, ?)",
                    actorUsername, practiceId, correlationId, responseBody
            );
            return finalState;

        } catch (RuntimeException ex) {
            // Retry esauriti: la pratica rimane IN_ATTESA_CONFERMA_BPM per rielaborazione manuale
            jdbcTemplate.update(
                    "INSERT INTO audit_event (occurred_at, actor_username, event_type, practice_id, correlation_id, payload_json) "
                            + "VALUES (CURRENT_TIMESTAMP(3), ?, 'PRACTICE_CLOSE_REQUESTED', ?, ?, ?)",
                    actorUsername, practiceId, correlationId, payloadJson
            );
            return STATE_IN_ATTESA_ACK;
        }
    }

    private void validateChecklistForClose(Long practiceId, String documentType) {
        String status;
        if ("VERBALE".equals(documentType)) {
            status = jdbcTemplate.query(
                    "SELECT status FROM checklist_verbale WHERE practice_id = ?",
                    rs -> rs.next() ? rs.getString(1) : null,
                    practiceId
            );
        } else if ("CARTA".equals(documentType)) {
            status = jdbcTemplate.query(
                    "SELECT status FROM checklist_carta WHERE practice_id = ?",
                    rs -> rs.next() ? rs.getString(1) : null,
                    practiceId
            );
        } else {
            throw new DocumentOperationException(HttpStatus.CONFLICT, 4024,
                    "Checklist disponibile solo per pratiche tipizzate VERBALE o CARTA");
        }

        if (status == null) {
            throw new DocumentOperationException(HttpStatus.CONFLICT, 4031,
                    "Checklist non disponibile per la chiusura pratica");
        }

        if (!STATUS_BOZZA.equals(status) && !STATUS_RIAPERTA.equals(status) && !STATUS_CONSOLIDATA.equals(status)) {
            throw new DocumentOperationException(HttpStatus.CONFLICT, 4032,
                    "Checklist non consolidabile per la chiusura pratica");
        }
    }

    private void consolidateChecklist(Long practiceId, String documentType) {
        if ("VERBALE".equals(documentType)) {
            jdbcTemplate.update(
                    "UPDATE checklist_verbale SET status = ?, updated_at = CURRENT_TIMESTAMP(3) WHERE practice_id = ?",
                    STATUS_CONSOLIDATA,
                    practiceId
            );
            return;
        }

        jdbcTemplate.update(
                "UPDATE checklist_carta SET status = ?, updated_at = CURRENT_TIMESTAMP(3) WHERE practice_id = ?",
                STATUS_CONSOLIDATA,
                practiceId
        );
    }

    private OutcomeSnapshot readOutcome(Long practiceId) {
        List<OutcomeSnapshot> rows = jdbcTemplate.query(
                "SELECT outcome, ko_codes_json FROM practice_outcome WHERE practice_id = ?",
                (rs, rowNum) -> new OutcomeSnapshot(
                        rs.getString("outcome"),
                        parseJsonArray(rs.getString("ko_codes_json"))
                ),
                practiceId
        );

        if (rows.isEmpty() || rows.get(0).outcome() == null) {
            throw new DocumentOperationException(HttpStatus.CONFLICT, 4033,
                    "Outcome non disponibile: completare checklist prima della chiusura");
        }

        return rows.get(0);
    }

    private PracticeSnapshot readPracticeSnapshot(Long practiceId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id, request_id, stato, document_type FROM practice WHERE id = ?",
                    (rs, rowNum) -> new PracticeSnapshot(
                            rs.getLong("id"),
                            rs.getString("request_id"),
                            rs.getString("stato"),
                            normalizeType(rs.getString("document_type"))
                    ),
                    practiceId
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new DocumentOperationException(HttpStatus.NOT_FOUND, 2004, "Pratica non trovata");
        }
    }

    private OwnedTask findOwnedTask(Long practiceId, Long userId) {
        List<OwnedTask> tasks = jdbcTemplate.query(
                "SELECT id, kogito_task_id FROM task WHERE practice_id = ? AND stato = 'IN_CARICO' AND owner_user_id = ?",
                (rs, rowNum) -> new OwnedTask(rs.getLong("id"), rs.getString("kogito_task_id")),
                practiceId,
                userId
        );

        if (tasks.isEmpty()) {
            throw new DocumentOperationException(HttpStatus.FORBIDDEN, 4030,
                    "Chiusura non autorizzata: task non in carico all'utente corrente");
        }

        return tasks.get(0);
    }

    private void deleteTask(Long taskId) {
        jdbcTemplate.update("DELETE FROM task WHERE id = ?", taskId);
    }

    private void completeKogitoTask(String kogitoTaskId, String actorUsername) {
        if (kogitoTaskId == null || kogitoTaskId.isBlank()) {
            return;
        }
        try {
            bpmEngineAdapter.completeTask(kogitoTaskId, Map.of("closedBy", actorUsername));
        } catch (RuntimeException ex) {
            // Task già chiuso/assente non blocca il close DB locale in POC light.
        }
    }

    private void insertStateHistory(Long practiceId,
                                    String actorUsername,
                                    String correlationId,
                                    String fromState,
                                    String toState,
                                    String note) {
        jdbcTemplate.update(
                "INSERT INTO practice_state_history (practice_id, from_state, to_state, occurred_at, actor_username, correlation_id, note) "
                        + "VALUES (?, ?, ?, CURRENT_TIMESTAMP(3), ?, ?, ?)",
                practiceId,
                fromState,
                toState,
                actorUsername,
                correlationId,
                note
        );
    }

    private String buildOutboundPayload(PracticeSnapshot practice, OutcomeSnapshot outcome, String correlationId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("practiceId", practice.practiceId());
        payload.put("requestId", practice.requestId());
        payload.put("correlationId", correlationId);
        payload.put("outcome", normalizeOutcomeForBpm(outcome));
        payload.put("koCodes", outcome.koCodes());

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Errore serializzazione payload outbound BPM", ex);
        }
    }

    private String normalizeOutcomeForBpm(OutcomeSnapshot outcome) {
        if ("APPROVATA".equals(outcome.outcome())) {
            return "OK";
        }
        if (outcome.koCodes().size() <= 1) {
            return "KO_SINGLE";
        }
        return "KO_MULTIPLE";
    }

    private Long findActiveUserId(String username) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM app_user WHERE username = ? AND active = 1",
                    Long.class,
                    username
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new DocumentOperationException(HttpStatus.UNAUTHORIZED, 1002, "Utente non autenticato");
        }
    }

    private void ensureOperatorRole(Long userId) {
        Integer membership = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user_group_member ugm "
                        + "JOIN user_group ug ON ug.id = ugm.group_id "
                        + "WHERE ugm.user_id = ? AND ug.code = ?",
                Integer.class,
                userId,
                GROUP_CODE
        );
        if (membership == null || membership == 0) {
            throw new DocumentOperationException(HttpStatus.FORBIDDEN, 4013,
                    "Utente non autorizzato: ruolo OPERATORE ANC richiesto");
        }
    }

    private String normalizeType(String documentType) {
        if (documentType == null) {
            throw new DocumentOperationException(HttpStatus.CONFLICT, 4023,
                    "Tipizzazione documento non confermata");
        }
        return documentType.toUpperCase(Locale.ROOT);
    }

    private List<String> parseJsonArray(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    private boolean parseEsitoFromResponse(String responseBody) {
        try {
            Map<?, ?> map = objectMapper.readValue(responseBody, Map.class);
            Object esito = map.get("esito");
            if (esito instanceof Boolean b) return b;
            if (esito instanceof String s) return "true".equalsIgnoreCase(s);
            return true; // default sicuro: se non parsabile, considera OK
        } catch (Exception ex) {
            log.warn("Risposta BPM non parsabile, assumo esito=true: {}", responseBody);
            return true;
        }
    }

    private record OwnedTask(Long taskId, String kogitoTaskId) {
    }

    private record PracticeSnapshot(Long practiceId, String requestId, String state, String documentType) {
    }

    private record OutcomeSnapshot(String outcome, List<String> koCodes) {
    }
}
