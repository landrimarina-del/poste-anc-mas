package it.poste.anc.practice.api;

public record PracticeListQuery(
        int page,
        int size,
        String sort,
        String practiceNumber,
        String state,
        String openedFrom,
        String openedTo,
        String closedFrom,
        String closedTo,
        String lastModifiedFrom,
        String lastModifiedTo,
        String sdOutcome
) {
}
