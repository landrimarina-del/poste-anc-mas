package it.poste.anc.document.api;

public record AttachmentListItem(
        Long attachmentId,
        String fileName,
        String extension,
        String mimeType,
        Long sizeBytes
) {
}
