package it.poste.anc.bpmgw.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({BpmOutcomeAckService.class, ObjectMapper.class})
class BpmOutcomeAckServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BpmOutcomeAckService service;

    @BeforeEach
    void setUpSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS audit_event");
        jdbcTemplate.execute("DROP TABLE IF EXISTS practice_state_history");
        jdbcTemplate.execute("DROP TABLE IF EXISTS bpm_outcome_ack");
        jdbcTemplate.execute("DROP TABLE IF EXISTS practice");

        jdbcTemplate.execute("""
                CREATE TABLE practice (
                    id BIGINT PRIMARY KEY,
                    request_id VARCHAR(64),
                    stato VARCHAR(64),
                    data_chiusura TIMESTAMP NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE bpm_outcome_ack (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    practice_id BIGINT,
                    request_id VARCHAR(64),
                    correlation_id VARCHAR(128),
                    final_state VARCHAR(20),
                    ack_payload_json VARCHAR(4000),
                    processed_at TIMESTAMP
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
    }

    @Test
    void ackOkClosesPractice() {
        jdbcTemplate.update("INSERT INTO practice (id, request_id, stato) VALUES (100, 'REQ-100', 'IN_ATTESA_CONFERMA_BPM')");

        BpmOutcomeAckResponse response = service.receiveAck(
                new BpmOutcomeAckRequest("CORR-100", "REQ-100", 100L, "OK", java.util.List.of())
        );

        assertThat(response.finalState()).isEqualTo("CHIUSA_OK");
        assertThat(response.closedAt()).isNotNull();

        String state = jdbcTemplate.queryForObject("SELECT stato FROM practice WHERE id = 100", String.class);
        assertThat(state).isEqualTo("CHIUSA_OK");
    }

    @Test
    void ackKoClosesPractice() {
        jdbcTemplate.update("INSERT INTO practice (id, request_id, stato) VALUES (101, 'REQ-101', 'IN_ATTESA_CONFERMA_BPM')");

        BpmOutcomeAckResponse response = service.receiveAck(
                new BpmOutcomeAckRequest("CORR-101", "REQ-101", 101L, "KO", java.util.List.of("CARTA_ASSENTE"))
        );

        assertThat(response.finalState()).isEqualTo("CHIUSA_KO");

        String state = jdbcTemplate.queryForObject("SELECT stato FROM practice WHERE id = 101", String.class);
        assertThat(state).isEqualTo("CHIUSA_KO");
    }

    @Test
    void ackIsIdempotentOnCorrelationId() {
        jdbcTemplate.update("INSERT INTO practice (id, request_id, stato) VALUES (102, 'REQ-102', 'IN_ATTESA_CONFERMA_BPM')");

        service.receiveAck(new BpmOutcomeAckRequest("CORR-102", "REQ-102", 102L, "OK", java.util.List.of()));
        BpmOutcomeAckResponse second = service.receiveAck(
                new BpmOutcomeAckRequest("CORR-102", "REQ-102", 102L, "OK", java.util.List.of())
        );

        assertThat(second.idempotent()).isTrue();

        Integer ackRows = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM bpm_outcome_ack WHERE correlation_id = 'CORR-102'", Integer.class);
        assertThat(ackRows).isEqualTo(1);
    }
}
