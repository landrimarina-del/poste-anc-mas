package it.poste.anc.document.application;

import it.poste.anc.document.api.IntakeTypingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

@JdbcTest
@Import(IntakeTypingService.class)
class IntakeTypingServiceTest {

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private IntakeTypingService intakeTypingService;

    @BeforeEach
    void setUpSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS audit_event");
        jdbcTemplate.execute("DROP TABLE IF EXISTS task");
        jdbcTemplate.execute("DROP TABLE IF EXISTS user_group_member");
        jdbcTemplate.execute("DROP TABLE IF EXISTS user_group");
        jdbcTemplate.execute("DROP TABLE IF EXISTS app_user");
        jdbcTemplate.execute("DROP TABLE IF EXISTS practice");

        jdbcTemplate.execute("""
                CREATE TABLE app_user (
                    id BIGINT PRIMARY KEY,
                    username VARCHAR(64) NOT NULL,
                    active TINYINT(1) NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE user_group (
                    id BIGINT PRIMARY KEY,
                    code VARCHAR(64) NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE user_group_member (
                    user_id BIGINT NOT NULL,
                    group_id BIGINT NOT NULL,
                    PRIMARY KEY (user_id, group_id)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE practice (
                    id BIGINT PRIMARY KEY,
                    stato VARCHAR(32) NOT NULL,
                    document_type VARCHAR(16) NULL,
                    version INT NOT NULL DEFAULT 0
                )
                """);

            jdbcTemplate.execute("""
                CREATE TABLE task (
                    id BIGINT PRIMARY KEY,
                    practice_id BIGINT NOT NULL,
                    stato VARCHAR(16) NOT NULL,
                    owner_user_id BIGINT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE audit_event (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    occurred_at TIMESTAMP,
                    actor_username VARCHAR(64),
                    event_type VARCHAR(64),
                    practice_id BIGINT,
                    correlation_id VARCHAR(64),
                    payload_json VARCHAR(2000)
                )
                """);

        jdbcTemplate.update("INSERT INTO practice (id, stato, document_type, version) VALUES (?,?,?,?)",
                100L,
                "IN_LAVORAZIONE",
                null,
                0
        );

            jdbcTemplate.update("INSERT INTO practice (id, stato, document_type, version) VALUES (?,?,?,?)",
                101L,
                "APERTA",
                null,
                0
            );

            jdbcTemplate.update("INSERT INTO practice (id, stato, document_type, version) VALUES (?,?,?,?)",
                102L,
                "CHIUSA_OK",
                null,
                0
            );

            jdbcTemplate.update("INSERT INTO app_user (id, username, active) VALUES (?,?,?)", 1L, "operatore.anc", 1);
            jdbcTemplate.update("INSERT INTO app_user (id, username, active) VALUES (?,?,?)", 2L, "operatore.altro", 1);
            jdbcTemplate.update("INSERT INTO user_group (id, code) VALUES (?,?)", 10L, "GRUPPO_OPERATORE_ANC");
            jdbcTemplate.update("INSERT INTO user_group_member (user_id, group_id) VALUES (?,?)", 1L, 10L);
            jdbcTemplate.update("INSERT INTO user_group_member (user_id, group_id) VALUES (?,?)", 2L, 10L);

            jdbcTemplate.update("INSERT INTO task (id, practice_id, stato, owner_user_id) VALUES (?,?,?,?)",
                1000L,
                100L,
                "IN_CARICO",
                1L
            );

            jdbcTemplate.update("INSERT INTO task (id, practice_id, stato, owner_user_id) VALUES (?,?,?,?)",
                1001L,
                101L,
                "IN_CARICO",
                1L
            );
    }

    @Test
            void ownerCanConfirmTypingValidVerbale() {
        IntakeTypingResponse response = intakeTypingService.confirmTyping(100L, "Verbale", "operatore.anc");

        assertThat(response.practiceId()).isEqualTo(100L);
        assertThat(response.documentType()).isEqualTo("VERBALE");
        assertThat(response.alreadyConfirmed()).isFalse();

        String documentType = jdbcTemplate.queryForObject(
                "SELECT document_type FROM practice WHERE id = 100",
                String.class
        );
        assertThat(documentType).isEqualTo("VERBALE");

        Integer auditCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM audit_event WHERE practice_id = 100 AND event_type = 'DOCUMENT_TYPED'",
                Integer.class
        );
        assertThat(auditCount).isEqualTo(1);
    }

    @Test
    void nonOwnerCannotConfirmTyping() {
        Throwable thrown = catchThrowable(
            () -> intakeTypingService.confirmTyping(100L, "Verbale", "operatore.altro"));
        assertThat(thrown).isInstanceOf(DocumentOperationException.class);
        DocumentOperationException exception = (DocumentOperationException) thrown;

        assertThat(exception.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);
        assertThat(exception.getResultCode()).isEqualTo(4014);
    }

    @Test
    void practiceApertaAllowsTyping() {
        IntakeTypingResponse response = intakeTypingService.confirmTyping(101L, "Verbale", "operatore.anc");

        assertThat(response.practiceId()).isEqualTo(101L);
        assertThat(response.documentType()).isEqualTo("VERBALE");
        assertThat(response.alreadyConfirmed()).isFalse();

        String documentType = jdbcTemplate.queryForObject(
                "SELECT document_type FROM practice WHERE id = 101",
                String.class
        );
        assertThat(documentType).isEqualTo("VERBALE");

        String practiceState = jdbcTemplate.queryForObject(
            "SELECT stato FROM practice WHERE id = 101",
            String.class
        );
        assertThat(practiceState).isEqualTo("IN_LAVORAZIONE");
    }

    @Test
    void practiceNotInAllowedStatesBlocksTyping() {
        assertThatThrownBy(() -> intakeTypingService.confirmTyping(102L, "Verbale", "operatore.anc"))
                .isInstanceOf(DocumentOperationException.class)
                .extracting(ex -> ((DocumentOperationException) ex).getResultCode())
                .isEqualTo(4010);
    }

    @Test
    void confirmTypingIsIrreversibleAndIdempotentOnSameValue() {
        intakeTypingService.confirmTyping(100L, "Verbale", "operatore.anc");

        IntakeTypingResponse idempotent = intakeTypingService.confirmTyping(100L, "Verbale", "operatore.anc");
        assertThat(idempotent.alreadyConfirmed()).isTrue();
        assertThat(idempotent.documentType()).isEqualTo("VERBALE");

        assertThatThrownBy(() -> intakeTypingService.confirmTyping(100L, "Carta", "operatore.anc"))
                .isInstanceOf(DocumentOperationException.class)
                .hasMessageContaining("irreversibile");

        String practiceState = jdbcTemplate.queryForObject("SELECT stato FROM practice WHERE id = 100", String.class);
        assertThat(practiceState).isEqualTo("IN_LAVORAZIONE");
    }
}
