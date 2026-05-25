package it.poste.anc.document.application;

import it.poste.anc.document.api.IntakeChecklistHelpResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class IntakeChecklistHelpService {

    private static final Map<String, HelpItem> DEFAULT_HELP_ITEMS = defaultHelpItems();

    private final JdbcTemplate jdbcTemplate;

    public IntakeChecklistHelpService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public IntakeChecklistHelpResponse getHelp(Long practiceId, String itemId, String actorUsername) {
        if (itemId == null || itemId.isBlank()) {
            throw new DocumentOperationException(HttpStatus.BAD_REQUEST, 4040, "itemId help obbligatorio");
        }

        PracticeContext context = loadPracticeContext(practiceId);
        HelpItem helpItem = loadHelpItem(context.documentType(), itemId.trim().toUpperCase(Locale.ROOT));

        jdbcTemplate.update(
                "INSERT INTO audit_event (occurred_at, actor_username, event_type, practice_id, correlation_id, payload_json) "
                        + "VALUES (CURRENT_TIMESTAMP(3), ?, 'CHECKLIST_HELP_VIEWED', ?, ?, JSON_OBJECT('itemId', ?, 'documentType', ?))",
                actorUsername,
                practiceId,
                "CHK_HELP_" + practiceId,
                helpItem.itemId(),
                context.documentType()
        );

        return new IntakeChecklistHelpResponse(
                practiceId,
                context.documentType(),
                helpItem.itemId(),
                helpItem.title(),
                helpItem.description()
        );
    }

    private PracticeContext loadPracticeContext(Long practiceId) {
        try {
            String documentType = jdbcTemplate.queryForObject(
                    "SELECT document_type FROM practice WHERE id = ?",
                    String.class,
                    practiceId
            );

            if (documentType == null || documentType.isBlank()) {
                throw new DocumentOperationException(HttpStatus.CONFLICT, 4023,
                        "Tipizzazione documento non confermata");
            }

            String normalizedType = documentType.toUpperCase(Locale.ROOT);
            if (!"VERBALE".equals(normalizedType) && !"CARTA".equals(normalizedType)) {
                throw new DocumentOperationException(HttpStatus.CONFLICT, 4024,
                        "Checklist disponibile solo per pratiche tipizzate VERBALE o CARTA");
            }

            return new PracticeContext(practiceId, normalizedType);
        } catch (EmptyResultDataAccessException ex) {
            throw new DocumentOperationException(HttpStatus.NOT_FOUND, 2004, "Pratica non trovata");
        }
    }

    private HelpItem loadHelpItem(String documentType, String itemId) {
        if (tableExists("checklist_item_catalog")) {
            List<HelpItem> rows = jdbcTemplate.query(
                "SELECT code, label, help_text FROM checklist_item_catalog "
                    + "WHERE document_type = ? AND code = ? AND active = 1",
                (rs, rowNum) -> new HelpItem(
                    rs.getString("code"),
                    rs.getString("label"),
                    rs.getString("help_text")
                ),
                documentType,
                itemId
            );
            if (!rows.isEmpty()) {
            return rows.getFirst();
            }
        }

        HelpItem fallback = DEFAULT_HELP_ITEMS.get(documentType + ":" + itemId);
        if (fallback != null) {
            return fallback;
        }

        throw new DocumentOperationException(HttpStatus.NOT_FOUND, 4041,
            "Descrizione help non disponibile per item " + itemId);
        }

        private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
            Integer.class,
            tableName
        );
        return count != null && count > 0;
        }

        private static Map<String, HelpItem> defaultHelpItems() {
        Map<String, HelpItem> map = new LinkedHashMap<>();

        map.put("VERBALE:DOCUMENTPRESENT", new HelpItem(
            "DOCUMENTPRESENT",
            "Documento presente",
            "Indica se il verbale e presente tra gli allegati visualizzati."
        ));
        map.put("VERBALE:READABILITYOK", new HelpItem(
            "READABILITYOK",
            "Leggibilita",
            "Conferma che tutti i dati necessari sul verbale sono leggibili in modo chiaro."
        ));
        map.put("VERBALE:FORMALOK", new HelpItem(
            "FORMALOK",
            "Conformita formale",
            "Verifica che il verbale rispetti i requisiti formali previsti dalla checklist ANC."
        ));
        map.put("VERBALE:CUSTOMERDATAOK", new HelpItem(
            "CUSTOMERDATAOK",
            "Dati cliente",
            "Conferma coerenza tra dati cliente in pratica e dati riportati nel verbale."
        ));
        map.put("VERBALE:CARDNUMBERMATCHREQUIRED", new HelpItem(
            "CARDNUMBERMATCHREQUIRED",
            "Confronto numero carta richiesto",
            "Attiva il controllo puntuale del numero carta quando necessario."
        ));
        map.put("VERBALE:CARDNUMBERMATCHOK", new HelpItem(
            "CARDNUMBERMATCHOK",
            "Numero carta coerente",
            "Conferma che il numero carta presente nel verbale coincide con la carta della pratica."
        ));
        map.put("VERBALE:KOREASONS", new HelpItem(
            "KOREASONS",
            "Causali KO formali",
            "Se conformita formale e negativa, selezionare almeno una causale KO coerente."
        ));
        map.put("VERBALE:INTERNALNOTES", new HelpItem(
            "INTERNALNOTES",
            "Note interne",
            "Campo libero per annotazioni operative interne non visibili al cliente."
        ));
        map.put("CARTA:CARDPRESENT", new HelpItem(
            "CARDPRESENT",
            "Carta presente",
            "Indica se la carta fisica e disponibile per i controlli richiesti."
        ));
        map.put("CARTA:CARDCONFORMITYOK", new HelpItem(
            "CARDCONFORMITYOK",
            "Conformita carta",
            "Conferma che la carta e conforme ai requisiti previsti dalla procedura ANC."
        ));
        map.put("CARTA:INTERNALNOTES", new HelpItem(
            "INTERNALNOTES",
            "Note interne",
            "Campo libero per annotazioni operative interne non visibili al cliente."
        ));

        return Map.copyOf(map);
    }

    private record PracticeContext(Long practiceId, String documentType) {
    }

    private record HelpItem(String itemId, String title, String description) {
    }
}
