package it.poste.anc.workflow.api;

public record TaskAcceptResponse(
        Long taskId,
        Long practiceId,
        String practiceState,
        String ownerUsername
) {
}
