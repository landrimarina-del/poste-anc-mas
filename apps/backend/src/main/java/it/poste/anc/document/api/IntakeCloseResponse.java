package it.poste.anc.document.api;

public record IntakeCloseResponse(
        Long practiceId,
        String practiceState,
        String correlationId
) {
}
