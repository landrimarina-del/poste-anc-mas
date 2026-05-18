package it.poste.anc.document.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.poste.anc.bpmgw.outbound.BpmOutcomeOutboundGateway;
import it.poste.anc.document.api.IntakeCloseResponse;
import org.flowable.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@JdbcTest
@Import({IntakePracticeCloseService.class, ObjectMapper.class})
class IntakePracticeCloseServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IntakePracticeCloseService closeService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private BpmOutcomeOutboundGateway bpmOutcomeOutboundGateway;

    @BeforeEach
    void setUpSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS bpm_outbound_message");
        jdbcTemplate.execute("DROP TABLE IF EXISTS audit_event");
        jdbcTemplate.execute("DROP TABLE IF EXISTS practice_state_history");
        jdbcTemplate.execute("DROP TABLE IF EXISTS task");
        jdbcTemplate.execute("DROP TABLE IF EXISTS checklist_verbale");
        jdbcTemplate.execute("DROP TABLE IF EXISTS checklist_carta");
        jdbcTemplate.execute("DROP TABLE IF EXISTS practice_outcome");
        jdbcTemplate.execute("DROP TABLE IF EXISTS user_group_member");
        jdbcTemplate.execute("DROP TABLE IF EXISTS user_group");
        jdbcTemplate.execute("DROP TABLE IF EXISTS app_user");
        jdbcTemplate.execute("DROP TABLE IF EXISTS practice");

        jdbcTemplate.execute("""
                CREATE TABLE app_user (
                    id BIGINT PRIMARY KEY,
                    username VARCHAR(64),
                    active BOOLEAN
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
                    request_id VARCHAR(64),
                    stato VARCHAR(64),
                    document_type VARCHAR(16),
                    data_chiusura TIMESTAMP NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE checklist_carta (
                    practice_id BIGINT PRIMARY KEY,
                    card_present BOOLEAN NOT NULL,
                    card_conformity_ok BOOLEAN NULL,
                    internal_notes VARCHAR(2000),
                    status VARCHAR(20) NOT NULL,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE checklist_verbale (
                    practice_id BIGINT PRIMARY KEY,
                    document_present BOOLEAN NOT NULL,
                    readability_ok BOOLEAN,
                    formal_ok BOOLEAN,
                    customer_data_ok BOOLEAN,
                    card_number_match_required BOOLEAN,
                    card_number_match_ok BOOLEAN,
                    ko_reasons_json VARCHAR(2000),
                    internal_notes VARCHAR(2000),
                    status VARCHAR(20) NOT NULL,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE practice_outcome (
                    practice_id BIGINT PRIMARY KEY,
                    outcome VARCHAR(20),
                    ko_codes_json VARCHAR(2000)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE task (
                    id BIGINT PRIMARY KEY,
                    practice_id BIGINT,
                    flowable_task_id VARCHAR(64),
                    stato VARCHAR(20),
                    owner_user_id BIGINT
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
                    note VARCHAR(255)
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
        jdbcTemplate.execute("""
                CREATE TABLE bpm_outbound_message (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    practice_id BIGINT,
                    request_id VARCHAR(64),
                    correlation_id VARCHAR(128),
                    payload_json VARCHAR(4000),
                    status VARCHAR(20),
                    response_body VARCHAR(2000),
                    created_at TIMESTAMP,
                    sent_at TIMESTAMP
                )
                """);

        jdbcTemplate.update("INSERT INTO app_user (id, username, active) VALUES (1, 'operatore.anc', true)");
        jdbcTemplate.update("INSERT INTO user_group (id, code) VALUES (10, 'GRUPPO_OPERATORE_ANC')");
        jdbcTemplate.update("INSERT INTO user_group_member (user_id, group_id) VALUES (1, 10)");

        when(bpmOutcomeOutboundGateway.sendOutcome(anyString())).thenReturn("OK");
    }

    @Test
    void closePracticeMovesStateAndRemovesTaskFromOperatorList() {
        jdbcTemplate.update("INSERT INTO practice (id, request_id, stato, document_type) VALUES (500, 'REQ-500', 'IN_LAVORAZIONE', 'CARTA')");
        jdbcTemplate.update("""
                INSERT INTO checklist_carta (practice_id, card_present, card_conformity_ok, internal_notes, status, created_at, updated_at)
                VALUES (500, true, true, 'ok', 'BOZZA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP())
                """);
        jdbcTemplate.update("INSERT INTO practice_outcome (practice_id, outcome, ko_codes_json) VALUES (500, 'APPROVATA', '[]')");
        jdbcTemplate.update("INSERT INTO task (id, practice_id, flowable_task_id, stato, owner_user_id) VALUES (99, 500, 'FT-99', 'IN_CARICO', 1)");

        IntakeCloseResponse response = closeService.closePractice(500L, "operatore.anc");

        assertThat(response.practiceState()).isEqualTo("IN_ATTESA_CONFERMA_BPM");
        String state = jdbcTemplate.queryForObject("SELECT stato FROM practice WHERE id = 500", String.class);
        assertThat(state).isEqualTo("IN_ATTESA_CONFERMA_BPM");

        Integer taskCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM task WHERE practice_id = 500", Integer.class);
        assertThat(taskCount).isEqualTo(0);
    }
}
