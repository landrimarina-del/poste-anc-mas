package it.poste.anc.practice.api;

import java.time.Instant;

public record PracticeHistoryItem(
        Long eventId,
        String eventType,
        String actor,
        String correlationId,
        Instant occurredAt,
        String note
) {
}
