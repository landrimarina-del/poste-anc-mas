package it.poste.anc.signals.api;

import java.time.Instant;

public record SignalReassignResponse(
        Long signalId,
        String state,
        String ownerUsername,
        String candidateGroupCode,
        Instant reassignedAt
) {
}
