package it.poste.anc.supervision.api;

import java.time.Instant;

public record SupervisionTaskListItem(
        Long taskId,
        Long practiceId,
        String practiceNumber,
        String taskState,
        String practiceState,
        String ownerUsername,
        String groupName,
        Instant assignmentDate,
        String activityLabel,
        Instant acceptedAt
) {
}
