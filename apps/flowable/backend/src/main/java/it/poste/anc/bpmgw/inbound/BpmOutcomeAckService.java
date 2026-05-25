package it.poste.anc.bpmgw.inbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class BpmOutcomeAckService {

    private static final String STATE_WAITING_ACK = "IN_ATTESA_CONFERMA_BPM";
    private static final String STATE_CLOSED_OK = "CHIUSA_OK";
    private static final String STATE_CLOSED_KO = "CHIUSA_KO";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public BpmOutcomeAckService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BpmOutcomeAckResponse receiveAck(BpmOutcomeAckRequest request) {
        validateRequest(request);

        AckRow existing = findExistingAck(request.correlationId(), request.requestId());
        if (existing != null) {
            return new BpmOutcomeAckResponse(
                    existing.practiceId(),
                    existing.requestId(),
                    existing.finalState(),
                    toInstant(existing.closedAt()),
                    true
            );
        }

        PracticeRef practiceRef = resolvePractice(request);
        String finalState = normalizeOutcome(request.outcome());

        int updated = jdbcTemplate.update(
                "UPDATE practice SET stato = ?, data_chiusura = CURRENT_TIMESTAMP(3) WHERE id = ? AND stato = ?",
                finalState,
                practiceRef.practiceId(),
                STATE_WAITING_ACK
        );

        if (updated == 0) {
            String currentState = jdbcTemplate.queryForObject(
                    "SELECT stato FROM practice WHERE id = ?",
                    String.class,
                    practiceRef.practiceId()
            );
            if (STATE_CLOSED_OK.equals(currentState) || STATE_CLOSED_KO.equals(currentState)) {
                Timestamp closedAt = jdbcTemplate.queryForObject(
                        "SELECT data_chiusura FROM practice WHERE id = ?",
                        Timestamp.class,
                        practiceRef.practiceId()
                );
                return new BpmOutcomeAckResponse(practiceRef.practiceId(), practiceRef.requestId(), currentState,
                        toInstant(closedAt), true);
            }
            throw new BpmAckOperationException(HttpStatus.CONFLICT, 5104,
                    "ACK non applicabile: pratica non in stato IN_ATTESA_CONFERMA_BPM");
        }

        String payloadJson = toJson(request);
        jdbcTemplate.update(
                "INSERT INTO bpm_outcome_ack (practice_id, request_id, correlation_id, final_state, ack_payload_json, processed_at) "
                        + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP(3))",
                practiceRef.practiceId(),
                practiceRef.requestId(),
                request.correlationId(),
                finalState,
                payloadJson
        );

        jdbcTemplate.update(
                "INSERT INTO practice_state_history (practice_id, from_state, to_state, occurred_at, actor_username, correlation_id, note) "
                        + "VALUES (?, ?, ?, CURRENT_TIMESTAMP(3), ?, ?, ?)",
                practiceRef.practiceId(),
                STATE_WAITING_ACK,
                finalState,
                "bpm-stub",
                request.correlationId(),
                "ACK esito pratica da BPM"
        );

        jdbcTemplate.update(
                "INSERT INTO audit_event (occurred_at, actor_username, event_type, practice_id, correlation_id, payload_json) "
                        + "VALUES (CURRENT_TIMESTAMP(3), 'bpm-stub', 'BPM_OUTCOME_ACK_RECEIVED', ?, ?, ?)",
                practiceRef.practiceId(),
                request.correlationId(),
                payloadJson
        );

        Timestamp closedAt = jdbcTemplate.queryForObject(
                "SELECT data_chiusura FROM practice WHERE id = ?",
                Timestamp.class,
                practiceRef.practiceId()
        );

        return new BpmOutcomeAckResponse(
                practiceRef.practiceId(),
                practiceRef.requestId(),
                finalState,
                toInstant(closedAt),
                false
        );
    }

    private void validateRequest(BpmOutcomeAckRequest request) {
        if (request == null) {
            throw new BpmAckOperationException(HttpStatus.BAD_REQUEST, 5100, "Payload ACK obbligatorio");
        }
        boolean hasPracticeId = request.practiceId() != null;
        boolean hasRequestId = request.requestId() != null && !request.requestId().isBlank();
        if (!hasPracticeId && !hasRequestId) {
            throw new BpmAckOperationException(HttpStatus.BAD_REQUEST, 5101,
                    "ACK non valido: practiceId o requestId obbligatorio");
        }
        if (request.correlationId() == null || request.correlationId().isBlank()) {
            throw new BpmAckOperationException(HttpStatus.BAD_REQUEST, 5102,
                    "ACK non valido: correlationId obbligatorio");
        }
        if (request.outcome() == null || request.outcome().isBlank()) {
            throw new BpmAckOperationException(HttpStatus.BAD_REQUEST, 5103,
                    "ACK non valido: outcome obbligatorio");
        }
    }

    private AckRow findExistingAck(String correlationId, String requestId) {
        List<AckRow> rows = jdbcTemplate.query(
                "SELECT practice_id, request_id, final_state, processed_at "
                        + "FROM bpm_outcome_ack "
                        + "WHERE correlation_id = ? OR (? IS NOT NULL AND request_id = ?) "
                        + "ORDER BY id DESC",
                (rs, rowNum) -> new AckRow(
                        rs.getLong("practice_id"),
                        rs.getString("request_id"),
                        rs.getString("final_state"),
                        rs.getTimestamp("processed_at")
                ),
                correlationId,
                requestId,
                requestId
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private PracticeRef resolvePractice(BpmOutcomeAckRequest request) {
        if (request.practiceId() != null) {
            try {
                String requestId = jdbcTemplate.queryForObject(
                        "SELECT request_id FROM practice WHERE id = ?",
                        String.class,
                        request.practiceId()
                );
                return new PracticeRef(request.practiceId(), requestId);
            } catch (EmptyResultDataAccessException ex) {
                throw new BpmAckOperationException(HttpStatus.NOT_FOUND, 2004, "Pratica non trovata");
            }
        }

        List<PracticeRef> rows = jdbcTemplate.query(
                "SELECT id, request_id FROM practice WHERE request_id = ?",
                (rs, rowNum) -> new PracticeRef(rs.getLong("id"), rs.getString("request_id")),
                request.requestId()
        );
        if (rows.isEmpty()) {
            throw new BpmAckOperationException(HttpStatus.NOT_FOUND, 2004, "Pratica non trovata");
        }
        return rows.getFirst();
    }

    private String normalizeOutcome(String input) {
        String normalized = input.trim().toUpperCase(Locale.ROOT);
        if ("OK".equals(normalized) || STATE_CLOSED_OK.equals(normalized)) {
            return STATE_CLOSED_OK;
        }
        if ("KO".equals(normalized) || STATE_CLOSED_KO.equals(normalized)) {
            return STATE_CLOSED_KO;
        }
        throw new BpmAckOperationException(HttpStatus.BAD_REQUEST, 5105,
                "Outcome ACK non valido: valori ammessi OK/KO");
    }

    private String toJson(BpmOutcomeAckRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Errore serializzazione ACK", ex);
        }
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private record PracticeRef(Long practiceId, String requestId) {
    }

    private record AckRow(Long practiceId, String requestId, String finalState, Timestamp closedAt) {
    }
}
