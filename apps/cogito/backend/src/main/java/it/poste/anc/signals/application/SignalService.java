package it.poste.anc.signals.application;

import it.poste.anc.signals.api.SignalCreateRequest;
import it.poste.anc.signals.api.SignalCreateResponse;
import it.poste.anc.signals.api.SignalForwardResponse;
import it.poste.anc.signals.api.SignalListItem;
import it.poste.anc.signals.api.SignalReassignRequest;
import it.poste.anc.signals.api.SignalReassignResponse;
import it.poste.anc.signals.api.SignalTakeResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SignalService {

    private static final String ROLE_OPERATORE = "OPERATORE_ANC";
    private static final String ROLE_SUPERVISORE = "SUPERVISORE_ANC";
    private static final String GROUP_OPERATORE = "GRUPPO_OPERATORE_ANC";
    private static final String STATE_IN_CODA = "IN_CODA";
    private static final String STATE_IN_LAVORAZIONE = "IN_LAVORAZIONE";
    private static final String STATE_CHIUSO = "CHIUSO";

    private final JdbcTemplate jdbcTemplate;
    private final SinergiaStubGateway sinergiaStubGateway;

    public SignalService(JdbcTemplate jdbcTemplate, SinergiaStubGateway sinergiaStubGateway) {
        this.jdbcTemplate = jdbcTemplate;
        this.sinergiaStubGateway = sinergiaStubGateway;
    }

    @Transactional
    public SignalCreateResponse createSignal(String username, SignalCreateRequest request) {
        Long userId = findActiveUserId(username);
        ensureUserCanCreateSignals(userId);
        ensurePracticeExists(request.practiceId());
        Long operatorGroupId = findOperatorGroupId();

        Instant now = Instant.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO signal_case (practice_id, created_by_user_id, owner_user_id, candidate_group_id, stato, subject, description, created_at, updated_at, version) "
                            + "VALUES (?, ?, ?, ?, 'IN_CODA', ?, ?, ?, ?, 0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, request.practiceId());
            ps.setLong(2, userId);
            ps.setLong(3, userId);
            ps.setLong(4, operatorGroupId);
            ps.setString(5, normalizeMandatory(request.subject(), "Oggetto segnalazione obbligatorio", 7002));
            ps.setString(6, normalizeMandatory(request.description(), "Descrizione segnalazione obbligatoria", 7003));
            ps.setTimestamp(7, Timestamp.from(now));
            ps.setTimestamp(8, Timestamp.from(now));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new SignalOperationException(HttpStatus.INTERNAL_SERVER_ERROR, 7004,
                    "Impossibile ottenere la chiave della segnalazione");
        }

        Long signalId = key.longValue();

        insertAuditEvent(
                username,
                request.practiceId(),
                "SIGNAL_CREATE_" + signalId,
                "SIGNAL_CREATED",
                "{\"signalId\":" + signalId + ",\"state\":\"IN_CODA\"}"
        );

        return new SignalCreateResponse(signalId, request.practiceId(), STATE_IN_CODA, username, now);
    }

    @Transactional(readOnly = true)
    public List<SignalListItem> listMySignals(String username,
                                              String state,
                                              LocalDate fromDate,
                                              LocalDate toDate) {
        Long userId = findActiveUserId(username);

        String normalizedState = normalizeFilter(state);
        validateStateFilter(normalizedState);

        StringBuilder sql = new StringBuilder(
                "SELECT s.id AS signal_id, s.practice_id, p.num_pratica, s.stato, "
                        + "owner.username AS owner_username, creator.username AS created_by_username, "
                        + "s.subject, s.sinergia_ticket_id, s.created_at, s.updated_at "
                        + "FROM signal_case s "
                        + "JOIN practice p ON p.id = s.practice_id "
                        + "JOIN app_user creator ON creator.id = s.created_by_user_id "
                        + "LEFT JOIN app_user owner ON owner.id = s.owner_user_id "
                        + "WHERE (s.owner_user_id = ? OR s.created_by_user_id = ?) "
        );

        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(userId);

        appendCommonFilters(sql, params, normalizedState, null, fromDate, toDate);

        sql.append("ORDER BY s.updated_at DESC, s.id DESC");
        return jdbcTemplate.query(sql.toString(), signalListItemRowMapper(), params.toArray());
    }

    @Transactional(readOnly = true)
    public List<SignalListItem> listSignalsForSupervisor(String username,
                                                         Long signalId,
                                                         String state,
                                                         String operator,
                                                         LocalDate fromDate,
                                                         LocalDate toDate) {
        Long userId = findActiveUserId(username);
        ensureUserIsSupervisor(userId);

        String normalizedState = normalizeFilter(state);
        String normalizedOperator = normalizeFilter(operator);

        validateStateFilter(normalizedState);

        StringBuilder sql = new StringBuilder(
                "SELECT s.id AS signal_id, s.practice_id, p.num_pratica, s.stato, "
                        + "owner.username AS owner_username, creator.username AS created_by_username, "
                        + "s.subject, s.sinergia_ticket_id, s.created_at, s.updated_at "
                        + "FROM signal_case s "
                        + "JOIN practice p ON p.id = s.practice_id "
                        + "JOIN app_user creator ON creator.id = s.created_by_user_id "
                        + "LEFT JOIN app_user owner ON owner.id = s.owner_user_id "
                        + "WHERE 1 = 1 "
        );

        List<Object> params = new ArrayList<>();
        appendCommonFilters(sql, params, normalizedState, signalId, fromDate, toDate);

        if (normalizedOperator != null) {
            sql.append("AND owner.username = ? ");
            params.add(normalizedOperator);
        }

        sql.append("ORDER BY s.updated_at DESC, s.id DESC");
        return jdbcTemplate.query(sql.toString(), signalListItemRowMapper(), params.toArray());
    }

    @Transactional
    public SignalReassignResponse reassignSignal(Long signalId,
                                                 String supervisorUsername,
                                                 SignalReassignRequest request) {
        Long supervisorId = findActiveUserId(supervisorUsername);
        ensureUserIsSupervisor(supervisorId);

        SignalSnapshot signal = loadSignal(signalId);
        ensureSignalNotClosed(signal.state());

        String targetType = normalizeMandatory(request.targetType(),
                "targetType obbligatorio", 7005).toUpperCase(Locale.ROOT);
        String normalizedReason = normalizeFilter(request.reason());

        ReassignTarget target = resolveReassignTarget(targetType, request.username(), supervisorId, supervisorUsername);

        int updated = jdbcTemplate.update(
                "UPDATE signal_case SET owner_user_id = ?, candidate_group_id = ?, stato = ?, updated_at = CURRENT_TIMESTAMP(3), version = version + 1 "
                        + "WHERE id = ?",
                target.targetUserId(),
                target.targetGroupId(),
                target.targetState(),
                signalId
        );

        if (updated == 0) {
            throw new SignalOperationException(HttpStatus.CONFLICT, 7006,
                    "Segnalazione non aggiornabile in modo concorrente");
        }

        insertAuditEvent(
                supervisorUsername,
                signal.practiceId(),
                "SIGNAL_REASSIGN_" + signalId,
                "SIGNAL_REASSIGNED",
                "{\"signalId\":" + signalId
                        + ",\"targetType\":\"" + escapeJson(targetType) + "\""
                        + ",\"reason\":" + jsonNullable(normalizedReason) + "}"
        );

        return new SignalReassignResponse(
                signalId,
                target.targetState(),
                target.targetUsername(),
                target.targetGroupCode(),
                Instant.now()
        );
    }

    @Transactional
    public SignalTakeResponse takeSignal(Long signalId, String actorUsername) {
        Long userId = findActiveUserId(actorUsername);
        ensureUserCanCreateSignals(userId);

        SignalSnapshot signal = loadSignal(signalId);

        if (!STATE_IN_CODA.equals(signal.state())) {
            throw new SignalOperationException(HttpStatus.CONFLICT, 7019,
                    "Segnalazione non in coda: impossibile prendere in carico");
        }

        Long operatorGroupId = findOperatorGroupId();
        Instant now = Instant.now();

        int updated = jdbcTemplate.update(
                "UPDATE signal_case SET owner_user_id = ?, candidate_group_id = ?, stato = 'IN_LAVORAZIONE', "
                        + "updated_at = CURRENT_TIMESTAMP(3), version = version + 1 WHERE id = ?",
                userId,
                operatorGroupId,
                signalId
        );

        if (updated == 0) {
            throw new SignalOperationException(HttpStatus.CONFLICT, 7020,
                    "Segnalazione non aggiornabile in modo concorrente");
        }

        insertAuditEvent(
                actorUsername,
                signal.practiceId(),
                "SIGNAL_TAKE_" + signalId,
                "SIGNAL_TAKEN",
                "{\"signalId\":" + signalId + ",\"state\":\"IN_LAVORAZIONE\"}"
        );

        return new SignalTakeResponse(signalId, signal.practiceId(), STATE_IN_LAVORAZIONE, actorUsername, now);
    }

    @Transactional
    public SignalForwardResponse forwardToSinergia(Long signalId, String actorUsername) {
        Long actorUserId = findActiveUserId(actorUsername);
        SignalSnapshot signal = loadSignal(signalId);

        boolean isSupervisor = hasRole(actorUserId, ROLE_SUPERVISORE);
        if (!isSupervisor && (signal.ownerUserId() == null || !signal.ownerUserId().equals(actorUserId))) {
            throw new SignalOperationException(HttpStatus.FORBIDDEN, 7007,
                    "Solo assegnatario o supervisore possono inoltrare la segnalazione");
        }
        ensureSignalNotClosed(signal.state());

        SinergiaStubGateway.SinergiaTicket ticket = sinergiaStubGateway.openTicket(
                signal.signalId(),
                signal.practiceId(),
                signal.subject(),
                signal.description(),
                actorUsername
        );

        int updated = jdbcTemplate.update(
                "UPDATE signal_case SET sinergia_ticket_id = ?, sinergia_forwarded_at = ?, stato = 'CHIUSO', closed_at = ?, "
                        + "updated_at = CURRENT_TIMESTAMP(3), version = version + 1 WHERE id = ?",
                ticket.ticketId(),
                Timestamp.from(ticket.openedAt()),
                Timestamp.from(ticket.openedAt()),
                signalId
        );

        if (updated == 0) {
            throw new SignalOperationException(HttpStatus.CONFLICT, 7008,
                    "Segnalazione non aggiornabile in modo concorrente");
        }

        insertAuditEvent(
                actorUsername,
                signal.practiceId(),
                "SIGNAL_FORWARD_" + signalId,
                "SIGNAL_FORWARDED_SINERGIA",
                "{\"signalId\":" + signalId
                        + ",\"ticketId\":\"" + escapeJson(ticket.ticketId()) + "\""
                        + ",\"summary\":\"" + escapeJson(ticket.summary()) + "\"}"
        );

        return new SignalForwardResponse(
                signalId,
                STATE_CHIUSO,
                ticket.ticketId(),
                ticket.openedAt(),
                ticket.openedAt()
        );
    }

    private void appendCommonFilters(StringBuilder sql,
                                     List<Object> params,
                                     String state,
                                     Long signalId,
                                     LocalDate fromDate,
                                     LocalDate toDate) {
        if (signalId != null) {
            sql.append("AND s.id = ? ");
            params.add(signalId);
        }
        if (state != null) {
            sql.append("AND s.stato = ? ");
            params.add(state);
        }
        if (fromDate != null) {
            sql.append("AND DATE(s.created_at) >= ? ");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append("AND DATE(s.created_at) <= ? ");
            params.add(toDate);
        }
    }

    private RowMapper<SignalListItem> signalListItemRowMapper() {
        return (rs, rowNum) -> new SignalListItem(
                rs.getLong("signal_id"),
                rs.getLong("practice_id"),
                rs.getString("num_pratica"),
                rs.getString("stato"),
                rs.getString("owner_username"),
                rs.getString("created_by_username"),
                rs.getString("subject"),
                rs.getString("sinergia_ticket_id"),
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("updated_at"))
        );
    }

    private ReassignTarget resolveReassignTarget(String targetType,
                                                 String username,
                                                 Long supervisorId,
                                                 String supervisorUsername) {
        Long operatorGroupId = findOperatorGroupId();

        if ("GROUP".equals(targetType)) {
            return new ReassignTarget(null, operatorGroupId, STATE_IN_CODA, null, GROUP_OPERATORE);
        }
        if ("ME".equals(targetType)) {
            return new ReassignTarget(supervisorId, operatorGroupId, STATE_IN_LAVORAZIONE,
                    supervisorUsername, supervisorUsername);
        }
        if ("USER".equals(targetType)) {
            String normalizedUsername = normalizeMandatory(username,
                    "username destinatario obbligatorio per targetType=USER", 7009);
            UserSnapshot target = findActiveOperatorUser(normalizedUsername);
            return new ReassignTarget(target.userId(), operatorGroupId, STATE_IN_LAVORAZIONE,
                    target.username(), target.username());
        }

        throw new SignalOperationException(HttpStatus.BAD_REQUEST, 7010,
                "targetType non valido: valori ammessi GROUP, USER, ME");
    }

    private SignalSnapshot loadSignal(Long signalId) {
        List<SignalSnapshot> signals = jdbcTemplate.query(
                "SELECT s.id, s.practice_id, s.stato, s.owner_user_id, s.subject, s.description "
                        + "FROM signal_case s WHERE s.id = ?",
                (rs, rowNum) -> new SignalSnapshot(
                        rs.getLong("id"),
                        rs.getLong("practice_id"),
                        rs.getString("stato"),
                        rs.getObject("owner_user_id", Long.class),
                        rs.getString("subject"),
                        rs.getString("description")
                ),
                signalId
        );

        if (signals.isEmpty()) {
            throw new SignalOperationException(HttpStatus.NOT_FOUND, 7011, "Segnalazione non trovata");
        }

        return signals.get(0);
    }

    private Long findActiveUserId(String username) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM app_user WHERE username = ? AND active = 1",
                    Long.class,
                    username
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new SignalOperationException(HttpStatus.UNAUTHORIZED, 1002, "Utente non autenticato");
        }
    }

    private void ensureUserCanCreateSignals(Long userId) {
        if (!hasRole(userId, ROLE_OPERATORE) && !hasRole(userId, ROLE_SUPERVISORE)) {
            throw new SignalOperationException(HttpStatus.FORBIDDEN, 7012,
                    "Utente non autorizzato: ruolo OPERATORE_ANC o SUPERVISORE_ANC richiesto");
        }
    }

    private void ensureUserIsSupervisor(Long userId) {
        if (!hasRole(userId, ROLE_SUPERVISORE)) {
            throw new SignalOperationException(HttpStatus.FORBIDDEN, 7013,
                    "Utente non autorizzato: ruolo SUPERVISORE_ANC richiesto");
        }
    }

    private boolean hasRole(Long userId, String roleCode) {
        Integer membership = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user_role ur JOIN role r ON r.id = ur.role_id WHERE ur.user_id = ? AND r.code = ?",
                Integer.class,
                userId,
                roleCode
        );
        return membership != null && membership > 0;
    }

    private UserSnapshot findActiveOperatorUser(String username) {
        List<UserSnapshot> users = jdbcTemplate.query(
                "SELECT u.id, u.username FROM app_user u "
                        + "JOIN user_group_member ugm ON ugm.user_id = u.id "
                        + "JOIN user_group ug ON ug.id = ugm.group_id "
                        + "WHERE u.username = ? AND u.active = 1 AND ug.code = ?",
                (rs, rowNum) -> new UserSnapshot(rs.getLong("id"), rs.getString("username")),
                username,
                GROUP_OPERATORE
        );

        if (users.isEmpty()) {
            throw new SignalOperationException(HttpStatus.BAD_REQUEST, 7014,
                    "Utente destinatario non valido o non appartenente al gruppo operatore ANC");
        }

        return users.get(0);
    }

    private void ensurePracticeExists(Long practiceId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM practice WHERE id = ?",
                Integer.class,
                practiceId
        );

        if (count == null || count == 0) {
            throw new SignalOperationException(HttpStatus.BAD_REQUEST, 7015,
                    "Pratica non trovata per la segnalazione");
        }
    }

    private Long findOperatorGroupId() {
        Long groupId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_group WHERE code = ?",
                Long.class,
                GROUP_OPERATORE
        );

        if (groupId == null) {
            throw new SignalOperationException(HttpStatus.INTERNAL_SERVER_ERROR, 7016,
                    "Gruppo operatore ANC non configurato");
        }

        return groupId;
    }

    private void ensureSignalNotClosed(String state) {
        if (STATE_CHIUSO.equals(state)) {
            throw new SignalOperationException(HttpStatus.CONFLICT, 7017,
                    "Segnalazione gia chiusa");
        }
    }

    private void validateStateFilter(String state) {
        if (state == null) {
            return;
        }
        if (!STATE_IN_CODA.equals(state) && !STATE_IN_LAVORAZIONE.equals(state) && !STATE_CHIUSO.equals(state)) {
            throw new SignalOperationException(HttpStatus.BAD_REQUEST, 7018,
                    "Filtro state non valido: valori ammessi IN_CODA, IN_LAVORAZIONE, CHIUSO");
        }
    }

    private void insertAuditEvent(String actorUsername,
                                  Long practiceId,
                                  String correlationId,
                                  String eventType,
                                  String payloadJson) {
        jdbcTemplate.update(
                "INSERT INTO audit_event (occurred_at, actor_username, event_type, practice_id, correlation_id, payload_json) "
                        + "VALUES (CURRENT_TIMESTAMP(3), ?, ?, ?, ?, ?)",
                actorUsername,
                eventType,
                practiceId,
                correlationId,
                payloadJson
        );
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeMandatory(String value, String errorMessage, int resultCode) {
        String normalized = normalizeFilter(value);
        if (normalized == null) {
            throw new SignalOperationException(HttpStatus.BAD_REQUEST, resultCode, errorMessage);
        }
        return normalized;
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String jsonNullable(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + escapeJson(value) + "\"";
    }

    private Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant();
    }

    private record ReassignTarget(Long targetUserId,
                                  Long targetGroupId,
                                  String targetState,
                                  String targetUsername,
                                  String targetGroupCode) {
    }

    private record SignalSnapshot(Long signalId,
                                  Long practiceId,
                                  String state,
                                  Long ownerUserId,
                                  String subject,
                                  String description) {
    }

    private record UserSnapshot(Long userId, String username) {
    }
}
