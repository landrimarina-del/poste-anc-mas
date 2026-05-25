package it.poste.anc.bpmgw.outbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Servizio outbound BPM con retry e tracciamento su bpm_outbound_message.
 * Wrappa HttpBpmOutcomeOutboundGateway aggiungendo persistenza, retry e audit.
 * Sprint 11 — GAP-US-02.
 */
@Service
public class BpmOutboundService {

    private static final Logger log = LoggerFactory.getLogger(BpmOutboundService.class);

    private final BpmOutcomeOutboundGateway gateway;
    private final JdbcTemplate jdbcTemplate;
    private final RetryTemplate retryTemplate;
    private final int maxRetry;

    public BpmOutboundService(
            BpmOutcomeOutboundGateway gateway,
            JdbcTemplate jdbcTemplate,
            @Value("${bpm.max-retry:3}") int maxRetry,
            @Value("${bpm.retry-interval-ms:2000}") long retryIntervalMs
    ) {
        this.gateway = gateway;
        this.jdbcTemplate = jdbcTemplate;
        this.maxRetry = maxRetry;
        this.retryTemplate = buildRetryTemplate(maxRetry, retryIntervalMs);
    }

    /**
     * Invia l'esito della pratica a BPM con retry automatico.
     * Traccia ogni tentativo su bpm_outbound_message.
     *
     * @param practiceId    ID della pratica
     * @param outcome       APPROVATA | RESPINTA
     * @param correlationId ID correlazione univoco
     * @param payloadJson   payload JSON da inviare
     * @return risposta raw del server BPM
     */
    public String sendOutcome(Long practiceId, String outcome, String correlationId, String payloadJson) {
        // Inserisci riga di tracciamento con stato_invio=0 (in attesa di invio)
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO bpm_outbound_message " +
                    "(practice_id, payload_json, correlation_id, status, retry_count, max_retry, stato_invio, created_at) " +
                    "VALUES (?, ?, ?, 'PENDING', 0, ?, 0, CURRENT_TIMESTAMP(3))",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, practiceId);
            ps.setString(2, payloadJson);
            ps.setString(3, correlationId);
            ps.setInt(4, maxRetry);
            return ps;
        }, keyHolder);

        Number keyNum = keyHolder.getKey();
        if (keyNum == null) {
            throw new IllegalStateException("Impossibile ottenere la PK di bpm_outbound_message");
        }
        long messageId = keyNum.longValue();

        try {
            return retryTemplate.execute(context -> {
                // Ogni tentativo: incrementa retry_count, aggiorna last_attempt_at, imposta stato_invio=2 (transiente)
                jdbcTemplate.update(
                        "UPDATE bpm_outbound_message " +
                        "SET retry_count = retry_count + 1, last_attempt_at = ?, stato_invio = 2 " +
                        "WHERE id = ?",
                        Timestamp.from(Instant.now()), messageId
                );
                String response = gateway.sendOutcome(payloadJson);
                // Successo: stato_invio=1
                jdbcTemplate.update(
                        "UPDATE bpm_outbound_message SET stato_invio = 1, response_json = ?, status = 'SENT' WHERE id = ?",
                        response, messageId
                );
                return response;
            });
        } catch (Exception ex) {
            // Retry esauriti: stato_invio=3 (scartato)
            jdbcTemplate.update(
                    "UPDATE bpm_outbound_message " +
                    "SET stato_invio = 3, status = 'FAILED', error_message = ? WHERE id = ?",
                    truncate(ex.getMessage(), 1000), messageId
            );
            log.error("Outbound BPM fallito dopo tutti i tentativi messageId={}: {}", messageId, ex.getMessage());
            throw new RuntimeException("Invio outcome BPM fallito dopo tutti i tentativi", ex);
        }
    }

    private RetryTemplate buildRetryTemplate(int maxAttempts, long intervalMs) {
        RetryTemplate template = new RetryTemplate();
        SimpleRetryPolicy policy = new SimpleRetryPolicy(maxAttempts);
        template.setRetryPolicy(policy);
        FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        backOff.setBackOffPeriod(intervalMs);
        template.setBackOffPolicy(backOff);
        return template;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
