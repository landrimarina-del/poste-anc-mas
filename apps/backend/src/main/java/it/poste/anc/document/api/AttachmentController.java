package it.poste.anc.document.api;

import it.poste.anc.document.application.AttachmentQueryService;
import it.poste.anc.document.application.AttachmentReadResult;
import it.poste.anc.document.application.DocumentOperationException;
import it.poste.anc.shared.common.ApiResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class AttachmentController {

    private final AttachmentQueryService attachmentQueryService;

    public AttachmentController(AttachmentQueryService attachmentQueryService) {
        this.attachmentQueryService = attachmentQueryService;
    }

    @GetMapping(path = "/api/v1/practices/{id}/attachments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<AttachmentListItem>>> listPracticeAttachments(@PathVariable("id") Long practiceId) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(attachmentQueryService.listPracticeAttachments(practiceId)));
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @GetMapping(path = "/api/v1/attachments/{id}/preview")
    public ResponseEntity<?> previewAttachment(@PathVariable("id") Long attachmentId) {
        try {
            AttachmentReadResult result = attachmentQueryService.readAttachmentForPreview(attachmentId);
            return buildAttachmentResponse(result, false);
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @GetMapping(path = "/api/v1/attachments/{id}/download")
    public ResponseEntity<?> downloadAttachment(@PathVariable("id") Long attachmentId) {
        try {
            AttachmentReadResult result = attachmentQueryService.readAttachmentForDownload(attachmentId);
            return buildAttachmentResponse(result, true);
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    private ResponseEntity<?> buildAttachmentResponse(AttachmentReadResult result, boolean attachmentDisposition) {
        if (result.hasInlineContent()) {
            MediaType contentType = safeMediaType(result.mimeType());
            ContentDisposition disposition = attachmentDisposition
                    ? ContentDisposition.attachment().filename(result.fileName(), StandardCharsets.UTF_8).build()
                    : ContentDisposition.inline().filename(result.fileName(), StandardCharsets.UTF_8).build();

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body(result.content());
        }

        // GAP-BLOCKER-001: niente piu' redirect 302 verso LINKDOWNLOAD.
        // Se non c'e' contenuto binario su MinIO, l'allegato e' indisponibile.
        return ResponseEntity.status(404)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(4004, "Allegato non disponibile"));
    }

    private MediaType safeMediaType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(mimeType);
        } catch (IllegalArgumentException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
