package it.poste.anc.practice.api;

import java.time.Instant;

public record PracticeDetailResponse(
        Header header,
        Client client,
        BlockedCard blockedCard
) {
    public record Header(
            Long practiceId,
            String practiceNumber,
            String requestId,
            String idWorkItem,
            String state,
            String sdOutcome,
            Instant openedAt,
            Instant closedAt,
            Instant lastModifiedAt
    ) {
    }

    public record Client(
            String firstName,
            String lastName,
            String fiscalCode,
            String customerCode
    ) {
    }

    public record BlockedCard(
            String cardNumberMasked,
            String cardType,
            String cardHolder
    ) {
    }
}
