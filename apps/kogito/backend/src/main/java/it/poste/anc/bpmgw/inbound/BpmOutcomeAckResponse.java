package it.poste.anc.bpmgw.inbound;

import java.time.Instant;

public record BpmOutcomeAckResponse(
        Long practiceId,
        String requestId,
        String finalState,
        Instant closedAt,
        boolean idempotent
) {
}
