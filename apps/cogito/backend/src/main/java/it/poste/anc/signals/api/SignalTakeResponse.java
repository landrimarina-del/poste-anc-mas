package it.poste.anc.signals.api;

import java.time.Instant;

public record SignalTakeResponse(
        Long signalId,
        Long practiceId,
        String state,
        String ownerUsername,
        Instant takenAt
) {
}
