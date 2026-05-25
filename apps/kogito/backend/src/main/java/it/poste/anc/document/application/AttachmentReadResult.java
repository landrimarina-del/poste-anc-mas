package it.poste.anc.document.application;

public record AttachmentReadResult(
        String fileName,
        String mimeType,
        byte[] content,
        String redirectUrl
) {

    public boolean hasInlineContent() {
        return content != null && content.length > 0;
    }

    public boolean hasRedirectUrl() {
        return redirectUrl != null && !redirectUrl.isBlank();
    }
}
