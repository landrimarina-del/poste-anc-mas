package it.poste.anc.workflow.application;

import it.poste.anc.workflow.api.TaskAcceptResponse;
import it.poste.anc.workflow.api.TaskDetailResponse;
import it.poste.anc.workflow.api.TaskListItem;
import it.poste.anc.workflow.engine.BpmEngineAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class TaskManagementService {

    private static final Logger log = LoggerFactory.getLogger(TaskManagementService.class);

    private static final String OPERATORE_GROUP_CODE = "GRUPPO_OPERATORE_ANC";
        private static final String TASK_STATE_IN_CODA = "IN_CODA";
        private static final String TASK_STATE_IN_CARICO = "IN_CARICO";

    private final JdbcTemplate jdbcTemplate;
    private final BpmEngineAdapter bpmEngineAdapter;

    public TaskManagementService(JdbcTemplate jdbcTemplate, BpmEngineAdapter bpmEngineAdapter) {
        this.jdbcTemplate = jdbcTemplate;
        this.bpmEngineAdapter = bpmEngineAdapter;
    }

    @Transactional
        public List<TaskListItem> listTasksForCurrentOperator(String username, String practiceNumber, String taskState, boolean assignedToMe, String activityLabel, String ownerUsername, String candidateGroup) {
        Long userId = findUserId(username);
                ensureUserIsOperator(userId);
        Long groupId = findOperatorGroupId();
        syncOpenPracticesAsQueuedTasks(groupId);

                String normalizedPracticeNumber = normalizeFilter(practiceNumber);
                String normalizedTaskState = normalizeFilter(taskState);
                validateTaskStateFilter(normalizedTaskState);
                String normalizedActivityLabel = normalizeFilter(activityLabel);
                String normalizedOwnerUsername = normalizeFilter(ownerUsername);
                String normalizedCandidateGroup = normalizeFilter(candidateGroup);

                StringBuilder sql = new StringBuilder(
                                "SELECT t.id, t.practice_id, p.num_pratica, p.request_id, p.id_work_item, "
                                                + "t.stato AS task_state, p.stato AS practice_state, owner.username AS owner_username, "
                                                + "t.created_at, t.accepted_at, t.sla_due_date, "
                                                + "ug.name AS candidate_group, "
                                                + "CASE WHEN p.document_type IS NULL "
                                                + "  THEN CONCAT('Attivazione Nuova Carta - ', COALESCE(cd.nome,''), ' ', COALESCE(cd.cognome,'')) "
                                                + "  ELSE CONCAT('Attivazione Nuova Carta - ', p.document_type, ' - ', COALESCE(cd.nome,''), ' ', COALESCE(cd.cognome,'')) "
                                                + "END AS activity_label "
                                                + "FROM task t "
                                                + "JOIN practice p ON p.id = t.practice_id "
                                                + "LEFT JOIN client_data cd ON cd.practice_id = p.id "
                                                + "JOIN user_group ug ON ug.id = t.candidate_group_id "
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
                if (assignedToMe) {
                        sql.append("AND t.owner_user_id = ? ");
                        params.add(userId);
                }
                if (normalizedActivityLabel != null) {
                    sql.append("AND (CASE WHEN p.document_type IS NULL "
                             + "THEN CONCAT('Attivazione Nuova Carta - ', COALESCE(cd.nome,''), ' ', COALESCE(cd.cognome,'')) "
                             + "ELSE CONCAT('Attivazione Nuova Carta - ', p.document_type, ' - ', COALESCE(cd.nome,''), ' ', COALESCE(cd.cognome,'')) "
                             + "END) LIKE ? ");
                    params.add("%" + normalizedActivityLabel + "%");
                }
                if (normalizedOwnerUsername != null) {
                    sql.append("AND owner.username LIKE ? ");
                    params.add("%" + normalizedOwnerUsername + "%");
                }
                if (normalizedCandidateGroup != null) {
                    sql.append("AND ug.name LIKE ? ");
                    params.add("%" + normalizedCandidateGroup + "%");
                }
                sql.append("ORDER BY t.created_at DESC, t.id DESC");

        return jdbcTemplate.query(
                                sql.toString(),
                (rs, rowNum) -> {
                        java.time.Instant sla = toInstant(rs.getTimestamp("sla_due_date"));
                        return new TaskListItem(
                                rs.getLong("id"),
                                rs.getLong("practice_id"),
                                rs.getString("num_pratica"),
                                rs.getString("request_id"),
                                rs.getString("id_work_item"),
                                rs.getString("task_state"),
                                rs.getString("practice_state"),
                                rs.getString("owner_username"),
                                toInstant(rs.getTimestamp("created_at")),
                                toInstant(rs.getTimestamp("accepted_at")),
                                sla,
                                computeSlaStatus(sla),
                                rs.getString("activity_label"),
                                rs.getString("candidate_group")
                        );
                },
                params.toArray()
        );
    }

    /**
     * Restituisce il dettaglio di un task con i campi intakeStep e sidebarState.
     * Sprint 12 — GAP-US-03, GAP-US-04.
     */
    @Transactional(readOnly = true)
    public TaskDetailResponse getTaskDetail(Long taskId, String username) {
        Long userId = findUserId(username);
        ensureUserIsOperator(userId);

        List<TaskDetailResponse> results = jdbcTemplate.query(
                "SELECT t.id, t.practice_id, t.stato AS task_state, "
                        + "p.stato AS practice_state, p.document_type, "
                        + "COALESCE(cv.status, cc.status) AS checklist_status, "
                        + "p.num_pratica, p.request_id, p.id_work_item, "
                        + "owner.username AS owner_username, "
                        + "t.created_at, t.accepted_at, t.sla_due_date "
                        + "FROM task t "
                        + "JOIN practice p ON p.id = t.practice_id "
                        + "LEFT JOIN checklist_verbale cv ON cv.practice_id = p.id "
                        + "LEFT JOIN checklist_carta cc ON cc.practice_id = p.id "
                        + "LEFT JOIN app_user owner ON owner.id = t.owner_user_id "
                        + "WHERE t.id = ?",
                (rs, rowNum) -> {
                    String documentType = rs.getString("document_type");
                    String checklistStatus = rs.getString("checklist_status");
                    String intakeStep = computeIntakeStep(documentType, checklistStatus);
                    TaskDetailResponse.SidebarState sidebarState = buildSidebarState(intakeStep, checklistStatus);
                    java.time.Instant slaDueDate = toInstant(rs.getTimestamp("sla_due_date"));
                    String slaStatus = computeSlaStatus(slaDueDate);
                    return new TaskDetailResponse(
                            rs.getLong("id"),
                            rs.getLong("practice_id"),
                            rs.getString("num_pratica"),
                            rs.getString("request_id"),
                            rs.getString("id_work_item"),
                            rs.getString("task_state"),
                            rs.getString("practice_state"),
                            rs.getString("owner_username"),
                            toInstant(rs.getTimestamp("created_at")),
                            toInstant(rs.getTimestamp("accepted_at")),
                            intakeStep,
                            documentType,
                            slaDueDate,
                            slaStatus,
                            sidebarState
                    );
                },
                taskId
        );

        if (results.isEmpty()) {
            throw new TaskOperationException(HttpStatus.NOT_FOUND, 3012, "Task non trovato");
        }
        return results.get(0);
    }

    /**
     * Deriva intakeStep in base a document_type e stato checklist.
     * - document_type IS NULL          → VERIFICA
     * - document_type IS NOT NULL
     *   e checklist non ancora salvata → CHECKLIST
     * - checklist già salvata (status
     *   BOZZA/RIAPERTA/CONSOLIDATA)    → RIEPILOGO
     */
    private String computeIntakeStep(String documentType, String checklistStatus) {
        if (documentType == null) {
            return "VERIFICA";
        }
        if (checklistStatus == null) {
            return "CHECKLIST";
        }
        return "RIEPILOGO";
    }

    /**
     * Costruisce il sidebarState con i 3 step fissi.
     * RIEPILOGO.enabled = true solo se la checklist ha almeno un record salvato.
     */
    private TaskDetailResponse.SidebarState buildSidebarState(String intakeStep, String checklistStatus) {
        boolean riepilogoEnabled = (checklistStatus != null);
        boolean verificaCompleted = "RIEPILOGO".equals(intakeStep);
        String currentStep = "RIEPILOGO".equals(intakeStep) ? "RIEPILOGO" : "VERIFICA_DOCUMENTO";

        List<TaskDetailResponse.StepInfo> steps = List.of(
                new TaskDetailResponse.StepInfo("DATI_PRATICA",       "Dati Pratica",       true,             true),
                new TaskDetailResponse.StepInfo("VERIFICA_DOCUMENTO", "Verifica Documento", true,             verificaCompleted),
                new TaskDetailResponse.StepInfo("RIEPILOGO",          "Riepilogo",          riepilogoEnabled, false)
        );
        return new TaskDetailResponse.SidebarState(currentStep, steps);
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

        claimBpmTask(candidateTask.kogitoTaskId(), username);

        return new TaskAcceptResponse(taskId, candidateTask.practiceId(), "IN_LAVORAZIONE", username);
    }

    private CandidateTask findCandidateTask(Long taskId, Long userId, Long groupId) {
        List<CandidateTask> tasks = jdbcTemplate.query(
                "SELECT t.id, t.practice_id, t.kogito_task_id "
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
                        rs.getString("kogito_task_id")
                ),
                taskId,
                groupId,
                userId
        );

        if (tasks.isEmpty()) {
            throw new TaskOperationException(HttpStatus.NOT_FOUND, 3004, "Task non trovato o non assegnabile all'utente");
        }

        return tasks.get(0);
    }

    private void syncOpenPracticesAsQueuedTasks(Long candidateGroupId) {
        List<OpenPractice> openPracticesWithoutTask = jdbcTemplate.query(
                "SELECT p.id, p.kogito_process_id "
                        + "FROM practice p "
                        + "LEFT JOIN task t ON t.practice_id = p.id AND t.stato IN ('IN_CODA', 'IN_CARICO') "
                        + "WHERE p.stato = 'APERTA' AND t.id IS NULL",
                (rs, rowNum) -> new OpenPractice(rs.getLong("id"), rs.getString("kogito_process_id"))
        );

        for (OpenPractice openPractice : openPracticesWithoutTask) {
            Instant slaDueDate = addWorkingDays(Instant.now(), 5);

            // Usa il WorkItem ID reale del processo Kogito, se disponibile
            String kogitoTaskId = null;
            if (openPractice.kogitoProcessId() != null) {
                kogitoTaskId = bpmEngineAdapter.getKogitoWorkItemId("anc_pratica", openPractice.kogitoProcessId());
            }
            if (kogitoTaskId == null) {
                // Fallback recovery: task non collegato a un processo (pratiche pre-esistenti)
                kogitoTaskId = "manual-" + UUID.randomUUID();
                log.warn("syncOpenPractices: pratica {} senza kogito_process_id, usato task standalone '{}'",
                        openPractice.practiceId(), kogitoTaskId);
            }

            jdbcTemplate.update(
                    "INSERT INTO task (practice_id, kogito_task_id, tipo_pratica, stato, candidate_group_id, created_at, sla_due_date, version) "
                            + "VALUES (?, ?, 'ANC', 'IN_CODA', ?, CURRENT_TIMESTAMP(3), ?, 0)",
                    openPractice.practiceId(),
                    kogitoTaskId,
                    candidateGroupId,
                    Timestamp.from(slaDueDate)
            );
        }
    }

    private String createBpmUserTask(Long practiceId, Instant slaDueDate) {
        return bpmEngineAdapter.createUserTask(
                "Accettazione pratica ANC",
                "Presa in carico pratica " + practiceId,
                OPERATORE_GROUP_CODE,
                slaDueDate
        );
    }

    private static Instant addWorkingDays(Instant from, int workingDays) {
        LocalDate date = from.atZone(ZoneOffset.UTC).toLocalDate();
        int added = 0;
        while (added < workingDays) {
            date = date.plusDays(1);
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                added++;
            }
        }
        return date.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
    }

    private void claimBpmTask(String taskId, String username) {
        if (taskId == null || taskId.isBlank()) {
            return;
        }
        try {
            bpmEngineAdapter.claimTask(taskId, username);
        } catch (RuntimeException ex) {
            throw new TaskOperationException(HttpStatus.INTERNAL_SERVER_ERROR, 3011,
                    "Task BPM non reclamabile: " + ex.getClass().getSimpleName());
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
                if (!TASK_STATE_IN_CODA.equals(taskState) && !TASK_STATE_IN_CARICO.equals(taskState)
                        && !"CHIUSA_SD_OK".equals(taskState) && !"CHIUSA_SD_KO".equals(taskState)
                        && !"CHIUSA_EXT_OK".equals(taskState) && !"CHIUSA_EXT_KO".equals(taskState)) {
                        throw new TaskOperationException(HttpStatus.BAD_REQUEST, 3007,
                                        "Filtro taskState non valido: valori ammessi IN_CODA, IN_CARICO, CHIUSA_SD_OK, CHIUSA_SD_KO, CHIUSA_EXT_OK, CHIUSA_EXT_KO");
                }
        }

    private Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant();
    }

    private String computeSlaStatus(java.time.Instant slaDueDate) {
        if (slaDueDate == null) return null;
        return java.time.Instant.now().isAfter(slaDueDate) ? "SCADUTO" : "IN_TEMPO";
    }

    private record CandidateTask(Long taskId, Long practiceId, String kogitoTaskId) {
    }

    private record OpenPractice(Long practiceId, String kogitoProcessId) {
    }

    @Transactional(readOnly = true)
    public long[] loadOperatorCounters(String username) {
        Long userId = findUserId(username);
        ensureUserIsOperator(userId);
        Long groupId = findOperatorGroupId();

        long activities = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM task t "
                        + "WHERE t.stato IN ('IN_CODA', 'IN_CARICO') "
                        + "AND ("
                        + "(t.owner_user_id = ?) "
                        + "OR (t.owner_user_id IS NULL AND t.candidate_group_id = ? "
                        + "    AND EXISTS (SELECT 1 FROM user_group_member ugm WHERE ugm.user_id = ? AND ugm.group_id = t.candidate_group_id))"
                        + ")",
                Long.class, userId, groupId, userId
        );
        long activePractices = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM practice WHERE stato IN ('APERTA','IN_LAVORAZIONE','CHIUSA_SD_OK','CHIUSA_SD_KO')",
                Long.class
        );
        long closedPractices = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM practice WHERE stato IN ('CHIUSA_EXT_OK','CHIUSA_EXT_KO')",
                Long.class
        );
        return new long[]{activities, activePractices, closedPractices};
    }
}
