package it.poste.anc.document.ingestion;

/**
 * Errore strutturato di acquisizione di un singolo allegato BPM
 * (GAP-BLOCKER-001). Esposto a valle nel campo details / message
 * della ApiResponse di errore -4.
 */
public record AttachmentIngestionError(
        String idDoc,
        String fileName,
        String reason
) {
}
