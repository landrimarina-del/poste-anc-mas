package it.poste.anc.document.ingestion;

/**
 * Risultato del fetch sincrono di un allegato BPM (GAP-BLOCKER-001).
 * Contiene il binario gia' validato (Content-Type vs estensione, size cap)
 * pronto per essere persistito su MinIO.
 */
public record FetchedAttachment(
        byte[] bytes,
        String mimeType,
        long sizeBytes,
        String sha256Hex
) {
}
