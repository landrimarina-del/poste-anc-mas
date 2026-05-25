package it.poste.anc.document.api;

public record IntakeTypingResponse(
        Long practiceId,
        String documentType,
        boolean alreadyConfirmed
) {
}
