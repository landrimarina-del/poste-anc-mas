package it.poste.anc.document.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.poste.anc.document.api.IntakeChecklistRequest;
import it.poste.anc.document.api.IntakeChecklistResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({IntakeChecklistService.class, ObjectMapper.class})
class IntakeChecklistServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IntakeChecklistService intakeChecklistService;

    @BeforeEach
    void setUpSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS audit_event");
        jdbcTemplate.execute("DROP TABLE IF EXISTS practice_outcome");
        jdbcTemplate.execute("DROP TABLE IF EXISTS checklist_verbale");
        jdbcTemplate.execute("DROP TABLE IF EXISTS practice");

        jdbcTemplate.execute("""
                CREATE TABLE practice (
                    id BIGINT PRIMARY KEY,
                    document_type VARCHAR(16) NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE checklist_verbale (
                    practice_id BIGINT PRIMARY KEY,
                    document_present BOOLEAN NOT NULL,
                    readability_ok BOOLEAN NULL,
                    formal_ok BOOLEAN NULL,
                    customer_data_ok BOOLEAN NULL,
                    card_number_match_required BOOLEAN NOT NULL,
                    card_number_match_ok BOOLEAN NULL,
                    ko_reasons_json VARCHAR(2000) NULL,
                    codice_causale_id BIGINT NULL,
                    internal_notes VARCHAR(2000) NULL,
                    status VARCHAR(20) NOT NULL,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE practice_outcome (
                    practice_id BIGINT PRIMARY KEY,
                    outcome VARCHAR(20) NOT NULL,
                    ko_codes_json VARCHAR(2000) NULL,
                    computed_at TIMESTAMP,
                    computed_by VARCHAR(100)
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

        jdbcTemplate.update("INSERT INTO practice (id, document_type) VALUES (?, ?)", 500L, "VERBALE");
    }

    @Test
    void documentPresentNoProducesRespinta() {
        IntakeChecklistRequest request = new IntakeChecklistRequest(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                "Documento assente",
                null
        );

        IntakeChecklistResponse response = intakeChecklistService.saveDraft(500L, request, "operatore.anc");

        assertThat(response.outcome()).isEqualTo("RESPINTA");
        assertThat(response.status()).isEqualTo("BOZZA");

        String persistedOutcome = jdbcTemplate.queryForObject(
                "SELECT outcome FROM practice_outcome WHERE practice_id = 500",
                String.class
        );
        assertThat(persistedOutcome).isEqualTo("RESPINTA");
    }

    @Test
    void formalKoWithoutReasonsReturnsBadRequest() {
        IntakeChecklistRequest request = new IntakeChecklistRequest(
                true,
                true,
                false,
                true,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                "KO formale",
                null
        );

        assertThatThrownBy(() -> intakeChecklistService.saveDraft(500L, request, "operatore.anc"))
                .isInstanceOf(DocumentOperationException.class)
                .extracting(ex -> ((DocumentOperationException) ex).getHttpStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void allChecksYesProducesApprovata() {
        IntakeChecklistRequest request = new IntakeChecklistRequest(
                true,
                true,
                true,
                true,
                true,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                "Verifiche complete",
                null
        );

        IntakeChecklistResponse response = intakeChecklistService.saveDraft(500L, request, "operatore.anc");

        assertThat(response.outcome()).isEqualTo("APPROVATA");
        assertThat(response.koCodes()).isEmpty();

        Integer auditCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM audit_event WHERE practice_id = 500 AND event_type = 'CHECKLIST_SAVED'",
                Integer.class
        );
        assertThat(auditCount).isEqualTo(1);
    }

        @Test
        void cartaPresentNoProducesRespinta() {
                jdbcTemplate.update("INSERT INTO practice (id, document_type) VALUES (?, ?)", 501L, "CARTA");
                jdbcTemplate.execute("""
                                CREATE TABLE IF NOT EXISTS checklist_carta (
                                        practice_id BIGINT PRIMARY KEY,
                                        card_present BOOLEAN NOT NULL,
                                        card_conformity_ok BOOLEAN NULL,
                                        codice_causale_id BIGINT NULL,
                                        internal_notes VARCHAR(2000) NULL,
                                        status VARCHAR(20) NOT NULL,
                                        created_at TIMESTAMP,
                                        updated_at TIMESTAMP
                                )
                                """);

                IntakeChecklistRequest request = new IntakeChecklistRequest(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                false,
                                null,
                                List.of(),
                                "Carta non presente",
                                null
                );

                IntakeChecklistResponse response = intakeChecklistService.saveDraft(501L, request, "operatore.anc");

                assertThat(response.documentType()).isEqualTo("CARTA");
                assertThat(response.outcome()).isEqualTo("RESPINTA");
                assertThat(response.koCodes()).containsExactly("CARTA_ASSENTE");
        }
}
