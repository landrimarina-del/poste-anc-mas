package it.poste.anc.supervision.application;

import it.poste.anc.supervision.api.SupervisionDailyOpenedPoint;
import it.poste.anc.supervision.api.SupervisionDailyWorkedPoint;
import it.poste.anc.supervision.api.SupervisionDashboardCountersResponse;
import it.poste.anc.supervision.api.SupervisionPracticeByStatePoint;
import it.poste.anc.workflow.application.TaskOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(SupervisionDashboardService.class)
class SupervisionDashboardServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SupervisionDashboardService supervisionDashboardService;

    @BeforeEach
    void setUpSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS practice_state_history");
        jdbcTemplate.execute("DROP TABLE IF EXISTS task");
        jdbcTemplate.execute("DROP TABLE IF EXISTS practice");
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
                CREATE TABLE practice (
                    id BIGINT PRIMARY KEY,
                    stato VARCHAR(64),
                    data_apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE task (
                    id BIGINT PRIMARY KEY,
                    stato VARCHAR(20)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE practice_state_history (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    practice_id BIGINT,
                    from_state VARCHAR(64),
                    to_state VARCHAR(64),
                    occurred_at TIMESTAMP,
                    actor_username VARCHAR(64),
                    correlation_id VARCHAR(128),
                    note VARCHAR(4000)
                )
                """);

        jdbcTemplate.update("INSERT INTO role (id, code) VALUES (1, 'SUPERVISORE_ANC')");
        jdbcTemplate.update("INSERT INTO app_user (id, username, active) VALUES (10, 'sup.verdi', true)");
        jdbcTemplate.update("INSERT INTO user_role (user_id, role_id) VALUES (10, 1)");
        jdbcTemplate.update("INSERT INTO app_user (id, username, active) VALUES (20, 'op.rossi', true)");

        jdbcTemplate.update("INSERT INTO practice (id, stato, data_apertura) VALUES (100, 'APERTA', TIMESTAMP '2026-05-01 08:00:00')");
        jdbcTemplate.update("INSERT INTO practice (id, stato, data_apertura) VALUES (101, 'IN_LAVORAZIONE', TIMESTAMP '2026-05-02 09:00:00')");
        jdbcTemplate.update("INSERT INTO practice (id, stato, data_apertura) VALUES (102, 'IN_ATTESA_CONFERMA_BPM', TIMESTAMP '2026-05-03 09:00:00')");
        jdbcTemplate.update("INSERT INTO practice (id, stato, data_apertura) VALUES (103, 'CHIUSA_OK', TIMESTAMP '2026-04-10 10:00:00')");
        jdbcTemplate.update("INSERT INTO practice (id, stato, data_apertura) VALUES (104, 'CHIUSA_KO', TIMESTAMP '2026-04-11 11:00:00')");

        jdbcTemplate.update("INSERT INTO task (id, stato) VALUES (1, 'IN_CODA')");
        jdbcTemplate.update("INSERT INTO task (id, stato) VALUES (2, 'IN_CARICO')");

        jdbcTemplate.update("""
                INSERT INTO practice_state_history (practice_id, from_state, to_state, occurred_at, actor_username, correlation_id, note)
                VALUES
                    (100, NULL, 'APERTA', TIMESTAMP '2026-05-01 08:15:00', 'system', 'C1', NULL),
                    (101, NULL, 'APERTA', TIMESTAMP '2026-05-01 09:15:00', 'system', 'C2', NULL),
                    (102, NULL, 'APERTA', TIMESTAMP '2026-05-03 09:15:00', 'system', 'C3', NULL),
                    (103, 'IN_ATTESA_CONFERMA_BPM', 'CHIUSA_OK', TIMESTAMP '2026-05-04 10:00:00', 'system', 'C4', NULL),
                    (104, 'IN_ATTESA_CONFERMA_BPM', 'CHIUSA_KO', TIMESTAMP '2026-05-04 11:00:00', 'system', 'C5', NULL)
                """);
    }

    @Test
    void countersReturnRealtimeVolumes() {
        SupervisionDashboardCountersResponse counters = supervisionDashboardService.loadCounters("sup.verdi");

        assertThat(counters.activities()).isEqualTo(2);
        assertThat(counters.activePractices()).isEqualTo(3);
        assertThat(counters.closedPractices()).isEqualTo(2);
    }

    @Test
    void dailyOpenedReturnsAllMonthDaysWithZeros() {
        List<SupervisionDailyOpenedPoint> points = supervisionDashboardService.loadDailyOpened(
                "sup.verdi",
                YearMonth.of(2026, 5)
        );

        assertThat(points).hasSize(31);
        assertThat(points.get(0).openedPractices()).isEqualTo(2);
        assertThat(points.get(1).openedPractices()).isEqualTo(0);
        assertThat(points.get(2).openedPractices()).isEqualTo(1);
    }

    @Test
    void dailyWorkedSplitsOkAndKoByDay() {
        List<SupervisionDailyWorkedPoint> points = supervisionDashboardService.loadDailyWorked(
                "sup.verdi",
                YearMonth.of(2026, 5)
        );

        assertThat(points).hasSize(31);
        SupervisionDailyWorkedPoint day4 = points.get(3);
        assertThat(day4.okPractices()).isEqualTo(1);
        assertThat(day4.koPractices()).isEqualTo(1);
    }

    @Test
    void byStateReturnsCurrentDistribution() {
        List<SupervisionPracticeByStatePoint> points = supervisionDashboardService.loadPracticesByState("sup.verdi", null);

        assertThat(points).extracting(SupervisionPracticeByStatePoint::state)
                .contains("APERTA", "IN_LAVORAZIONE", "IN_ATTESA_CONFERMA_BPM", "CHIUSA_OK", "CHIUSA_KO");
    }

    @Test
    void byStateFilteredByMonthReturnsSubset() {
        // Maggio 2026: practice 100, 101, 102 (create a maggio)
        // Aprile 2026: practice 103, 104 (create ad aprile)
        List<SupervisionPracticeByStatePoint> points = supervisionDashboardService.loadPracticesByState(
                "sup.verdi",
                YearMonth.of(2026, 5)
        );

        assertThat(points).extracting(SupervisionPracticeByStatePoint::state)
                .contains("APERTA", "IN_LAVORAZIONE", "IN_ATTESA_CONFERMA_BPM")
                .doesNotContain("CHIUSA_OK", "CHIUSA_KO");
    }

    @Test
    void nonSupervisorIsRejected() {
        assertThatThrownBy(() -> supervisionDashboardService.loadCounters("op.rossi"))
                .isInstanceOf(TaskOperationException.class)
                .hasMessageContaining("SUPERVISORE_ANC");
    }
}
