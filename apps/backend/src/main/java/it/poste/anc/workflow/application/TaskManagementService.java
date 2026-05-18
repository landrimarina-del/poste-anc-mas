package it.poste.anc.workflow.application;

import it.poste.anc.workflow.api.TaskAcceptResponse;
import it.poste.anc.workflow.api.TaskListItem;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class TaskManagementService {

    private static final String OPERATORE_GROUP_CODE = "GRUPPO_OPERATORE_ANC";
        private static final String TASK_STATE_IN_CODA = "IN_CODA";
        private static final String TASK_STATE_IN_CARICO = "IN_CARICO";

    private final JdbcTemplate jdbcTemplate;
    private final TaskService flowableTaskService;

    public TaskManagementService(JdbcTemplate jdbcTemplate, TaskService flowableTaskService) {
        this.jdbcTemplate = jdbcTemplate;
        this.flowableTaskService = flowableTaskService;
    }

    @Transactional
        public List<TaskListItem> listTasksForCurrentOperator(String username, String practiceNumber, String taskState) {
        Long userId = findUserId(username);
                ensureUserIsOperator(userId);
        Long groupId = findOperatorGroupId();
        syncOpenPracticesAsQueuedTasks(groupId);

                String normalizedPracticeNumber = normalizeFilter(practiceNumber);
                String normalizedTaskState = normalizeFilter(taskState);
                validateTaskStateFilter(normalizedTaskState);

                StringBuilder sql = new StringBuilder(
                                "SELECT t.id, t.practice_id, p.num_pratica, p.request_id, p.id_work_item, "
                                                + "t.stato AS task_state, p.stato AS practice_state, owner.username AS owner_username, "
                                                + "t.created_at, t.accepted_at "
                                                + "FROM task t "
                                                + "JOIN practice p ON p.id = t.practice_id "
                                                + "LEFT JOIN app_user owner ON owner.id = t.owner_user_id "
                                                + "WHERE t.stato IN ('IN_CODA', 'IN_CARICO') "
                                                + "AND ("
                                                + "(t.owner_user_id = ?) "
                                                + "OR (t.owner_user_id IS NULL AND t.candidate_group_id = ? AND EXISTS ("
                                                + "  SELECT 1 FROM user_group_member ugm "
                                                + "  WHERE ugm.user_id = ? AND ugm.group_id = t.candidate_group_id"
                                                + "))"
                                                + ") "
                );

                List<Object> params = new java.util.ArrayList<>();
                params.add(userId);
                params.add(groupId);
                params.add(userId);

                if (normalizedPracticeNumber != null) {
                        sql.append("AND p.num_pratica = ? ");
                        params.add(normalizedPracticeNumber);
                }
                if (normalizedTaskState != null) {
                        sql.append("AND t.stato = ? ");
                        params.add(normalizedTaskState);
                }
                sql.append("ORDER BY t.created_at DESC, t.id DESC");

        return jdbcTemplate.query(
                                sql.toString(),
                (rs, rowNum) -> new TaskListItem(
                        rs.getLong("id"),
                        rs.getLong("practice_id"),
                        rs.getString("num_pratica"),
                        rs.getString("request_id"),
                        rs.getString("id_work_item"),
                        rs.getString("task_state"),
                        rs.getString("practice_state"),
                        rs.getString("owner_username"),
                        toInstant(rs.getTimestamp("created_at")),
                        toInstant(rs.getTimestamp("accepted_at"))
                ),
                params.toArray()
        );
    }

    @Transactional
    public TaskAcceptResponse acceptTask(Long taskId, String username) {
        Long userId = findUserId(username);
                ensureUserIsOperator(userId);
        Long groupId = findOperatorGroupId();
        syncOpenPracticesAsQueuedTasks(groupId);

        CandidateTask candidateTask = findCandidateTask(taskId, userId, groupId);

        int updatedTask = jdbcTemplate.update(
                "UPDATE task SET stato = 'IN_CARICO', owner_user_id = ?, accepted_at = CURRENT_TIMESTAMP(3), version = version + 1 "
                        + "WHERE id = ? AND stato = 'IN_CODA' AND owner_user_id IS NULL",
                userId,
                taskId
        );

        if (updatedTask == 0) {
            throw new TaskOperationException(HttpStatus.CONFLICT, 3008, "Task non piu disponibile per la presa in carico");
        }

        int updatedPractice = jdbcTemplate.update(
                "UPDATE practice SET stato = 'IN_LAVORAZIONE' "
                        + "WHERE id = ? AND stato = 'APERTA'",
                candidateTask.practiceId()
        );

        if (updatedPractice == 0) {
            throw new TaskOperationException(HttpStatus.CONFLICT, 3009, "La pratica non e in stato APERTA");
        }

        jdbcTemplate.update(
                "INSERT INTO practice_state_history (practice_id, from_state, to_state, occurred_at, actor_username, correlation_id, note) "
                        + "VALUES (?, 'APERTA', 'IN_LAVORAZIONE', CURRENT_TIMESTAMP(3), ?, ?, ?)",
                candidateTask.practiceId(),
                username,
                "TASK_ACCEPT_" + taskId,
                "Presa in carico operatore ANC"
        );

        jdbcTemplate.update(
                "INSERT INTO audit_event (occurred_at, actor_username, event_type, practice_id, correlation_id, payload_json) "
                        + "VALUES (CURRENT_TIMESTAMP(3), ?, 'TASK_ACCEPTED', ?, ?, JSON_OBJECT('taskId', ?, 'taskState', 'IN_CARICO'))",
                username,
                candidateTask.practiceId(),
                "TASK_ACCEPT_" + taskId,
                String.valueOf(taskId)
        );

        claimFlowableTask(candidateTask.flowableTaskId(), username);

        return new TaskAcceptResponse(taskId, candidateTask.practiceId(), "IN_LAVORAZIONE", username);
    }

    private CandidateTask findCandidateTask(Long taskId, Long userId, Long groupId) {
        List<CandidateTask> tasks = jdbcTemplate.query(
                "SELECT t.id, t.practice_id, t.flowable_task_id "
                        + "FROM task t "
                        + "WHERE t.id = ? AND t.stato = 'IN_CODA' AND t.owner_user_id IS NULL "
                        + "AND t.candidate_group_id = ? "
                        + "AND EXISTS ("
                        + "  SELECT 1 FROM user_group_member ugm "
                        + "  WHERE ugm.user_id = ? AND ugm.group_id = t.candidate_group_id"
                        + ")",
                (rs, rowNum) -> new CandidateTask(
                        rs.getLong("id"),
                        rs.getLong("practice_id"),
                        rs.getString("flowable_task_id")
                ),
                taskId,
                groupId,
                userId
        );

        if (tasks.isEmpty()) {
            throw new TaskOperationException(HttpStatus.NOT_FOUND, 3004, "Task non trovato o non assegnabile all'utente");
        }

        return tasks.getFirst();
    }

    private void syncOpenPracticesAsQueuedTasks(Long candidateGroupId) {
        List<OpenPractice> openPracticesWithoutTask = jdbcTemplate.query(
                "SELECT p.id "
                        + "FROM practice p "
                        + "LEFT JOIN task t ON t.practice_id = p.id AND t.stato IN ('IN_CODA', 'IN_CARICO') "
                        + "WHERE p.stato = 'APERTA' AND t.id IS NULL",
                (rs, rowNum) -> new OpenPractice(rs.getLong("id"))
        );

        for (OpenPractice openPractice : openPracticesWithoutTask) {
            String flowableTaskId = createFlowableAcceptTask(openPractice.practiceId());

            jdbcTemplate.update(
                    "INSERT INTO task (practice_id, flowable_task_id, tipo_pratica, stato, candidate_group_id, created_at, version) "
                            + "VALUES (?, ?, 'ANC', 'IN_CODA', ?, CURRENT_TIMESTAMP(3), 0)",
                    openPractice.practiceId(),
                    flowableTaskId,
                    candidateGroupId
            );
        }
    }

    private String createFlowableAcceptTask(Long practiceId) {
        Task task = flowableTaskService.newTask();
        task.setName("Accettazione pratica ANC");
        task.setCategory("ANC");
        task.setDescription("Presa in carico pratica " + practiceId);
        flowableTaskService.saveTask(task);
        flowableTaskService.addCandidateGroup(task.getId(), OPERATORE_GROUP_CODE);
        return task.getId();
    }

    private void claimFlowableTask(String flowableTaskId, String username) {
        if (flowableTaskId == null || flowableTaskId.isBlank()) {
            return;
        }
        try {
            flowableTaskService.claim(flowableTaskId, username);
        } catch (RuntimeException ex) {
            throw new TaskOperationException(HttpStatus.INTERNAL_SERVER_ERROR, 3011,
                    "Task Flowable non reclamabile: " + ex.getClass().getSimpleName());
        }
    }

    private Long findUserId(String username) {
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM app_user WHERE username = ? AND active = 1",
                Long.class,
                username
        );
        if (userId == null) {
            throw new TaskOperationException(HttpStatus.UNAUTHORIZED, 1002, "Utente non autenticato");
        }
        return userId;
    }

    private Long findOperatorGroupId() {
        Long groupId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_group WHERE code = ?",
                Long.class,
                OPERATORE_GROUP_CODE
        );
        if (groupId == null) {
            throw new TaskOperationException(HttpStatus.INTERNAL_SERVER_ERROR, 3010,
                    "Gruppo operatore ANC non configurato");
        }
        return groupId;
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
                        throw new TaskOperationException(HttpStatus.FORBIDDEN, 3005,
                                        "Utente non autorizzato: ruolo OPERATORE ANC richiesto");
                }
        }

        private String normalizeFilter(String filter) {
                if (filter == null) {
                        return null;
                }
                String trimmed = filter.trim();
                return trimmed.isEmpty() ? null : trimmed;
        }

        private void validateTaskStateFilter(String taskState) {
                if (taskState == null) {
                        return;
                }
                if (!TASK_STATE_IN_CODA.equals(taskState) && !TASK_STATE_IN_CARICO.equals(taskState)) {
                        throw new TaskOperationException(HttpStatus.BAD_REQUEST, 3007,
                                        "Filtro taskState non valido: valori ammessi IN_CODA, IN_CARICO");
                }
        }

    private Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant();
    }

    private record CandidateTask(Long taskId, Long practiceId, String flowableTaskId) {
    }

    private record OpenPractice(Long practiceId) {
    }
}
