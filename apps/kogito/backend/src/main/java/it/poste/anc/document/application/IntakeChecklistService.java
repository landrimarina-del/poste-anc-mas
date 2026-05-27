package it.poste.anc.document.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.poste.anc.document.api.IntakeChecklistEditResponse;
import it.poste.anc.document.api.IntakeChecklistRequest;
import it.poste.anc.document.api.IntakeChecklistResponse;
import it.poste.anc.document.application.OutcomeDmnService.OutcomeComputed;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class IntakeChecklistService {

    private static final String DOC_TYPE_VERBALE = "VERBALE";
    private static final String DOC_TYPE_CARTA = "CARTA";
    private static final String STATUS_NON_INIZIATA = "NON_INIZIATA";
    private static final String STATUS_BOZZA = "BOZZA";
    private static final String STATUS_RIAPERTA = "RIAPERTA";
    private static final String STATUS_CONSOLIDATA = "CONSOLIDATA";
    private static final String OUTCOME_APPROVATA = "APPROVATA";
    private static final String OUTCOME_RESPINTA = "RESPINTA";
    private static final Set<String> ALLOWED_FORMAL_KO_REASONS = Set.of(
            "INTESTAZIONE",
            "FIRME",
            "TIMBRO",
            "DICHIARAZIONE",
            "CARTA_PI"
    );

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final OutcomeDmnService outcomeDmnService;

    public IntakeChecklistService(JdbcTemplate jdbcTemplate,
                                  ObjectMapper objectMapper,
                                  OutcomeDmnService outcomeDmnService) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.outcomeDmnService = outcomeDmnService;
    }

    @Transactional(readOnly = true)
    public IntakeChecklistResponse loadChecklist(Long practiceId) {
        PracticeContext practiceContext = loadPracticeContext(practiceId);
        OutcomeRow outcome = loadOutcomeRow(practiceId).orElse(null);
        if (DOC_TYPE_VERBALE.equals(practiceContext.documentType())) {
            ChecklistVerbaleRow checklist = loadVerbaleChecklistRow(practiceId)
                .orElse(ChecklistVerbaleRow.defaultFor(practiceId));
            return toVerbaleResponse(practiceContext.documentType(), checklist, outcome);
        }

        ChecklistCartaRow checklist = loadCartaChecklistRow(practiceId)
            .orElse(ChecklistCartaRow.defaultFor(practiceId));
        return toCartaResponse(practiceContext.documentType(), checklist, outcome);
    }

    @Transactional
    public IntakeChecklistResponse saveDraft(Long practiceId, IntakeChecklistRequest request, String actorUsername) {
        PracticeContext practiceContext = loadPracticeContext(practiceId);
        OutcomeComputed computed;
        IntakeChecklistResponse response;

        if (DOC_TYPE_VERBALE.equals(practiceContext.documentType())) {
            ChecklistVerbaleRow current = loadVerbaleChecklistRow(practiceId)
                .orElse(ChecklistVerbaleRow.defaultFor(practiceId));
            ChecklistVerbaleRow validated = validateAndNormalizeVerbale(practiceId, request, current.createdAt(), STATUS_BOZZA);
            upsertVerbaleChecklist(validated);
            computed = computeVerbaleOutcome(validated);
            response = toVerbaleResponse(practiceContext.documentType(),
                validated,
                new OutcomeRow(practiceId, computed.outcome(), computed.koCodes()));
        } else {
            ChecklistCartaRow current = loadCartaChecklistRow(practiceId)
                .orElse(ChecklistCartaRow.defaultFor(practiceId));
            ChecklistCartaRow validated = validateAndNormalizeCarta(practiceId, request, current.createdAt(), STATUS_BOZZA);
            upsertCartaChecklist(validated);
            computed = computeCartaOutcome(validated);
            response = toCartaResponse(practiceContext.documentType(),
                validated,
                new OutcomeRow(practiceId, computed.outcome(), computed.koCodes()));
        }

        upsertOutcome(practiceId, computed, actorUsername);

        insertAuditEvent(
                actorUsername,
                "CHECKLIST_SAVED",
                practiceId,
                "CHK_SAVE_" + practiceId,
                "{\"status\":\"" + STATUS_BOZZA + "\",\"outcome\":\"" + computed.outcome() + "\"}"
        );

        return response;
    }

    @Transactional
    public IntakeChecklistEditResponse reopenDraft(Long practiceId, String actorUsername) {
        PracticeContext practiceContext = loadPracticeContext(practiceId);
        String currentStatus;

        if (DOC_TYPE_VERBALE.equals(practiceContext.documentType())) {
            ChecklistVerbaleRow current = loadVerbaleChecklistRow(practiceId)
                .orElseThrow(() -> new DocumentOperationException(
                    HttpStatus.CONFLICT,
                    4025,
                    "Checklist verbale non disponibile per modifica"
                ));
            currentStatus = current.status();
            if (STATUS_BOZZA.equals(currentStatus) || STATUS_CONSOLIDATA.equals(currentStatus)) {
            jdbcTemplate.update(
                "UPDATE checklist_verbale SET status = ?, updated_at = CURRENT_TIMESTAMP(3) WHERE practice_id = ?",
                STATUS_RIAPERTA,
                practiceId
            );
            insertAuditEvent(
                actorUsername,
                "CHECKLIST_REOPENED",
                practiceId,
                "CHK_REOPEN_" + practiceId,
                "{\"status\":\"RIAPERTA\"}"
            );
            return new IntakeChecklistEditResponse(practiceId, STATUS_RIAPERTA);
            }
            return new IntakeChecklistEditResponse(practiceId, currentStatus);
        }

        ChecklistCartaRow current = loadCartaChecklistRow(practiceId)
            .orElseThrow(() -> new DocumentOperationException(
                HttpStatus.CONFLICT,
                4026,
                "Checklist carta non disponibile per modifica"
            ));
        currentStatus = current.status();
        if (STATUS_BOZZA.equals(currentStatus) || STATUS_CONSOLIDATA.equals(currentStatus)) {
            jdbcTemplate.update(
                "UPDATE checklist_carta SET status = ?, updated_at = CURRENT_TIMESTAMP(3) WHERE practice_id = ?",
                STATUS_RIAPERTA,
                practiceId
            );
            insertAuditEvent(
                    actorUsername,
                    "CHECKLIST_REOPENED",
                    practiceId,
                    "CHK_REOPEN_" + practiceId,
                    "{\"status\":\"RIAPERTA\"}"
            );
            return new IntakeChecklistEditResponse(practiceId, STATUS_RIAPERTA);
        }

        return new IntakeChecklistEditResponse(practiceId, currentStatus);
    }

        private PracticeContext loadPracticeContext(Long practiceId) {
        try {
            String documentType = jdbcTemplate.queryForObject(
                    "SELECT document_type FROM practice WHERE id = ?",
                    String.class,
                    practiceId
            );
            if (documentType == null) {
                throw new DocumentOperationException(HttpStatus.CONFLICT, 4023,
                        "Tipizzazione documento non confermata");
            }
            String normalizedType = documentType.toUpperCase(Locale.ROOT);
            if (!DOC_TYPE_VERBALE.equals(normalizedType) && !DOC_TYPE_CARTA.equals(normalizedType)) {
                throw new DocumentOperationException(HttpStatus.CONFLICT, 4024,
                "Checklist disponibile solo per pratiche tipizzate VERBALE o CARTA");
            }
            return new PracticeContext(practiceId, normalizedType);
        } catch (EmptyResultDataAccessException ex) {
            throw new DocumentOperationException(HttpStatus.NOT_FOUND, 2004, "Pratica non trovata");
        }
    }

        private java.util.Optional<ChecklistVerbaleRow> loadVerbaleChecklistRow(Long practiceId) {
        List<ChecklistVerbaleRow> rows = jdbcTemplate.query(
                "SELECT practice_id, document_present, readability_ok, formal_ok, customer_data_ok, "
                + "card_number_match_required, card_number_match_ok, ko_reasons_json, internal_notes, "
                + "note_legibility, note_formal_suitability, note_client_data_consistency, note_card_number_match, final_note_practice, "
                + "status, created_at, codice_causale_id "
                        + "FROM checklist_verbale WHERE practice_id = ?",
            (rs, rowNum) -> new ChecklistVerbaleRow(
                        rs.getLong("practice_id"),
                        rs.getBoolean("document_present"),
                        readNullableBoolean(rs, "readability_ok"),
                        readNullableBoolean(rs, "formal_ok"),
                        readNullableBoolean(rs, "customer_data_ok"),
                        rs.getBoolean("card_number_match_required"),
                        readNullableBoolean(rs, "card_number_match_ok"),
                        parseJsonArray(rs.getString("ko_reasons_json")),
                        rs.getString("internal_notes"),
                        rs.getString("note_legibility"),
                        rs.getString("note_formal_suitability"),
                        rs.getString("note_client_data_consistency"),
                        rs.getString("note_card_number_match"),
                        rs.getString("final_note_practice"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at"),
                        readNullableLong(rs, "codice_causale_id"),
                        // Sotto-voci non persistite come colonne separate: null al caricamento
                        null, null, null, null, null
                ),
                practiceId
        );
        if (rows.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(rows.get(0));
    }

    private java.util.Optional<ChecklistCartaRow> loadCartaChecklistRow(Long practiceId) {
        List<ChecklistCartaRow> rows = jdbcTemplate.query(
                "SELECT practice_id, card_present, card_conformity_ok, card_legibility_ok, internal_notes, status, created_at, codice_causale_id "
                        + "FROM checklist_carta WHERE practice_id = ?",
                (rs, rowNum) -> new ChecklistCartaRow(
                        rs.getLong("practice_id"),
                        rs.getBoolean("card_present"),
                        readNullableBoolean(rs, "card_conformity_ok"),
                        readNullableBoolean(rs, "card_legibility_ok"),
                        rs.getString("internal_notes"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at"),
                        readNullableLong(rs, "codice_causale_id")
                ),
                practiceId
        );
        if (rows.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(rows.get(0));
    }

    private java.util.Optional<OutcomeRow> loadOutcomeRow(Long practiceId) {
        List<OutcomeRow> rows = jdbcTemplate.query(
                "SELECT practice_id, outcome, ko_codes_json FROM practice_outcome WHERE practice_id = ?",
                (rs, rowNum) -> new OutcomeRow(
                        rs.getLong("practice_id"),
                        rs.getString("outcome"),
                        parseJsonArray(rs.getString("ko_codes_json"))
                ),
                practiceId
        );
        if (rows.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(rows.get(0));
    }

    private ChecklistVerbaleRow validateAndNormalizeVerbale(Long practiceId,
                                                            IntakeChecklistRequest request,
                                                            Timestamp createdAt,
                                                            String nextStatus) {
        if (request.documentPresent() == null) {
            throw new DocumentOperationException(HttpStatus.BAD_REQUEST, 4020,
                    "Campo documentPresent obbligatorio");
        }

        boolean documentPresent = request.documentPresent();
        boolean cardRequired = request.cardNumberMatchRequired() != null && request.cardNumberMatchRequired();

        Boolean readability = request.readabilityOk();
        Boolean intestazione = request.intestazioneOk();
        Boolean firme = request.firmeOk();
        Boolean intestazioneConformeTimbro = request.intestazioneConformeAlTimbroOk();
        Boolean dichiarazioneConformeFirme = request.dichiarazioneConformeAlleFirmeOk();
        Boolean cartaPosteItaliane = request.cartaPosteItalianeOk();
        Boolean customerData = request.customerDataOk();
        Boolean cardMatch = request.cardNumberMatchOk();

        if (!documentPresent) {
            readability = null;
            intestazione = null;
            firme = null;
            intestazioneConformeTimbro = null;
            dichiarazioneConformeFirme = null;
            cartaPosteItaliane = null;
            customerData = null;
            cardRequired = false;
            cardMatch = null;
        } else {
            requireNotNull(readability, "readabilityOk");
            requireNotNull(intestazione, "intestazioneOk");
            requireNotNull(firme, "firmeOk");
            requireNotNull(intestazioneConformeTimbro, "intestazioneConformeAlTimbroOk");
            requireNotNull(dichiarazioneConformeFirme, "dichiarazioneConformeAlleFirmeOk");
            requireNotNull(cartaPosteItaliane, "cartaPosteItalianeOk");
            requireNotNull(customerData, "customerDataOk");

            if (cardRequired) {
                requireNotNull(cardMatch, "cardNumberMatchOk");
            } else {
                cardMatch = null;
            }
        }

        // Aggrega formalOk per compatibilità con la colonna DB (formal_ok)
        Boolean formal = (intestazione != null)
                ? (intestazione && firme && intestazioneConformeTimbro
                   && dichiarazioneConformeFirme && cartaPosteItaliane)
                : null;

        return new ChecklistVerbaleRow(
                practiceId,
                documentPresent,
                readability,
                formal,
                customerData,
                cardRequired,
                cardMatch,
                List.of(),
                request.internalNotes(),
                request.noteLegibility(),
                request.noteFormalSuitability(),
                request.noteClientDataConsistency(),
                request.noteCardNumberMatch(),
                request.finalNotePractice(),
                nextStatus,
                createdAt != null ? createdAt : Timestamp.from(Instant.now()),
                request.codiceCausaleId(),
                intestazione,
                firme,
                intestazioneConformeTimbro,
                dichiarazioneConformeFirme,
                cartaPosteItaliane
        );
    }

    private void requireNotNull(Boolean value, String fieldName) {
        if (value == null) {
            throw new DocumentOperationException(HttpStatus.BAD_REQUEST, 4020,
                    "Campo " + fieldName + " obbligatorio");
        }
    }

    private List<String> normalizeFormalReasons(List<String> inputReasons) {
        if (inputReasons == null || inputReasons.isEmpty()) {
            return List.of();
        }

        Set<String> unique = new LinkedHashSet<>();
        for (String item : inputReasons) {
            if (item == null || item.isBlank()) {
                continue;
            }
            String normalized = item.trim().toUpperCase(Locale.ROOT);
            if (!ALLOWED_FORMAL_KO_REASONS.contains(normalized)) {
                throw new DocumentOperationException(HttpStatus.BAD_REQUEST, 4022,
                        "Causale KO non valida: " + normalized);
            }
            unique.add(normalized);
        }
        return List.copyOf(unique);
    }

    private ChecklistCartaRow validateAndNormalizeCarta(Long practiceId,
                                                        IntakeChecklistRequest request,
                                                        Timestamp createdAt,
                                                        String nextStatus) {
        if (request.cardPresent() == null) {
            throw new DocumentOperationException(HttpStatus.BAD_REQUEST, 4030,
                    "Campo cardPresent obbligatorio");
        }

        boolean cardPresent = request.cardPresent();
        Boolean cardConformityOk = request.cardConformityOk();
        Boolean legibilityOk = request.cardLegibilityOk();
        if (!cardPresent) {
            cardConformityOk = null;
            legibilityOk = null;
        } else {
            requireNotNull(cardConformityOk, "cardConformityOk");
            requireNotNull(legibilityOk, "cardLegibilityOk");
        }

        return new ChecklistCartaRow(
                practiceId,
                cardPresent,
                cardConformityOk,
                legibilityOk,
                request.internalNotes(),
                nextStatus,
                createdAt != null ? createdAt : Timestamp.from(Instant.now()),
                request.codiceCausaleId()
        );
    }

    private void upsertVerbaleChecklist(ChecklistVerbaleRow row) {
        String koReasonsJson = toJsonArray(row.koReasons());
        int updated = jdbcTemplate.update(
                "UPDATE checklist_verbale SET document_present = ?, readability_ok = ?, formal_ok = ?, customer_data_ok = ?, "
                + "card_number_match_required = ?, card_number_match_ok = ?, ko_reasons_json = ?, codice_causale_id = ?, internal_notes = ?, "
                + "note_legibility = ?, note_formal_suitability = ?, note_client_data_consistency = ?, note_card_number_match = ?, final_note_practice = ?, "
                        + "status = ?, updated_at = CURRENT_TIMESTAMP(3) WHERE practice_id = ?",
                row.documentPresent(),
                row.readabilityOk(),
                row.formalOk(),
                row.customerDataOk(),
                row.cardNumberMatchRequired(),
                row.cardNumberMatchOk(),
                koReasonsJson,
                row.codiceCausaleId(),
                row.internalNotes(),
                row.noteLegibility(),
                row.noteFormalSuitability(),
                row.noteClientDataConsistency(),
                row.noteCardNumberMatch(),
                row.finalNotePractice(),
                row.status(),
                row.practiceId()
        );

        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO checklist_verbale (practice_id, document_present, readability_ok, formal_ok, customer_data_ok, "
                        + "card_number_match_required, card_number_match_ok, ko_reasons_json, codice_causale_id, internal_notes, "
                        + "note_legibility, note_formal_suitability, note_client_data_consistency, note_card_number_match, final_note_practice, "
                        + "status, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))",
                    row.practiceId(),
                    row.documentPresent(),
                    row.readabilityOk(),
                    row.formalOk(),
                    row.customerDataOk(),
                    row.cardNumberMatchRequired(),
                    row.cardNumberMatchOk(),
                    koReasonsJson,
                    row.codiceCausaleId(),
                    row.internalNotes(),
                    row.noteLegibility(),
                    row.noteFormalSuitability(),
                    row.noteClientDataConsistency(),
                    row.noteCardNumberMatch(),
                    row.finalNotePractice(),
                    row.status()
            );
        }
    }

                private void upsertCartaChecklist(ChecklistCartaRow row) {
                int updated = jdbcTemplate.update(
                    "UPDATE checklist_carta SET card_present = ?, card_conformity_ok = ?, card_legibility_ok = ?, codice_causale_id = ?, internal_notes = ?, "
                        + "status = ?, updated_at = CURRENT_TIMESTAMP(3) WHERE practice_id = ?",
                    row.cardPresent(),
                    row.cardConformityOk(),
                    row.legibilityOk(),
                    row.codiceCausaleId(),
                    row.internalNotes(),
                    row.status(),
                    row.practiceId()
                );

                if (updated == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO checklist_carta (practice_id, card_present, card_conformity_ok, card_legibility_ok, codice_causale_id, internal_notes, status, created_at, updated_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))",
                        row.practiceId(),
                        row.cardPresent(),
                        row.cardConformityOk(),
                        row.legibilityOk(),
                        row.codiceCausaleId(),
                        row.internalNotes(),
                        row.status()
                    );
                }
                }

                private OutcomeComputed computeVerbaleOutcome(ChecklistVerbaleRow row) {
        return outcomeDmnService.computeVerbaleOutcome(
                row.documentPresent(),
                row.readabilityOk(),
                row.intestazioneOk(),
                row.firmeOk(),
                row.intestazioneConformeAlTimbroOk(),
                row.dichiarazioneConformeAlleFirmeOk(),
                row.cartaPosteItalianeOk(),
                row.customerDataOk(),
                row.cardNumberMatchRequired(),
                row.cardNumberMatchOk()
        );
    }

    private OutcomeComputed computeCartaOutcome(ChecklistCartaRow row) {
        return outcomeDmnService.computeCartaOutcome(
                row.cardPresent(),
                row.legibilityOk(),
                row.cardConformityOk()
        );
    }

    private void upsertOutcome(Long practiceId, OutcomeComputed computed, String actorUsername) {
        int updated = jdbcTemplate.update(
                "UPDATE practice_outcome SET outcome = ?, ko_codes_json = ?, computed_at = CURRENT_TIMESTAMP(3), computed_by = ? "
                        + "WHERE practice_id = ?",
                computed.outcome(),
                toJsonArray(computed.koCodes()),
                actorUsername,
                practiceId
        );

        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO practice_outcome (practice_id, outcome, ko_codes_json, computed_at, computed_by) "
                            + "VALUES (?, ?, ?, CURRENT_TIMESTAMP(3), ?)",
                    practiceId,
                    computed.outcome(),
                    toJsonArray(computed.koCodes()),
                    actorUsername
            );
        }
    }

    private void insertAuditEvent(String actorUsername,
                                  String eventType,
                                  Long practiceId,
                                  String correlationId,
                                  String payloadJson) {
        jdbcTemplate.update(
                "INSERT INTO audit_event (occurred_at, actor_username, event_type, practice_id, correlation_id, payload_json) "
                        + "VALUES (CURRENT_TIMESTAMP(3), ?, ?, ?, ?, ?)",
                actorUsername,
                eventType,
                practiceId,
                correlationId,
                payloadJson
        );
    }

    private IntakeChecklistResponse toVerbaleResponse(String documentType,
                                                      ChecklistVerbaleRow checklist,
                                                      OutcomeRow outcome) {
        String finalNotePractice = checklist.finalNotePractice();

        return new IntakeChecklistResponse(
                checklist.practiceId(),
                documentType,
                checklist.status(),
                checklist.documentPresent(),
                checklist.readabilityOk(),
                checklist.formalOk(),
                checklist.customerDataOk(),
                checklist.cardNumberMatchRequired(),
                checklist.cardNumberMatchOk(),
            checklist.noteLegibility(),
            checklist.noteFormalSuitability(),
            checklist.noteClientDataConsistency(),
            checklist.noteCardNumberMatch(),
            finalNotePractice,
                null,
                null,
                checklist.koReasons(),
                checklist.internalNotes(),
                outcome != null ? outcome.outcome() : null,
                outcome != null ? outcome.koCodes() : List.of(),
                checklist.codiceCausaleId(),
                null // cardLegibilityOk: non applicabile per VERBALE
        );
    }

    private IntakeChecklistResponse toCartaResponse(String documentType,
                                                    ChecklistCartaRow checklist,
                                                    OutcomeRow outcome) {
        return new IntakeChecklistResponse(
                checklist.practiceId(),
                documentType,
                checklist.status(),
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
                checklist.cardPresent(),
                checklist.cardConformityOk(),
                List.of(),
                checklist.internalNotes(),
                outcome != null ? outcome.outcome() : null,
                outcome != null ? outcome.koCodes() : List.of(),
                checklist.codiceCausaleId(),
                checklist.legibilityOk()
        );
    }

    private String toJsonArray(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Errore serializzazione JSON", ex);
        }
    }

    private List<String> parseJsonArray(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    private Boolean readNullableBoolean(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        boolean value = rs.getBoolean(column);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    private Long readNullableLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    @Transactional(readOnly = true)
    public List<it.poste.anc.document.api.CausaleChecklistDto> loadCausali(String categoria) {
        return jdbcTemplate.query(
                "SELECT id, codice, descrizione FROM ref_causali_checklist "
                        + "WHERE categoria = ? AND attivo = 1 ORDER BY codice",
                (rs, rowNum) -> new it.poste.anc.document.api.CausaleChecklistDto(
                        rs.getLong("id"),
                        rs.getString("codice"),
                        rs.getString("descrizione")
                ),
                categoria.toUpperCase(Locale.ROOT)
        );
    }

    private record ChecklistVerbaleRow(Long practiceId,
                                       boolean documentPresent,
                                       Boolean readabilityOk,
                                       Boolean formalOk,
                                       Boolean customerDataOk,
                                       boolean cardNumberMatchRequired,
                                       Boolean cardNumberMatchOk,
                                       List<String> koReasons,
                                       String internalNotes,
                                       String noteLegibility,
                                       String noteFormalSuitability,
                                       String noteClientDataConsistency,
                                       String noteCardNumberMatch,
                                       String finalNotePractice,
                                       String status,
                                       Timestamp createdAt,
                                       Long codiceCausaleId,
                                       // Sotto-voci VERBALE (non persistite come colonne separate)
                                       Boolean intestazioneOk,
                                       Boolean firmeOk,
                                       Boolean intestazioneConformeAlTimbroOk,
                                       Boolean dichiarazioneConformeAlleFirmeOk,
                                       Boolean cartaPosteItalianeOk) {
        static ChecklistVerbaleRow defaultFor(Long practiceId) {
            return new ChecklistVerbaleRow(
                    practiceId,
                    true,
                    null,
                    null,
                    null,
                    false,
                    null,
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    STATUS_NON_INIZIATA,
                    Timestamp.from(Instant.now()),
                    null,
                    null, null, null, null, null
            );
        }
    }

    private record ChecklistCartaRow(Long practiceId,
                                     boolean cardPresent,
                                     Boolean cardConformityOk,
                                     // legibilityOk: leggibilità carta (PPEZ034), non persistita come colonna separata
                                     Boolean legibilityOk,
                                     String internalNotes,
                                     String status,
                                     Timestamp createdAt,
                                     Long codiceCausaleId) {
        static ChecklistCartaRow defaultFor(Long practiceId) {
            return new ChecklistCartaRow(
                    practiceId,
                    true,
                    null,
                    null,
                    null,
                    STATUS_NON_INIZIATA,
                    Timestamp.from(Instant.now()),
                    null
            );
        }
    }

    private record PracticeContext(Long practiceId, String documentType) {
    }

    private record OutcomeRow(Long practiceId, String outcome, List<String> koCodes) {
    }
}
