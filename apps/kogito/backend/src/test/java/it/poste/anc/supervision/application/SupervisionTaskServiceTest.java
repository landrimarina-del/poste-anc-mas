package it.poste.anc.supervision.application;

import it.poste.anc.supervision.api.SupervisionTaskReassignResponse;
import it.poste.anc.workflow.application.TaskOperationException;
import it.poste.anc.workflow.engine.BpmEngineAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(SupervisionTaskService.class)
class SupervisionTaskServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SupervisionTaskService supervisionTaskService;

    @MockBean
    private BpmEngineAdapter bpmEngineAdapter;

    @BeforeEach
    void setUpSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS audit_event");
        jdbcTemplate.execute("DROP TABLE IF EXISTS task_assignment_history");
        jdbcTemplate.execute("DROP TABLE IF EXISTS task");
        jdbcTemplate.execute("DROP TABLE IF EXISTS practice");
        jdbcTemplate.execute("DROP TABLE IF EXISTS user_group_member");
        jdbcTemplate.execute("DROP TABLE IF EXISTS user_group");
        jdbcTemplate.execute("DROP TABLE IF EXISTS user_role");
        jdbcTemplate.execute("DROP TABLE IF EXISTS role");
        jdbcTemplate.execute("DROP TABLE IF EXISTS app_user");

        jdbcTemplate.execute("""
                CREATE TABLE app_user (
                    id BIGINT PRIMARY KEY,
                    username VARCHAR(64),
                    active BOOLEAN
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE role (
                    id BIGINT PRIMARY KEY,
                    code VARCHAR(32)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE user_role (
                    user_id BIGINT,
                    role_id BIGINT
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE user_group (
                    id BIGINT PRIMARY KEY,
                    code VARCHAR(64)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE user_group_member (
                    user_id BIGINT,
                    group_id BIGINT
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE practice (
                    id BIGINT PRIMARY KEY,
                    num_pratica VARCHAR(64),
                    stato VARCHAR(64)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE task (
                    id BIGINT PRIMARY KEY,
                    practice_id BIGINT,
                    kogito_task_id VARCHAR(64),
                    stato VARCHAR(20),
                    candidate_group_id BIGINT,
                    owner_user_id BIGINT,
                    created_at TIMESTAMP,
                    accepted_at TIMESTAMP,
                    version INT
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE task_assignment_history (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    task_id BIGINT,
                    assigned_at TIMESTAMP,
                    assigned_by VARCHAR(64),
                    assignment_type VARCHAR(20),
                    target_user_id BIGINT,
                    target_group_id BIGINT,
                    reason VARCHAR(255)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE audit_event (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    occurred_at TIMESTAMP,
                    actor_username VARCHAR(64),
                    event_type VARCHAR(64),
                    practice_id BIGINT,
                    correlation_id VARCHAR(128),
                    payload_json VARCHAR(4000)
                )
                """);

        jdbcTemplate.update("INSERT INTO role (id, code) VALUES (1, 'SUPERVISORE_ANC')");
        jdbcTemplate.update("INSERT INTO app_user (id, username, active) VALUES (10, 'sup.verdi', true)");
        jdbcTemplate.update("INSERT INTO user_role (user_id, role_id) VALUES (10, 1)");

        jdbcTemplate.update("INSERT INTO app_user (id, username, active) VALUES (20, 'op.rossi', true)");
        jdbcTemplate.update("INSERT INTO app_user (id, username, active) VALUES (21, 'op.bianchi', true)");
        jdbcTemplate.update("INSERT INTO user_group (id, code) VALUES (100, 'GRUPPO_OPERATORE_ANC')");
        jdbcTemplate.update("INSERT INTO user_group_member (user_id, group_id) VALUES (20, 100)");
        jdbcTemplate.update("INSERT INTO user_group_member (user_id, group_id) VALUES (21, 100)");

        jdbcTemplate.update("INSERT INTO practice (id, num_pratica, stato) VALUES (500, 'PRAT-500', 'IN_LAVORAZIONE')");
        jdbcTemplate.update("""
                INSERT INTO task (id, practice_id, kogito_task_id, stato, candidate_group_id, owner_user_id, created_at, accepted_at, version)
                VALUES (900, 500, 'FT-900', 'IN_CARICO', 100, 20, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0)
                """);
    }

    @Test
    void reassignToGroupDoesNotChangePracticeStateAndWritesAudit() {
        SupervisionTaskReassignResponse response = supervisionTaskService.reassignToOperatorGroup(
                900L,
                "sup.verdi",
                "Ribilanciamento"
        );

        assertThat(response.assignmentType()).isEqualTo("REASSIGN_GROUP");
        assertThat(response.taskState()).isEqualTo("IN_CODA");

        String practiceState = jdbcTemplate.queryForObject(
                "SELECT stato FROM practice WHERE id = 500",
                String.class
        );
        assertThat(practiceState).isEqualTo("IN_LAVORAZIONE");

        String taskState = jdbcTemplate.queryForObject("SELECT stato FROM task WHERE id = 900", String.class);
        assertThat(taskState).isEqualTo("IN_CODA");

        Integer historyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM task_assignment_history WHERE task_id = 900 AND assignment_type = 'REASSIGN_GROUP'",
                Integer.class
        );
        assertThat(historyCount).isEqualTo(1);

        Integer auditCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM audit_event WHERE practice_id = 500 AND event_type = 'TASK_REASSIGNED'",
                Integer.class
        );
        assertThat(auditCount).isEqualTo(1);
    }

    @Test
    void reassignToUserTargetsSpecificOperatorAndKeepsPracticeState() {
        SupervisionTaskReassignResponse response = supervisionTaskService.reassignToUser(
                900L,
                "sup.verdi",
                "op.bianchi",
                "Bilanciamento coda"
        );

        assertThat(response.assignmentType()).isEqualTo("REASSIGN_USER");
        assertThat(response.ownerUsername()).isEqualTo("op.bianchi");

        String practiceState = jdbcTemplate.queryForObject(
                "SELECT stato FROM practice WHERE id = 500",
                String.class
        );
        assertThat(practiceState).isEqualTo("IN_LAVORAZIONE");

        Long ownerId = jdbcTemplate.queryForObject("SELECT owner_user_id FROM task WHERE id = 900", Long.class);
        assertThat(ownerId).isEqualTo(21L);

        Integer historyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM task_assignment_history WHERE task_id = 900 AND assignment_type = 'REASSIGN_USER'",
                Integer.class
        );
        assertThat(historyCount).isEqualTo(1);
    }

    @Test
    void nonSupervisorCannotReassign() {
        jdbcTemplate.update("INSERT INTO app_user (id, username, active) VALUES (30, 'op.senza.ruolo', true)");

        assertThatThrownBy(() -> supervisionTaskService.reassignToOperatorGroup(900L, "op.senza.ruolo", null))
                .isInstanceOf(TaskOperationException.class)
                .hasMessageContaining("SUPERVISORE_ANC");
    }
}
