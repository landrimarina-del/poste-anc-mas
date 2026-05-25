package it.poste.anc.bpmgw.inbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.poste.anc.document.ingestion.AttachmentFetcher;
import it.poste.anc.document.ingestion.AttachmentIngestionError;
import it.poste.anc.document.ingestion.AttachmentIngestionException;
import it.poste.anc.document.ingestion.AttachmentStorage;
import it.poste.anc.document.ingestion.FetchedAttachment;
import it.poste.anc.shared.common.ApiResponse;
import it.poste.anc.ticketing.TicketingClient;
import it.poste.anc.workflow.engine.BpmEngineAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BpmPracticeInboundService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final AttachmentFetcher attachmentFetcher;
    private final AttachmentStorage attachmentStorage;
    private final BpmInboundMessageWriter inboundMessageWriter;
    private final TransactionTemplate transactionTemplate;
    private final TicketingClient ticketingClient;
    private final BpmEngineAdapter bpmEngineAdapter;
    private final boolean ticketingEnabled;

    public BpmPracticeInboundService(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            AttachmentFetcher attachmentFetcher,
            AttachmentStorage attachmentStorage,
            BpmInboundMessageWriter inboundMessageWriter,
            PlatformTransactionManager transactionManager,
            TicketingClient ticketingClient,
            BpmEngineAdapter bpmEngineAdapter,
            @Value("${ticketing.enabled:true}") boolean ticketingEnabled
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.attachmentFetcher = attachmentFetcher;
        this.attachmentStorage = attachmentStorage;
        this.inboundMessageWriter = inboundMessageWriter;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.ticketingClient = ticketingClient;
        this.bpmEngineAdapter = bpmEngineAdapter;
        this.ticketingEnabled = ticketingEnabled;
    }

    public ApiResponse<BpmPracticeOpenResponse> openPractice(JsonNode payloadNode) {
        try {
            return transactionTemplate.execute(status -> openPracticeTransactional(payloadNode));
        } catch (AttachmentIngestionException ex) {
            // GAP-BLOCKER-001: la transazione e' stata gia' rollbackata da TransactionTemplate;
            // tracciamo l'esito su bpm_inbound_message in REQUIRES_NEW e ritorniamo -4.
            String idWorkItem = extractIdWorkItem(payloadNode);
            String payloadJson = payloadNode == null ? "{}" : payloadNode.toString();
            String message = ex.getMessage();
            inboundMessageWriter.persistInboundMessageRequiresNew(
                    idWorkItem == null ? "UNKNOWN" : idWorkItem,
                    payloadJson,
                    -4,
                    truncate(message, 500),
                    null
            );
            return ApiResponse.error(-4, buildErrorMessageWithDetails(ex.getErrors()));
        }
    }

    private ApiResponse<BpmPracticeOpenResponse> openPracticeTransactional(JsonNode payloadNode) {
        if (payloadNode == null || payloadNode.isNull() || payloadNode.isMissingNode() || !payloadNode.isObject()) {
            return ApiResponse.error(-4, "Messaggio non valido: payload obbligatorio");
        }

        BpmPracticeOpenRequest request = objectMapper.convertValue(payloadNode, BpmPracticeOpenRequest.class);
        String idWorkItem = safeTrim(request.getIdWorkItem());
        String payloadJson = payloadNode.toString();
        String canale = safeTrim(request.getCanale());
        String numPratica = safeTrim(request.getNumPratica());
        String cfCliente = normalizeCfCliente(request);
        String codiceCliente = safeTrim(request.getCodiceCliente());
        Timestamp dataInserimentoRichiesta = parseItalianTimestamp(request.getDataInserimentoRichiesta());
        BpmPracticeOpenRequest.ClientData cliente = request.getCliente();
        BpmPracticeOpenRequest.CardData cardData = request.getDatiCartaBloccata();

        if (idWorkItem == null
                || canale == null
                || numPratica == null
                || cfCliente == null
                || dataInserimentoRichiesta == null
            || !hasRequiredClientData(cliente)
            || !hasRequiredBlockedCardData(cardData)
                || !hasDocuments(request.getDocumenti())) {
            persistInboundMessage(orDefault(idWorkItem, "UNKNOWN"), payloadJson, -4,
                    "Messaggio non valido: campi obbligatori assenti o sezione DOCUMENTI non valida", null);
            return ApiResponse.error(-4,
                    "Messaggio non valido: campi obbligatori assenti o sezione DOCUMENTI non valida");
        }

        if (!hasSupportedDocumentCodes(request.getDocumenti())) {
            persistInboundMessage(idWorkItem, payloadJson, -4,
                    "Messaggio non valido: CODICE_DOC_ID consentiti {1,2,3}", null);
            return ApiResponse.error(-4, "Messaggio non valido: CODICE_DOC_ID consentiti {1,2,3}");
        }

        if (isDuplicate(idWorkItem)) {
            persistInboundMessage(idWorkItem, payloadJson, -5,
                    "Idempotenza violata: ID_WORKITEM gia' presente", null);
            return ApiResponse.error(-5, "Idempotenza violata: ID_WORKITEM gia' presente");
        }

        String requestId = numPratica;

    Long practiceId = insertPractice(requestId, idWorkItem, canale, numPratica, cfCliente, codiceCliente, dataInserimentoRichiesta);
    insertClientData(practiceId, request.getCliente(), cfCliente);
    insertCardData(practiceId, request.getDatiCartaBloccata());
    ingestAndInsertAttachments(practiceId, idWorkItem, payloadJson, request.getDocumenti());
    insertStateHistory(practiceId, idWorkItem, "APERTA");
    insertAuditEvent(practiceId, idWorkItem, payloadJson, "PRACTICE_OPENED");
        persistInboundMessage(idWorkItem, payloadJson, 0, "OK", practiceId);

        if (ticketingEnabled) {
            String ticketId = ticketingClient.openTicket(idWorkItem, canale);
            if (ticketId != null) {
                jdbcTemplate.update("UPDATE practice SET ticket_id = ? WHERE id = ?", ticketId, practiceId);
            }
        }

        // Avvia l'istanza di processo Kogito
        Map<String, Object> processVars = new HashMap<>();
        processVars.put("practiceId", practiceId);
        processVars.put("numPratica", numPratica);
        processVars.put("canale", canale);
        processVars.put("cfCliente", cfCliente);
        processVars.put("idWorkItem", idWorkItem);
        bpmEngineAdapter.startProcess("anc_pratica", numPratica, processVars);

        return ApiResponse.ok(new BpmPracticeOpenResponse(practiceId, requestId, "APERTA"));
    }

    private boolean hasDocuments(List<BpmPracticeOpenRequest.DocumentData> documents) {
        if (documents == null || documents.isEmpty()) {
            return false;
        }
        return documents.stream().allMatch(doc ->
                doc.getContenuti() != null
                        && !doc.getContenuti().isEmpty()
                        && doc.getContenuti().stream().allMatch(this::hasRequiredContent)
        );
    }

    private boolean hasRequiredClientData(BpmPracticeOpenRequest.ClientData cliente) {
        return cliente != null
                && safeTrim(cliente.getNome()) != null
                && safeTrim(cliente.getCognome()) != null
                && parseBirthDate(cliente.getDataNascita()) != null
                && safeTrim(cliente.getComuneNascita()) != null
                && safeTrim(cliente.getProvinciaNascita()) != null
                && safeTrim(cliente.getNazioneNascita()) != null
                && safeTrim(cliente.getCittadinanza()) != null;
    }

    private boolean hasRequiredBlockedCardData(BpmPracticeOpenRequest.CardData cardData) {
        return cardData != null
                && safeTrim(cardData.getNumeroCarta()) != null
                && safeTrim(cardData.getTipoCarta()) != null;
    }

    private boolean hasRequiredContent(BpmPracticeOpenRequest.DocumentContentData contentData) {
        return contentData != null
                && safeTrim(contentData.getNomeFile()) != null
                && safeTrim(contentData.getEstensione()) != null
                && safeTrim(contentData.getIdDoc()) != null
                && safeTrim(contentData.getLinkDownload()) != null;
    }

    private boolean hasSupportedDocumentCodes(List<BpmPracticeOpenRequest.DocumentData> documents) {
        Set<Integer> allowedCodes = Set.of(1, 2, 3);
        return documents.stream()
                .map(BpmPracticeOpenRequest.DocumentData::getCodiceDocId)
                .allMatch(code -> code != null && allowedCodes.contains(code));
    }

    private boolean isDuplicate(String idWorkItem) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM practice WHERE id_work_item = ?",
                Integer.class,
                idWorkItem
        );
        return count != null && count > 0;
    }

        private Long insertPractice(
            String requestId,
            String idWorkItem,
            String canale,
            String numPratica,
            String cfCliente,
                String codiceCliente,
            Timestamp dataInserimentoRichiesta
        ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO practice " +
                        "(request_id, id_work_item, canale, num_pratica, cf_cliente, codice_cliente, data_inserimento_richiesta, stato) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, requestId);
            ps.setString(2, idWorkItem);
            ps.setString(3, canale);
            ps.setString(4, numPratica);
            ps.setString(5, cfCliente);
                ps.setString(6, codiceCliente);
                ps.setTimestamp(7, dataInserimentoRichiesta);
                ps.setString(8, "APERTA");
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Impossibile ottenere la PK della pratica creata");
        }
        return key.longValue();
    }

        private void insertClientData(Long practiceId, BpmPracticeOpenRequest.ClientData clientData, String cfCliente) {
        BpmPracticeOpenRequest.ResidenceAddressData address = clientData.getIndirizzoDiResidenza();
        LocalDate birthDate = parseBirthDate(clientData.getDataNascita());
        jdbcTemplate.update(
            "INSERT INTO client_data (" +
                "practice_id, cognome, nome, codice_fiscale, documento_tipo, documento_num, data_nascita, " +
                "sesso, comune_nascita, provincia_nascita, nazione_nascita, cittadinanza, cellulare, telefono, " +
                "residenza_luogo, residenza_comune, residenza_provincia, residenza_nazione, residenza_cap, residenza_civico" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            practiceId,
            safeTrim(clientData.getCognome()),
            safeTrim(clientData.getNome()),
            cfCliente,
            safeTrim(clientData.getDocumentoTipo()),
            safeTrim(clientData.getDocumentoNum()),
            birthDate,
            safeTrim(clientData.getSesso()),
            safeTrim(clientData.getComuneNascita()),
            safeTrim(clientData.getProvinciaNascita()),
            safeTrim(clientData.getNazioneNascita()),
            safeTrim(clientData.getCittadinanza()),
            safeTrim(clientData.getCellulare()),
            safeTrim(clientData.getTelefono()),
            address == null ? null : safeTrim(address.getLuogo()),
            address == null ? null : safeTrim(address.getComune()),
            address == null ? null : safeTrim(address.getProvincia()),
            address == null ? null : safeTrim(address.getNazione()),
            address == null ? null : safeTrim(address.getCap()),
            address == null ? null : safeTrim(address.getCivico())
        );
        }

        private void insertCardData(Long practiceId, BpmPracticeOpenRequest.CardData cardData) {
        String numeroCarta = safeTrim(cardData.getNumeroCarta());
        String tipoCarta = safeTrim(cardData.getTipoCarta());
        String intestatarioCarta = safeTrim(cardData.getIntestatarioCarta());
        jdbcTemplate.update(
            "INSERT INTO card_data (" +
                "practice_id, card_type, iban, pan_masked, note, numero_carta, tipo_carta, intestatario_carta" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            practiceId,
            tipoCarta,
            null,
            maskCardNumber(numeroCarta),
            "Inbound BPM Sprint1",
            numeroCarta,
            tipoCarta,
            intestatarioCarta
        );
        }

        private void ingestAndInsertAttachments(
            Long practiceId,
            String idWorkItem,
            String payloadJson,
            List<BpmPracticeOpenRequest.DocumentData> documents
        ) {
        // GAP-BLOCKER-001: pull-through sincrono BPM->SD->MinIO.
        // Allow-list host, schemi http/https, no redirect cross-host, size cap,
        // validazione Content-Type vs ESTENSIONE, persistenza su MinIO,
        // riga attachment con ingestion_status='AVAILABLE'.
        List<AttachmentRow> rows = new ArrayList<>();
        for (BpmPracticeOpenRequest.DocumentData document : documents) {
            String codiceDocId = String.valueOf(document.getCodiceDocId());
            for (BpmPracticeOpenRequest.DocumentContentData content : document.getContenuti()) {
                String fileName = safeTrim(content.getNomeFile());
                String extension = safeTrim(content.getEstensione());
                String idDoc = safeTrim(content.getIdDoc());
                String linkDownload = safeTrim(content.getLinkDownload());
                rows.add(new AttachmentRow(codiceDocId, fileName, extension, idDoc, linkDownload));
            }
        }

        List<AttachmentIngestionError> errors = new ArrayList<>();
        List<IngestedRow> ingested = new ArrayList<>(rows.size());

        for (AttachmentRow row : rows) {
            try {
                FetchedAttachment fetched = attachmentFetcher.fetch(row.linkDownload, row.extension);
                String key = practiceId + "/" + row.idDoc + "." + row.extension.toLowerCase();
                String storageUri = attachmentStorage.put(key, fetched.bytes(), fetched.mimeType());
                ingested.add(new IngestedRow(row, fetched, storageUri));
            } catch (RuntimeException ex) {
                errors.add(new AttachmentIngestionError(row.idDoc, row.fileName, ex.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            // Anche un solo allegato fallito ⇒ rollback dell'intera apertura pratica.
            throw new AttachmentIngestionException(errors);
        }

        for (IngestedRow ingestedRow : ingested) {
            AttachmentRow row = ingestedRow.row();
            FetchedAttachment fetched = ingestedRow.fetched();
            jdbcTemplate.update(
                "INSERT INTO attachment (" +
                    "practice_id, codice_doc_id, file_name, estensione, id_doc, link_download, mime_type, size_bytes, storage_uri, checksum_sha256, ingestion_status, ingested_at" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'AVAILABLE', CURRENT_TIMESTAMP(3))",
                practiceId,
                row.codiceDocId,
                row.fileName,
                row.extension,
                row.idDoc,
                row.linkDownload,
                fetched.mimeType(),
                fetched.sizeBytes(),
                ingestedRow.storageUri(),
                fetched.sha256Hex()
            );
            insertAuditEvent(practiceId, idWorkItem, payloadJson, "ATTACHMENT_INGESTED");
        }
        }

        private record IngestedRow(AttachmentRow row, FetchedAttachment fetched, String storageUri) {
        }

        private String extractIdWorkItem(JsonNode payloadNode) {
            if (payloadNode == null || !payloadNode.isObject()) {
                return null;
            }
            JsonNode node = payloadNode.get("ID_WORKITEM");
            if (node == null || node.isNull()) {
                return null;
            }
            return safeTrim(node.asText());
        }

        private String buildErrorMessageWithDetails(List<AttachmentIngestionError> errors) {
            String prefix = "Errore acquisizione allegati";
            try {
                String json = objectMapper.writeValueAsString(new ErrorDetails(errors));
                return prefix + " " + json;
            } catch (JsonProcessingException ex) {
                return prefix;
            }
        }

        private String truncate(String value, int max) {
            if (value == null) {
                return null;
            }
            return value.length() <= max ? value : value.substring(0, max);
        }

        private record ErrorDetails(List<AttachmentIngestionError> attachmentErrors) {
        }

        private void insertStateHistory(Long practiceId, String idWorkItem, String toState) {
        jdbcTemplate.update(
            "INSERT INTO practice_state_history (practice_id, from_state, to_state, actor_username, correlation_id, note) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
            practiceId,
            null,
            toState,
            "BPM_SYSTEM",
            idWorkItem,
            "Apertura pratica inbound"
        );
        }

        private void insertAuditEvent(Long practiceId, String idWorkItem, String payloadJson, String eventType) {
        jdbcTemplate.update(
            "INSERT INTO audit_event (actor_username, event_type, practice_id, correlation_id, payload_json) " +
                    "VALUES (?, ?, ?, ?, ?)",
            "BPM_SYSTEM",
            eventType,
            practiceId,
            idWorkItem,
            payloadJson
        );
        }

    private void persistInboundMessage(String idWorkItem, String payloadJson, int resultCode, String resultMessage, Long practiceId) {
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

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String orDefault(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private String normalizeCfCliente(BpmPracticeOpenRequest request) {
        if (request == null) {
            return null;
        }
        String cf = safeTrim(request.getCfCliente());
        if (cf == null && request.getCliente() != null) {
            cf = safeTrim(request.getCliente().getCodiceFiscale());
        }
        if (cf == null) {
            return null;
        }
        return cf.length() > 16 ? cf.substring(0, 16) : cf;
    }

    private Timestamp parseItalianTimestamp(String value) {
        String ts = safeTrim(value);
        if (ts == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(ts, formatter);
            return Timestamp.from(localDateTime.atZone(ZoneId.of("Europe/Rome")).toInstant());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private LocalDate parseBirthDate(String value) {
        String dateValue = safeTrim(value);
        if (dateValue == null) {
            return null;
        }
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateValue, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next format.
            }
        }
        return null;
    }

    private String maskCardNumber(String value) {
        if (value == null || value.length() < 4) {
            return value;
        }
        String last4 = value.substring(value.length() - 4);
        return "**** **** **** " + last4;
    }

    private String mimeTypeFromExtension(String extension) {
        if (extension == null) {
            return "application/octet-stream";
        }
        return switch (extension.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> "application/octet-stream";
        };
    }

    private record AttachmentRow(String codiceDocId, String fileName, String extension, String idDoc, String linkDownload) {
    }
}