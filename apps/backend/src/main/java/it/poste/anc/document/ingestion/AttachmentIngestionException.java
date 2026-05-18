package it.poste.anc.document.ingestion;

import java.util.List;

/**
 * Eccezione di acquisizione allegati (GAP-BLOCKER-001).
 * Lanciata da BpmPracticeInboundService.openPractice quando anche un solo
 * allegato fallisce il pull/validazione/storage: provoca rollback della
 * transazione di apertura pratica e mappatura a resultCode = -4.
 */
public class AttachmentIngestionException extends RuntimeException {

    private final List<AttachmentIngestionError> errors;

    public AttachmentIngestionException(List<AttachmentIngestionError> errors) {
        super(buildMessage(errors));
        this.errors = List.copyOf(errors);
    }

    public List<AttachmentIngestionError> getErrors() {
        return errors;
    }

    private static String buildMessage(List<AttachmentIngestionError> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Errore acquisizione allegati";
        }
        StringBuilder sb = new StringBuilder("Errore acquisizione allegati: ");
        for (int i = 0; i < errors.size(); i++) {
            AttachmentIngestionError e = errors.get(i);
            if (i > 0) {
                sb.append("; ");
            }
            sb.append("idDoc=").append(e.idDoc())
              .append(" file=").append(e.fileName())
              .append(" reason=").append(e.reason());
        }
        return sb.toString();
    }
}
