package it.poste.anc.document.application;

import it.poste.anc.document.api.IntakeTypingResponse;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class IntakeTypingService {

    private static final String STATE_IN_LAVORAZIONE = "IN_LAVORAZIONE";
    private static final String TASK_STATE_IN_CARICO = "IN_CARICO";
    private static final String DOCUMENT_TYPE_VERBALE = "VERBALE";
    private static final String DOCUMENT_TYPE_CARTA = "CARTA";
    private static final String OPERATORE_GROUP_CODE = "GRUPPO_OPERATORE_ANC";

    private final JdbcTemplate jdbcTemplate;

    public IntakeTypingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public IntakeTypingResponse confirmTyping(Long practiceId, String documentTypeInput, String actorUsername) {
        PracticeTypingSnapshot snapshot = readPracticeSnapshot(practiceId);
        String normalizedType = normalizeDocumentType(documentTypeInput);

        if (!STATE_IN_LAVORAZIONE.equals(snapshot.practiceState())) {
            throw new DocumentOperationException(
                    HttpStatus.CONFLICT,
                    4010,
                    "Tipizzazione consentita solo per pratiche in stato IN_LAVORAZIONE"
            );
        }

                Long userId = findUserId(actorUsername);
                ensureUserIsOperator(userId);
                ensureTypingTaskOwnership(practiceId, userId);

        if (snapshot.documentType() == null) {
            int updated = jdbcTemplate.update(
                "UPDATE practice SET document_type = ? "
                            + "WHERE id = ? AND stato = 'IN_LAVORAZIONE' AND document_type IS NULL",
                    normalizedType,
                    practiceId
            );

            if (updated == 0) {
                throw new DocumentOperationException(
                        HttpStatus.CONFLICT,
                        4011,
                        "Tipizzazione non confermata per concorrenza o stato non valido"
                );
            }

            jdbcTemplate.update(
                    "INSERT INTO audit_event (occurred_at, actor_username, event_type, practice_id, correlation_id, payload_json) "
                            + "VALUES (CURRENT_TIMESTAMP(3), ?, 'DOCUMENT_TYPED', ?, ?, JSON_OBJECT('documentType', ?))",
                    actorUsername,
                    practiceId,
                    "DOC_TYPE_" + practiceId,
                    normalizedType
            );

            return new IntakeTypingResponse(practiceId, normalizedType, false);
        }

        if (snapshot.documentType().equals(normalizedType)) {
            return new IntakeTypingResponse(practiceId, snapshot.documentType(), true);
        }

        throw new DocumentOperationException(
                HttpStatus.CONFLICT,
                4012,
                "Tipo documento gia confermato in modo irreversibile: " + snapshot.documentType()
        );
    }

    private Long findUserId(String username) {
        try {
            Long userId = jdbcTemplate.queryForObject(
                    "SELECT id FROM app_user WHERE username = ? AND active = 1",
                    Long.class,
                    username
            );
            if (userId == null) {
                throw new DocumentOperationException(HttpStatus.UNAUTHORIZED, 1002, "Utente non autenticato");
            }
            return userId;
        } catch (EmptyResultDataAccessException ex) {
            throw new DocumentOperationException(HttpStatus.UNAUTHORIZED, 1002, "Utente non autenticato");
        }
    }

    private void ensureUserIsOperator(Long userId) {
        Integer membership = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) "
                        + "FROM user_group_member ugm "
                        + "JOIN user_group ug ON ug.id = ugm.group_id "
                        + "WHERE ugm.user_id = ? AND ug.code = ?",
                Integer.class,
                userId,
                OPERATORE_GROUP_CODE
        );
        if (membership == null || membership == 0) {
            throw new DocumentOperationException(HttpStatus.FORBIDDEN, 4013,
                    "Utente non autorizzato: ruolo OPERATORE ANC richiesto");
        }
    }

    private void ensureTypingTaskOwnership(Long practiceId, Long userId) {
        Integer ownedTaskCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM task "
                        + "WHERE practice_id = ? AND stato = ? AND owner_user_id = ?",
                Integer.class,
                practiceId,
                TASK_STATE_IN_CARICO,
                userId
        );
        if (ownedTaskCount == null || ownedTaskCount == 0) {
            throw new DocumentOperationException(HttpStatus.FORBIDDEN, 4014,
                    "Tipizzazione non autorizzata: task non in carico all'utente corrente");
        }
    }

    private PracticeTypingSnapshot readPracticeSnapshot(Long practiceId) {
        List<String> columns = readPracticeColumns();
        String typeColumn = pickRequiredColumn(columns, "document_type");
        String stateColumn = pickRequiredColumn(columns, "stato");

        List<PracticeTypingSnapshot> rows = jdbcTemplate.query(
                "SELECT id, " + "" + stateColumn + " AS practice_state, " + typeColumn + " AS document_type "
                        + "FROM practice WHERE id = ?",
                (rs, rowNum) -> new PracticeTypingSnapshot(
                        rs.getLong("id"),
                        rs.getString("practice_state"),
                        rs.getString("document_type")
                ),
                practiceId
        );

        if (rows.isEmpty()) {
            throw new DocumentOperationException(HttpStatus.NOT_FOUND, 2004, "Pratica non trovata");
        }

        return rows.getFirst();
    }

    private String normalizeDocumentType(String input) {
        if (input == null || input.isBlank()) {
            throw new DocumentOperationException(HttpStatus.BAD_REQUEST, 4008,
                    "Tipo documento obbligatorio: valori ammessi Verbale o Carta");
        }

        String normalized = input.trim().toUpperCase(Locale.ROOT);
        if ("VERBALE".equals(normalized)) {
            return DOCUMENT_TYPE_VERBALE;
        }
        if ("CARTA".equals(normalized)) {
            return DOCUMENT_TYPE_CARTA;
        }

        throw new DocumentOperationException(HttpStatus.BAD_REQUEST, 4008,
                "Tipo documento non valido: valori ammessi Verbale o Carta");
    }

    private List<String> readPracticeColumns() {
        return jdbcTemplate.query("SELECT * FROM practice WHERE 1 = 0", rs -> {
            ResultSetMetaData md = rs.getMetaData();
            List<String> dbColumns = new ArrayList<>();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                dbColumns.add(md.getColumnName(i).toLowerCase(Locale.ROOT));
            }
            return dbColumns;
        });
    }

    private String pickRequiredColumn(List<String> availableColumns, String... candidates) {
        for (String candidate : candidates) {
            if (availableColumns.contains(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
        throw new IllegalStateException("Colonna practice obbligatoria non trovata");
    }

    private record PracticeTypingSnapshot(Long practiceId, String practiceState, String documentType) {
    }
}
