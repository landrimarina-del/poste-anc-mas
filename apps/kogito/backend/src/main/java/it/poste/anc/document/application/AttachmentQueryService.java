package it.poste.anc.document.application;

import it.poste.anc.document.api.AttachmentListItem;
import it.poste.anc.document.ingestion.AttachmentStorage;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AttachmentQueryService {

    private static final String TECHNICAL_ATTACHMENT_ERROR = "Errore tecnico nel recupero allegato. Usa il download fallback e prosegui con la tipizzazione.";
    private static final String UNAVAILABLE_ATTACHMENT_ERROR = "Allegato non disponibile";
    private static final String S3_PREFIX = "s3://";

    private final JdbcTemplate jdbcTemplate;
    private final AttachmentStorage attachmentStorage;

    public AttachmentQueryService(JdbcTemplate jdbcTemplate, AttachmentStorage attachmentStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.attachmentStorage = attachmentStorage;
    }

    public List<AttachmentListItem> listPracticeAttachments(Long practiceId) {
        ensurePracticeExists(practiceId);

        List<String> columns = readAttachmentColumns();
        String fileNameColumn = pickRequiredColumn(columns, "file_name", "nome_file");
        String extensionColumn = pickOptionalColumn(columns, "estensione", "extension");
        String mimeTypeColumn = pickOptionalColumn(columns, "mime_type");
        String sizeColumn = pickOptionalColumn(columns, "size_bytes", "file_size");

        String sql = "SELECT a.id, a." + fileNameColumn + " AS file_name, "
                + selectStringColumn("a", extensionColumn) + " AS extension, "
                + selectStringColumn("a", mimeTypeColumn) + " AS mime_type, "
                + selectNumericColumn("a", sizeColumn) + " AS size_bytes "
                + "FROM attachment a WHERE a.practice_id = ? ORDER BY a.id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new AttachmentListItem(
                rs.getLong("id"),
                rs.getString("file_name"),
                rs.getString("extension"),
                rs.getString("mime_type"),
                rs.getObject("size_bytes") == null ? null : rs.getLong("size_bytes")
        ), practiceId);
    }

    public AttachmentReadResult readAttachmentForPreview(Long attachmentId) {
        return readAttachment(attachmentId);
    }

    public AttachmentReadResult readAttachmentForDownload(Long attachmentId) {
        return readAttachment(attachmentId);
    }

    private AttachmentReadResult readAttachment(Long attachmentId) {
        // GAP-BLOCKER-001: lettura SOLO da MinIO (storage_uri = s3://...), no piu' fallback redirect.
        List<String> columns = readAttachmentColumns();
        String fileNameColumn = pickRequiredColumn(columns, "file_name", "nome_file");
        String mimeTypeColumn = pickOptionalColumn(columns, "mime_type");
        String storageUriColumn = pickOptionalColumn(columns, "storage_uri");
        String ingestionStatusColumn = pickOptionalColumn(columns, "ingestion_status");

        String sql = "SELECT a.id, a." + fileNameColumn + " AS file_name, "
                + selectStringColumn("a", mimeTypeColumn) + " AS mime_type, "
                + selectStringColumn("a", storageUriColumn) + " AS storage_uri, "
                + selectStringColumn("a", ingestionStatusColumn) + " AS ingestion_status "
                + "FROM attachment a WHERE a.id = ?";

        List<AttachmentRow> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new AttachmentRow(
                rs.getString("file_name"),
                rs.getString("mime_type"),
                rs.getString("storage_uri"),
                rs.getString("ingestion_status")
        ), attachmentId);

        if (rows.isEmpty()) {
            throw new DocumentOperationException(HttpStatus.NOT_FOUND, 4004, TECHNICAL_ATTACHMENT_ERROR);
        }

        AttachmentRow row = rows.get(0);
        if (row.ingestionStatus() != null && !"AVAILABLE".equalsIgnoreCase(row.ingestionStatus())) {
            throw new DocumentOperationException(HttpStatus.NOT_FOUND, 4004, UNAVAILABLE_ATTACHMENT_ERROR);
        }

        String storageUri = row.storageUri();
        if (storageUri == null || !storageUri.startsWith(S3_PREFIX)) {
            throw new DocumentOperationException(HttpStatus.NOT_FOUND, 4004, UNAVAILABLE_ATTACHMENT_ERROR);
        }

        byte[] content;
        try {
            content = attachmentStorage.getBytes(storageUri);
        } catch (RuntimeException ex) {
            throw new DocumentOperationException(HttpStatus.NOT_FOUND, 4004, TECHNICAL_ATTACHMENT_ERROR);
        }

        if (content == null || content.length == 0) {
            throw new DocumentOperationException(HttpStatus.NOT_FOUND, 4004, UNAVAILABLE_ATTACHMENT_ERROR);
        }

        return new AttachmentReadResult(row.fileName(), row.mimeType(), content, null);
    }

    private record AttachmentRow(String fileName, String mimeType, String storageUri, String ingestionStatus) {
    }

    private void ensurePracticeExists(Long practiceId) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM practice WHERE id = ?", Long.class, practiceId);
        if (count == null || count == 0) {
            throw new DocumentOperationException(HttpStatus.NOT_FOUND, 2004, "Pratica non trovata");
        }
    }

    private List<String> readAttachmentColumns() {
        return jdbcTemplate.query("SELECT * FROM attachment WHERE 1 = 0", rs -> {
            ResultSetMetaData md = rs.getMetaData();
            List<String> dbColumns = new ArrayList<>();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                dbColumns.add(md.getColumnName(i).toLowerCase(Locale.ROOT));
            }
            return dbColumns;
        });
    }

    private String pickRequiredColumn(List<String> availableColumns, String... candidates) {
        String column = pickOptionalColumn(availableColumns, candidates);
        if (column == null) {
            throw new IllegalStateException("Colonna attachment obbligatoria non trovata");
        }
        return column;
    }

    private String pickOptionalColumn(List<String> availableColumns, String... candidates) {
        for (String candidate : candidates) {
            if (availableColumns.contains(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
        return null;
    }

    private String selectStringColumn(String alias, String column) {
        if (column == null) {
            return "NULL";
        }
        return alias + "." + column;
    }

    private String selectNumericColumn(String alias, String column) {
        if (column == null) {
            return "NULL";
        }
        return alias + "." + column;
    }
}
