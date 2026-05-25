package it.poste.anc.practice.api;

import java.time.Instant;

public record PracticeStateItem(
        Long transitionId,
        String fromState,
        String toState,
        String actor,
        String correlationId,
        String note,
        Instant transitionedAt
) {
}
