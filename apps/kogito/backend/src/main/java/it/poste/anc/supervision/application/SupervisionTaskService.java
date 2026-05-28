package it.poste.anc.supervision.application;

import it.poste.anc.supervision.api.SupervisionTaskListItem;
import it.poste.anc.supervision.api.SupervisionTaskReassignResponse;
import it.poste.anc.workflow.application.TaskOperationException;
import it.poste.anc.workflow.engine.BpmEngineAdapter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupervisionTaskService {

    private static final String OPERATORE_GROUP_CODE = "GRUPPO_OPERATORE_ANC";
    private static final String SUPERVISORE_ROLE_CODE = "SUPERVISORE_ANC";

    private final JdbcTemplate jdbcTemplate;
    private final BpmEngineAdapter bpmEngineAdapter;

    public SupervisionTaskService(JdbcTemplate jdbcTemplate, BpmEngineAdapter bpmEngineAdapter) {
        this.jdbcTemplate = jdbcTemplate;
        this.bpmEngineAdapter = bpmEngineAdapter;
    }

    @Transactional(readOnly = true)
    public List<SupervisionTaskListItem> listSupervisionTasks(String username,
                                                               String practiceNumber,
                                                               LocalDate assignmentDate,
                                                               String owner,
                                                               String assignee) {
        Long supervisorId = findActiveUserId(username);
        ensureUserIsSupervisor(supervisorId);

        String normalizedPracticeNumber = normalizeFilter(practiceNumber);
        String normalizedOwner = normalizeFilter(owner);
        String normalizedAssignee = normalizeFilter(assignee);

        StringBuilder sql = new StringBuilder(
                "SELECT t.id AS task_id, t.practice_id, p.num_pratica, t.stato AS task_state, p.stato AS practice_state, "
                        + "owner.username AS owner_username, "
                        + "COALESCE(last_tah.assigned_at, t.accepted_at, t.created_at) AS assignment_date, "
                        + "COALESCE(last_user.username, owner.username, last_group.code, candidate_group.code) AS assignee, "
                        + "t.accepted_at, "
                        + "CASE WHEN p.document_type IS NULL "
                        + "  THEN CONCAT('Attivazione Nuova Carta - ', COALESCE(cd.nome,''), ' ', COALESCE(cd.cognome,'')) "
                        + "  ELSE CONCAT('Attivazione Nuova Carta - ', p.document_type, ' - ', COALESCE(cd.nome,''), ' ', COALESCE(cd.cognome,'')) "
                        + "END AS activity_label "
                        + "FROM task t "
                        + "JOIN practice p ON p.id = t.practice_id "
                        + "LEFT JOIN client_data cd ON cd.practice_id = p.id "
                        + "LEFT JOIN app_user owner ON owner.id = t.owner_user_id "
                        + "LEFT JOIN user_group candidate_group ON candidate_group.id = t.candidate_group_id "
                        + "LEFT JOIN task_assignment_history last_tah ON last_tah.id = ("
                        + "  SELECT tah2.id FROM task_assignment_history tah2 WHERE tah2.task_id = t.id "
                        + "  ORDER BY tah2.assigned_at DESC, tah2.id DESC LIMIT 1"
                        + ") "
                        + "LEFT JOIN app_user last_user ON last_user.id = last_tah.target_user_id "
                        + "LEFT JOIN user_group last_group ON last_group.id = last_tah.target_group_id "
                        + "WHERE t.stato IN ('IN_CODA', 'IN_CARICO') "
        );

        List<Object> params = new ArrayList<>();

        if (normalizedPracticeNumber != null) {
            sql.append("AND p.num_pratica = ? ");
            params.add(normalizedPracticeNumber);
        }
        if (assignmentDate != null) {
            sql.append("AND DATE(COALESCE(last_tah.assigned_at, t.accepted_at, t.created_at)) = ? ");
            params.add(assignmentDate);
        }
        if (normalizedOwner != null) {
            sql.append("AND owner.username = ? ");
            params.add(normalizedOwner);
        }
        if (normalizedAssignee != null) {
            sql.append("AND COALESCE(last_user.username, owner.username, last_group.code, candidate_group.code) = ? ");
            params.add(normalizedAssignee);
        }

        sql.append("ORDER BY COALESCE(last_tah.assigned_at, t.accepted_at, t.created_at) DESC, t.id DESC");

        return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> new SupervisionTaskListItem(
                        rs.getLong("task_id"),
                        rs.getLong("practice_id"),
                        rs.getString("num_pratica"),
                        rs.getString("task_state"),
                        rs.getString("practice_state"),
                        rs.getString("owner_username"),
                        rs.getString("assignee"),
                        toInstant(rs.getTimestamp("assignment_date")),
                        rs.getString("activity_label"),
                        toInstant(rs.getTimestamp("accepted_at"))
                ),
                params.toArray()
        );
    }

    @Transactional
    public SupervisionTaskReassignResponse reassignToOperatorGroup(Long taskId,
                                                                    String supervisorUsername,
                                                                    String reason) {
        Long supervisorId = findActiveUserId(supervisorUsername);
        ensureUserIsSupervisor(supervisorId);

        TaskSnapshot task = loadTaskSnapshot(taskId);
        Long operatorGroupId = findOperatorGroupId();

        int updated = jdbcTemplate.update(
                "UPDATE task SET owner_user_id = NULL, candidate_group_id = ?, stato = 'IN_CODA', accepted_at = NULL, version = version + 1 "
                        + "WHERE id = ?",
                operatorGroupId,
                taskId
        );
        if (updated == 0) {
            throw new TaskOperationException(HttpStatus.CONFLICT, 6005,
                    "Task non aggiornabile in modo concorrente");
        }

        Instant assignedAt = Instant.now();
        String correlationId = "TASK_REASSIGN_GROUP_" + taskId + "_" + System.currentTimeMillis();

        insertAssignmentHistory(
                taskId,
                supervisorUsername,
                "REASSIGN_GROUP",
                null,
                operatorGroupId,
                reason,
                assignedAt
        );

        insertAuditEvent(
                supervisorUsername,
                task.practiceId(),
                correlationId,
                "TASK_REASSIGNED",
                buildReassignPayload(taskId, "REASSIGN_GROUP", task.ownerUsername(), null, OPERATORE_GROUP_CODE, reason)
        );

        syncKogitoReassignToGroup(task.kogitoTaskId());

        return new SupervisionTaskReassignResponse(
                taskId,
                task.practiceId(),
                "REASSIGN_GROUP",
                "IN_CODA",
                null,
                OPERATORE_GROUP_CODE,
                assignedAt
        );
    }

    @Transactional
    public SupervisionTaskReassignResponse reassignToUser(Long taskId,
                                                           String supervisorUsername,
                                                           String targetUsername,
                                                           String reason) {
        Long supervisorId = findActiveUserId(supervisorUsername);
        ensureUserIsSupervisor(supervisorId);

        String normalizedTargetUsername = normalizeFilter(targetUsername);
        if (normalizedTargetUsername == null) {
            throw new TaskOperationException(HttpStatus.BAD_REQUEST, 6006,
                    "Username destinatario obbligatorio");
        }

        TaskSnapshot task = loadTaskSnapshot(taskId);
        Long operatorGroupId = findOperatorGroupId();
        UserSnapshot targetUser = findActiveOperatorUser(normalizedTargetUsername);

        int updated = jdbcTemplate.update(
                "UPDATE task SET owner_user_id = ?, candidate_group_id = ?, stato = 'IN_CARICO', "
                        + "accepted_at = CURRENT_TIMESTAMP(3), version = version + 1 WHERE id = ?",
                targetUser.userId(),
                operatorGroupId,
                taskId
        );
        if (updated == 0) {
            throw new TaskOperationException(HttpStatus.CONFLICT, 6007,
                    "Task non aggiornabile in modo concorrente");
        }

        Instant assignedAt = Instant.now();
        String correlationId = "TASK_REASSIGN_USER_" + taskId + "_" + System.currentTimeMillis();

        insertAssignmentHistory(
                taskId,
                supervisorUsername,
                "REASSIGN_USER",
                targetUser.userId(),
                null,
                reason,
                assignedAt
        );

        insertAuditEvent(
                supervisorUsername,
                task.practiceId(),
                correlationId,
                "TASK_REASSIGNED",
                buildReassignPayload(taskId, "REASSIGN_USER", task.ownerUsername(), targetUser.username(), null, reason)
        );

        syncKogitoReassignToUser(task.kogitoTaskId(), targetUser.username());

        return new SupervisionTaskReassignResponse(
                taskId,
                task.practiceId(),
                "REASSIGN_USER",
                "IN_CARICO",
                targetUser.username(),
                targetUser.username(),
                assignedAt
        );
    }

    private void insertAssignmentHistory(Long taskId,
                                         String assignedBy,
                                         String assignmentType,
                                         Long targetUserId,
                                         Long targetGroupId,
                                         String reason,
                                         Instant assignedAt) {
        jdbcTemplate.update(
                "INSERT INTO task_assignment_history "
                        + "(task_id, assigned_at, assigned_by, assignment_type, target_user_id, target_group_id, reason) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                taskId,
                Timestamp.from(assignedAt),
                assignedBy,
                assignmentType,
                targetUserId,
                targetGroupId,
                normalizeFilter(reason)
        );
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

    private String buildReassignPayload(Long taskId,
                                        String assignmentType,
                                        String fromOwner,
                                        String toUser,
                                        String toGroup,
                                        String reason) {
        String fromOwnerJson = fromOwner == null ? "null" : "\"" + escapeJson(fromOwner) + "\"";
        String toUserJson = toUser == null ? "null" : "\"" + escapeJson(toUser) + "\"";
        String toGroupJson = toGroup == null ? "null" : "\"" + escapeJson(toGroup) + "\"";
        String reasonJson = reason == null || reason.isBlank() ? "null" : "\"" + escapeJson(reason.trim()) + "\"";

        return "{" +
                "\"taskId\":" + taskId +
                ",\"assignmentType\":\"" + assignmentType + "\"" +
                ",\"fromOwner\":" + fromOwnerJson +
                ",\"toUser\":" + toUserJson +
                ",\"toGroup\":" + toGroupJson +
                ",\"reason\":" + reasonJson +
                "}";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void syncKogitoReassignToGroup(String kogitoTaskId) {
        if (kogitoTaskId == null || kogitoTaskId.isBlank()) {
            return;
        }
        try {
            // Kogito: task standalone (manual-*) non ha lifecycle BPM da resettare
            // Per task process-linked il claim state è gestito dall'applicazione
            bpmEngineAdapter.claimTask(kogitoTaskId, null);
        } catch (RuntimeException ex) {
            throw new TaskOperationException(HttpStatus.INTERNAL_SERVER_ERROR, 6008,
                    "Riassegnazione BPM a gruppo non riuscita: " + ex.getClass().getSimpleName());
        }
    }

    private void syncKogitoReassignToUser(String kogitoTaskId, String targetUsername) {
        if (kogitoTaskId == null || kogitoTaskId.isBlank()) {
            return;
        }
        try {
            bpmEngineAdapter.claimTask(kogitoTaskId, targetUsername);
        } catch (RuntimeException ex) {
            throw new TaskOperationException(HttpStatus.INTERNAL_SERVER_ERROR, 6009,
                    "Riassegnazione BPM a utente non riuscita: " + ex.getClass().getSimpleName());
        }
    }

    private TaskSnapshot loadTaskSnapshot(Long taskId) {
        List<TaskSnapshot> tasks = jdbcTemplate.query(
                "SELECT t.id, t.practice_id, t.kogito_task_id, owner.username AS owner_username "
                        + "FROM task t "
                        + "LEFT JOIN app_user owner ON owner.id = t.owner_user_id "
                        + "WHERE t.id = ? AND t.stato IN ('IN_CODA', 'IN_CARICO')",
                (rs, rowNum) -> new TaskSnapshot(
                        rs.getLong("id"),
                        rs.getLong("practice_id"),
                        rs.getString("kogito_task_id"),
                        rs.getString("owner_username")
                ),
                taskId
        );
        if (tasks.isEmpty()) {
            throw new TaskOperationException(HttpStatus.NOT_FOUND, 6004,
                    "Task non trovato o non riassegnabile");
        }
        return tasks.get(0);
    }

    private Long findActiveUserId(String username) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM app_user WHERE username = ? AND active = 1",
                    Long.class,
                    username
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new TaskOperationException(HttpStatus.UNAUTHORIZED, 1002, "Utente non autenticato");
        }
    }

    private void ensureUserIsSupervisor(Long userId) {
        Integer membership = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) "
                        + "FROM user_role ur "
                        + "JOIN role r ON r.id = ur.role_id "
                        + "WHERE ur.user_id = ? AND r.code = ?",
                Integer.class,
                userId,
                SUPERVISORE_ROLE_CODE
        );
        if (membership == null || membership == 0) {
            throw new TaskOperationException(HttpStatus.FORBIDDEN, 6001,
                    "Utente non autorizzato: ruolo SUPERVISORE_ANC richiesto");
        }
    }

    private Long findOperatorGroupId() {
        Long groupId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_group WHERE code = ?",
                Long.class,
                OPERATORE_GROUP_CODE
        );
        if (groupId == null) {
            throw new TaskOperationException(HttpStatus.INTERNAL_SERVER_ERROR, 6002,
                    "Gruppo operatore ANC non configurato");
        }
        return groupId;
    }

    private UserSnapshot findActiveOperatorUser(String username) {
        List<UserSnapshot> users = jdbcTemplate.query(
                "SELECT u.id, u.username "
                        + "FROM app_user u "
                        + "JOIN user_group_member ugm ON ugm.user_id = u.id "
                        + "JOIN user_group ug ON ug.id = ugm.group_id "
                        + "WHERE u.username = ? AND u.active = 1 AND ug.code = ?",
                (rs, rowNum) -> new UserSnapshot(
                        rs.getLong("id"),
                        rs.getString("username")
                ),
                username,
                OPERATORE_GROUP_CODE
        );
        if (users.isEmpty()) {
            throw new TaskOperationException(HttpStatus.BAD_REQUEST, 6003,
                    "Utente destinatario non valido o non appartenente al gruppo operatore ANC");
        }
        return users.get(0);
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant();
    }

    private record TaskSnapshot(Long taskId, Long practiceId, String kogitoTaskId, String ownerUsername) {
    }

    private record UserSnapshot(Long userId, String username) {
    }

    @Transactional(readOnly = true)
    public List<String> listOperatorUsernames(String supervisorUsername) {
        Long supervisorId = findActiveUserId(supervisorUsername);
        ensureUserIsSupervisor(supervisorId);
        return jdbcTemplate.queryForList(
                "SELECT u.username "
                        + "FROM app_user u "
                        + "JOIN user_group_member ugm ON ugm.user_id = u.id "
                        + "JOIN user_group ug ON ug.id = ugm.group_id "
                        + "WHERE u.active = 1 AND ug.code = ? "
                        + "ORDER BY u.username ASC",
                String.class,
                OPERATORE_GROUP_CODE
        );
    }
}
