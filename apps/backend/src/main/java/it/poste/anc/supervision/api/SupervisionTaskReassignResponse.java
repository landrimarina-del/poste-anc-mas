package it.poste.anc.supervision.api;

import java.time.Instant;

public record SupervisionTaskReassignResponse(
        Long taskId,
        Long practiceId,
        String assignmentType,
        String taskState,
        String ownerUsername,
        String assignee,
        Instant assignedAt
) {
}
