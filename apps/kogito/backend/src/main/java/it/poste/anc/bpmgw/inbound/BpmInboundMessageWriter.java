package it.poste.anc.bpmgw.inbound;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writer dedicato alla tabella bpm_inbound_message in transazione REQUIRES_NEW.
 *
 * Usato dal flusso di apertura pratica quando il rollback della transazione
 * principale (es. AttachmentIngestionException) richiede di tracciare comunque
 * l'esito sulla tabella di idempotenza. Classe separata per evitare problemi
 * di self-invocation dei proxy Spring.
 */
@Service
public class BpmInboundMessageWriter {

    private final JdbcTemplate jdbcTemplate;

    public BpmInboundMessageWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistInboundMessageRequiresNew(
            String idWorkItem,
            String payloadJson,
            int resultCode,
            String resultMessage,
            Long practiceId
    ) {
        jdbcTemplate.update(
                "INSERT INTO bpm_inbound_message (id_work_item, payload_json, result_code, result_message, practice_id) " +
                        "VALUES (?, ?, ?, ?, ?)",
                idWorkItem,
                payloadJson,
                resultCode,
                resultMessage,
                practiceId
        );
    }
}
