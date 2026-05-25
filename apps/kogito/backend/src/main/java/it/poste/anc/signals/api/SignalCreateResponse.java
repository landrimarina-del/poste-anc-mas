package it.poste.anc.signals.api;

import java.time.Instant;

public record SignalCreateResponse(
        Long signalId,
        Long practiceId,
        String state,
        String ownerUsername,
        Instant createdAt
) {
}
