package it.poste.anc.practice.application;

import it.poste.anc.practice.api.PracticeDetailResponse;
import it.poste.anc.practice.api.PracticeHistoryItem;
import it.poste.anc.practice.api.PracticeListItem;
import it.poste.anc.practice.api.PracticeListPage;
import it.poste.anc.practice.api.PracticeListQuery;
import it.poste.anc.practice.api.PracticeRelatedActionItem;
import it.poste.anc.practice.api.PracticeStateItem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PracticeQueryService {

    private final JdbcTemplate jdbcTemplate;

    public PracticeQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PracticeListPage getPracticeList(PracticeListQuery query) {
        List<String> selectColumns = readPracticeColumns();
        String practiceNumberColumn = pickRequiredColumn(selectColumns, "num_pratica", "request_id");
        String openedAtColumn = pickRequiredColumn(selectColumns, "data_apertura", "data_inserimento_richiesta", "created_at");
        String closedAtColumn = pickOptionalColumn(selectColumns, "data_chiusura", "closed_at");
        String lastModifiedAtColumn = pickOptionalColumn(selectColumns, "ultima_modifica", "updated_at", "last_modified_at");
        String outcomeColumn = pickOptionalColumn(selectColumns, "esito_sd", "outcome_sd", "esito");

        List<Object> params = new ArrayList<>();
        String whereClause = buildWhereClause(query, practiceNumberColumn, openedAtColumn, closedAtColumn, lastModifiedAtColumn,
                outcomeColumn, params);

        Long totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM practice p LEFT JOIN practice_outcome po ON po.practice_id = p.id" + whereClause,
                Long.class,
                params.toArray()
        );
        long total = totalCount == null ? 0L : totalCount;

        String orderBy = resolveSort(query.sort(), practiceNumberColumn, openedAtColumn, closedAtColumn, lastModifiedAtColumn, outcomeColumn);
        int safeSize = normalizeSize(query.size());
        int safePage = normalizePage(query.page());

        List<Object> listParams = new ArrayList<>(params);
        listParams.add(safeSize);
        listParams.add(safePage * safeSize);

        String sql = "SELECT p.id, p.request_id, p.id_work_item, p.stato, "
                + "p." + practiceNumberColumn + " AS practice_number, "
                + "" + resolveSelectTimestamp(openedAtColumn) + " AS opened_at, "
                + "" + resolveSelectTimestamp(closedAtColumn) + " AS closed_at, "
                + "" + resolveSelectTimestamp(lastModifiedAtColumn, openedAtColumn) + " AS last_modified_at, "
                + "po.outcome AS sd_outcome, "
                + "p.cf_cliente AS codice_fiscale, "
                + "p.codice_cliente, "
                + "p.data_inserimento_richiesta, "
                + "(SELECT u.username FROM task t JOIN app_user u ON u.id = t.owner_user_id "
                + " WHERE t.practice_id = p.id ORDER BY t.created_at DESC LIMIT 1) AS operatore, "
                + "(SELECT COUNT(1) FROM signal_case sc WHERE sc.practice_id = p.id) AS segnalazioni_count "
                + "FROM practice p LEFT JOIN practice_outcome po ON po.practice_id = p.id"
                + whereClause
                + " ORDER BY " + orderBy
                + " LIMIT ? OFFSET ?";

        List<PracticeListItem> items = jdbcTemplate.query(sql, (rs, rowNum) -> new PracticeListItem(
                rs.getLong("id"),
                rs.getString("practice_number"),
                rs.getString("request_id"),
                rs.getString("id_work_item"),
                rs.getString("stato"),
                rs.getString("sd_outcome"),
                toInstant(rs.getTimestamp("opened_at")),
                toInstant(rs.getTimestamp("closed_at")),
                toInstant(rs.getTimestamp("last_modified_at")),
                rs.getString("codice_fiscale"),
                rs.getString("codice_cliente"),
                toInstant(rs.getTimestamp("data_inserimento_richiesta")),
                rs.getString("operatore"),
                rs.getInt("segnalazioni_count")
        ), listParams.toArray());

        return new PracticeListPage(items, total, safePage, safeSize);
    }

    public Optional<PracticeDetailResponse> getPracticeDetail(Long practiceId) {
        List<String> selectColumns = readPracticeColumns();
        String practiceNumberColumn = pickRequiredColumn(selectColumns, "num_pratica", "request_id");
        String openedAtColumn = pickRequiredColumn(selectColumns, "data_apertura", "data_inserimento_richiesta", "created_at");
        String closedAtColumn = pickOptionalColumn(selectColumns, "data_chiusura", "closed_at");
        String lastModifiedAtColumn = pickOptionalColumn(selectColumns, "ultima_modifica", "updated_at", "last_modified_at");
        String outcomeColumn = pickOptionalColumn(selectColumns, "esito_sd", "outcome_sd", "esito");

        String sql = "SELECT p.id, p.request_id, p.id_work_item, p.stato, p.codice_cliente, p.document_type, "
                + "p." + practiceNumberColumn + " AS practice_number, "
                + "" + resolveSelectTimestamp(openedAtColumn) + " AS opened_at, "
                + "" + resolveSelectTimestamp(closedAtColumn) + " AS closed_at, "
                + "" + resolveSelectTimestamp(lastModifiedAtColumn, openedAtColumn) + " AS last_modified_at, "
                + "po.outcome AS sd_outcome, po.ko_codes_json, "
            + "c.nome, c.cognome, c.codice_fiscale, c.sesso, c.data_nascita, c.comune_nascita, "
            + "c.provincia_nascita, c.nazione_nascita, c.telefono, c.cellulare, "
            + "c.residenza_luogo, c.residenza_comune, c.residenza_provincia, c.residenza_nazione, "
            + "c.residenza_cap, c.residenza_civico, "
                + "cd.pan_masked, cd.card_type, cd.intestatario_carta "
                + "FROM practice p "
                + "LEFT JOIN practice_outcome po ON po.practice_id = p.id "
                + "LEFT JOIN client_data c ON c.practice_id = p.id "
                + "LEFT JOIN card_data cd ON cd.practice_id = p.id "
                + "WHERE p.id = ?";

        List<PracticeDetailResponse> details = jdbcTemplate.query(sql, (rs, rowNum) -> {
            PracticeDetailResponse.Header header = new PracticeDetailResponse.Header(
                    rs.getLong("id"),
                    rs.getString("practice_number"),
                    rs.getString("request_id"),
                    rs.getString("id_work_item"),
                    rs.getString("stato"),
                    rs.getString("sd_outcome"),
                    toInstant(rs.getTimestamp("opened_at")),
                    toInstant(rs.getTimestamp("closed_at")),
                    toInstant(rs.getTimestamp("last_modified_at")),
                    rs.getString("document_type"),
                    rs.getString("ko_codes_json")
            );

            PracticeDetailResponse.Client client = new PracticeDetailResponse.Client(
                    rs.getString("nome"),
                    rs.getString("cognome"),
                    rs.getString("codice_fiscale"),
                    rs.getString("codice_cliente"),
                    rs.getString("sesso"),
                    rs.getObject("data_nascita", LocalDate.class),
                    rs.getString("comune_nascita"),
                    rs.getString("provincia_nascita"),
                    rs.getString("nazione_nascita"),
                    rs.getString("telefono"),
                    rs.getString("cellulare"),
                    new PracticeDetailResponse.ResidenceAddress(
                        rs.getString("residenza_luogo"),
                        rs.getString("residenza_comune"),
                        rs.getString("residenza_provincia"),
                        rs.getString("residenza_nazione"),
                        rs.getString("residenza_cap"),
                        rs.getString("residenza_civico")
                    )
            );

            PracticeDetailResponse.BlockedCard blockedCard = new PracticeDetailResponse.BlockedCard(
                    rs.getString("pan_masked"),
                    rs.getString("card_type"),
                    rs.getString("intestatario_carta")
            );

            return new PracticeDetailResponse(header, client, blockedCard, computeFase(rs.getString("stato")));
        }, practiceId);

        return details.stream().findFirst();
    }

    private static final String HISTORY_EVENT_TYPES =
            "'PRACTICE_OPENED','TASK_ACCEPTED','TASK_REASSIGNED','DOCUMENT_TYPED',"
            + "'CHECKLIST_SAVED','CHECKLIST_REOPENED','PRACTICE_CLOSED_SD',"
            + "'PRACTICE_FINALIZED','PRACTICE_CLOSE_REQUESTED','BPM_OUTCOME_ACK_RECEIVED'";

    public List<PracticeHistoryItem> getPracticeHistory(Long practiceId) {
        List<PracticeHistoryItem> historyItems = new ArrayList<>();

        String auditSql = "SELECT MIN(id) AS id, event_type, actor_username, "
                + "MIN(correlation_id) AS correlation_id, occurred_at, "
                + "MIN(JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.note'))) AS note "
                + "FROM audit_event WHERE practice_id = ? "
                + "AND event_type IN (" + HISTORY_EVENT_TYPES + ") "
                + "GROUP BY event_type, actor_username, occurred_at";
        historyItems.addAll(jdbcTemplate.query(
            auditSql,
            (rs, rowNum) -> new PracticeHistoryItem(
                rs.getLong("id"),
                rs.getString("event_type"),
                rs.getString("actor_username"),
                rs.getString("correlation_id"),
                toInstant(rs.getTimestamp("occurred_at")),
                rs.getString("note")
            ),
            practiceId
        ));

        historyItems.sort(
            Comparator.comparing(PracticeHistoryItem::occurredAt,
                    Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(PracticeHistoryItem::eventId, Comparator.nullsLast(Comparator.reverseOrder()))
        );
        return historyItems;
    }

    public List<PracticeStateItem> getPracticeStates(Long practiceId) {
        return jdbcTemplate.query(
            "SELECT id, from_state, to_state, actor_username, correlation_id, note, occurred_at AS changed_at "
                + "FROM practice_state_history WHERE practice_id = ? ORDER BY occurred_at DESC, id DESC",
                (rs, rowNum) -> new PracticeStateItem(
                        rs.getLong("id"),
                        rs.getString("from_state"),
                        rs.getString("to_state"),
                        rs.getString("actor_username"),
                        rs.getString("correlation_id"),
                        rs.getString("note"),
                        toInstant(rs.getTimestamp("changed_at"))
                ),
                practiceId
        );
    }

    public boolean practiceExists(Long practiceId) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM practice WHERE id = ?", Long.class, practiceId);
        return count != null && count > 0;
    }

    public List<PracticeRelatedActionItem> getRelatedActions(Long practiceId) {
        if (tableExists("related_action")) {
            List<PracticeRelatedActionItem> staticActions = jdbcTemplate.query(
                    "SELECT id, action_code, action_label, target_url FROM related_action WHERE practice_id = ? ORDER BY id ASC",
                    (rs, rowNum) -> new PracticeRelatedActionItem(
                            rs.getLong("id"),
                            rs.getString("action_code"),
                            rs.getString("action_label"),
                            rs.getString("target_url")
                    ),
                    practiceId
            );
            if (!staticActions.isEmpty()) {
                return staticActions;
            }
        }

        if (!tableExists("signal_case")) {
            return List.of();
        }

        return jdbcTemplate.query(
                "SELECT id, stato FROM signal_case WHERE practice_id = ? ORDER BY updated_at DESC, id DESC",
                (rs, rowNum) -> new PracticeRelatedActionItem(
                        rs.getLong("id"),
                        "SIGNAL_DETAIL",
                        "Segnalazione #" + rs.getLong("id") + " (" + rs.getString("stato") + ")",
                        "/signals/" + rs.getLong("id")
                ),
                practiceId
        );
    }

    public byte[] exportPracticeListExcel(PracticeListQuery query) {
        int exportSize = Math.min(Math.max(query.size(), 1), 2000);
        PracticeListQuery exportQuery = new PracticeListQuery(
                Math.max(query.page(), 0),
                exportSize,
                query.sort(),
                query.practiceNumber(),
                query.state(),
                query.openedFrom(),
                query.openedTo(),
                query.closedFrom(),
                query.closedTo(),
                query.lastModifiedFrom(),
                query.lastModifiedTo(),
                query.sdOutcome()
        );

        PracticeListPage page = getPracticeList(exportQuery);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.of("UTC"));

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Pratiche ANC");
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Practice ID", "Numero Pratica", "Request ID", "Work Item", "Stato", "Esito SD",
                    "Data Apertura", "Data Chiusura", "Ultima Modifica"
            };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (PracticeListItem item : page.items()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.practiceId() == null ? "" : String.valueOf(item.practiceId()));
                row.createCell(1).setCellValue(toCell(item.practiceNumber()));
                row.createCell(2).setCellValue(toCell(item.requestId()));
                row.createCell(3).setCellValue(toCell(item.idWorkItem()));
                row.createCell(4).setCellValue(toCell(item.state()));
                row.createCell(5).setCellValue(toCell(item.sdOutcome()));
                row.createCell(6).setCellValue(formatInstant(item.openedAt(), formatter));
                row.createCell(7).setCellValue(formatInstant(item.closedAt(), formatter));
                row.createCell(8).setCellValue(formatInstant(item.lastModifiedAt(), formatter));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Errore generazione workbook Excel", ex);
        }
    }

    private String buildWhereClause(
            PracticeListQuery query,
            String practiceNumberColumn,
            String openedAtColumn,
            String closedAtColumn,
            String lastModifiedAtColumn,
            String outcomeColumn,
            List<Object> params
    ) {
        List<String> conditions = new ArrayList<>();

        if (notBlank(query.practiceNumber())) {
            conditions.add("UPPER(p." + practiceNumberColumn + ") LIKE ?");
            params.add("%" + query.practiceNumber().trim().toUpperCase(Locale.ROOT) + "%");
        }
        if (notBlank(query.state())) {
            conditions.add("UPPER(p.stato) = ?");
            params.add(query.state().trim().toUpperCase(Locale.ROOT));
        }
        if (notBlank(query.sdOutcome())) {
            String normalizedOutcome = query.sdOutcome().trim().toUpperCase(Locale.ROOT);
            if ("OK".equals(normalizedOutcome) || "APPROVATA".equals(normalizedOutcome)) {
                conditions.add("UPPER(po.outcome) IN ('OK', 'APPROVATA')");
            } else if ("KO".equals(normalizedOutcome) || "RESPINTA".equals(normalizedOutcome) || "NOK".equals(normalizedOutcome)) {
                conditions.add("UPPER(po.outcome) IN ('KO', 'RESPINTA')");
            } else {
                conditions.add("UPPER(po.outcome) = ?");
                params.add(normalizedOutcome);
            }
        }

        appendDateRange(conditions, params, "p." + openedAtColumn, query.openedFrom(), query.openedTo());
        if (closedAtColumn != null) {
            appendDateRange(conditions, params, "p." + closedAtColumn, query.closedFrom(), query.closedTo());
        }
        if (lastModifiedAtColumn != null) {
            appendDateRange(conditions, params, "p." + lastModifiedAtColumn, query.lastModifiedFrom(), query.lastModifiedTo());
        }

        if (conditions.isEmpty()) {
            return "";
        }
        return " WHERE " + String.join(" AND ", conditions);
    }

    private String composeStateTransitionNote(String fromState, String toState, String note) {
        String base = (fromState == null ? "?" : fromState) + " -> " + (toState == null ? "?" : toState);
        if (!notBlank(note)) {
            return base;
        }
        return base + " | " + note;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private String toCell(String value) {
        return value == null ? "" : value;
    }

    private String formatInstant(Instant value, DateTimeFormatter formatter) {
        if (value == null) {
            return "";
        }
        return formatter.format(value);
    }

    private void appendDateRange(List<String> conditions, List<Object> params, String column, String from, String to) {
        Timestamp fromTs = parseDateStart(from);
        Timestamp toTs = parseDateEnd(to);

        if (fromTs != null) {
            conditions.add(column + " >= ?");
            params.add(fromTs);
        }
        if (toTs != null) {
            conditions.add(column + " <= ?");
            params.add(toTs);
        }
    }

    private String resolveSort(
            String sort,
            String practiceNumberColumn,
            String openedAtColumn,
            String closedAtColumn,
            String lastModifiedAtColumn,
            String outcomeColumn
    ) {
        if (!notBlank(sort)) {
            return "p." + openedAtColumn + " DESC, p.id DESC";
        }

        String[] parts = sort.split(",", 2);
        String requestedField = parts[0].trim().toLowerCase(Locale.ROOT);
        String direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())) ? "ASC" : "DESC";

        String resolvedColumn = switch (requestedField) {
            case "practicenumber", "practice_number", "numpratica", "requestid", "request_id" -> "p." + practiceNumberColumn;
            case "state", "stato" -> "p.stato";
            case "openedat", "opened_at", "dataapertura", "data_apertura" -> "p." + openedAtColumn;
            case "closedat", "closed_at", "datachiusura", "data_chiusura" -> closedAtColumn == null ? "p." + openedAtColumn : "p." + closedAtColumn;
            case "lastmodifiedat", "last_modified_at", "ultimamodifica", "ultima_modifica" ->
                    lastModifiedAtColumn == null ? "p." + openedAtColumn : "p." + lastModifiedAtColumn;
            case "sdoutcome", "sd_outcome", "esitosd", "esito_sd" -> "po.outcome";
            default -> "p." + openedAtColumn;
        };

        return resolvedColumn + " " + direction + ", p.id DESC";
    }

    private int normalizeSize(int requestedSize) {
        if (requestedSize <= 0) {
            return 20;
        }
        return Math.min(requestedSize, 100);
    }

    private int normalizePage(int requestedPage) {
        return Math.max(requestedPage, 0);
    }

    private List<String> readPracticeColumns() {
        return jdbcTemplate.query("SELECT * FROM practice WHERE 1 = 0", rs -> {
            ResultSetMetaData md = rs.getMetaData();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                columns.add(md.getColumnName(i).toLowerCase(Locale.ROOT));
            }
            return columns;
        });
    }

    private String pickOptionalColumn(List<String> availableColumns, String... candidates) {
        for (String candidate : candidates) {
            if (availableColumns.contains(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
        return null;
    }

    private String pickRequiredColumn(List<String> availableColumns, String... candidates) {
        String column = pickOptionalColumn(availableColumns, candidates);
        if (column != null) {
            return column;
        }
        throw new IllegalStateException("Colonna obbligatoria non trovata nella tabella practice");
    }

    private String resolveSelectTimestamp(String preferredColumn) {
        if (preferredColumn == null) {
            return "NULL";
        }
        return "p." + preferredColumn;
    }

    private String resolveSelectTimestamp(String preferredColumn, String fallbackColumn) {
        if (preferredColumn != null) {
            return "p." + preferredColumn;
        }
        if (fallbackColumn != null) {
            return "p." + fallbackColumn;
        }
        return "NULL";
    }

    private String resolveSelectString(String preferredColumn) {
        if (preferredColumn == null) {
            return "NULL";
        }
        return "p." + preferredColumn;
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Timestamp parseDateStart(String value) {
        LocalDate date = parseDate(value);
        if (date == null) {
            return null;
        }
        return Timestamp.valueOf(date.atStartOfDay());
    }

    private Timestamp parseDateEnd(String value) {
        LocalDate date = parseDate(value);
        if (date == null) {
            return null;
        }
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);
        return Timestamp.valueOf(endOfDay);
    }

    private LocalDate parseDate(String value) {
        if (!notBlank(value)) {
            return null;
        }
        String clean = value.trim();
        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );
        for (DateTimeFormatter formatter : formats) {
            try {
                return LocalDate.parse(clean, formatter);
            } catch (DateTimeParseException ignored) {
                // Ignora formato non valido in ottica fallback read-only.
            }
        }
        return null;
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    /**
     * Deriva la fase operativa dalla pratica.
     * GAP-US-06 — GAP_Architettura.md §GAP-US-06
     */
    private String computeFase(String stato) {
        if (stato == null) {
            return "RACCOLTA_INPUT";
        }
        return switch (stato) {
            case "IN_LAVORAZIONE" -> "LAVORAZIONE";
            case "CHIUSA_SD_OK", "CHIUSA_SD_KO", "CHIUSA_EXT_OK", "CHIUSA_EXT_KO" -> "CHIUSURA_PRATICA";
            default -> "RACCOLTA_INPUT";
        };
    }
}