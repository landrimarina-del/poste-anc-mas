package it.poste.anc.document.api;

public record IntakeChecklistHelpResponse(
        Long practiceId,
        String documentType,
        String itemId,
        String title,
        String description
) {
}
