package it.poste.anc.bpmgw.inbound;

import java.util.List;

public record BpmOutcomeAckRequest(
        String correlationId,
        String requestId,
        Long practiceId,
        String outcome,
        List<String> koCodes
) {
}
