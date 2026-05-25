package it.poste.anc.supervision.api;

public record SupervisionDailyWorkedPoint(
        int day,
        long okPractices,
        long koPractices
) {
}
