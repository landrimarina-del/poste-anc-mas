package it.poste.anc.practice.api;

import java.time.Instant;

public record PracticeListItem(
        Long practiceId,
        String practiceNumber,
        String requestId,
        String idWorkItem,
        String state,
        String sdOutcome,
        Instant openedAt,
        Instant closedAt,
        Instant lastModifiedAt,
        String codiceFiscale,
        String codiceCliente,
        Instant dataInserimentoRichiesta,
        String operatore,
        Integer segnalazioniCount
) {
}