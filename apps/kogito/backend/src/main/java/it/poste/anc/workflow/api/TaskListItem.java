package it.poste.anc.workflow.api;

import java.time.Instant;

public record TaskListItem(
        Long taskId,
        Long practiceId,
        String practiceNumber,
        String requestId,
        String idWorkItem,
        String taskState,
        String practiceState,
        String ownerUsername,
        Instant createdAt,
        Instant acceptedAt,
        Instant slaDueDate,
        String slaStatus,
        String activityLabel,
        String candidateGroup
) {
}
