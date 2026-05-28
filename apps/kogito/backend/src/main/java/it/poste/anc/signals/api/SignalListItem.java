package it.poste.anc.signals.api;

import java.time.Instant;

public record SignalListItem(
        Long signalId,
        Long practiceId,
        String practiceNumber,
        String state,
        String ownerUsername,
        String createdByUsername,
        String subject,
        String sinergiaTicketId,
        Instant createdAt,
        Instant updatedAt,
        String activityLabel,
        Instant acceptedAt,
        String groupName
) {
}
