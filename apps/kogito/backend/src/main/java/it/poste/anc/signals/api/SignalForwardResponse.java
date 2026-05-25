package it.poste.anc.signals.api;

import java.time.Instant;

public record SignalForwardResponse(
        Long signalId,
        String state,
        String sinergiaTicketId,
        Instant forwardedAt,
        Instant closedAt
) {
}
