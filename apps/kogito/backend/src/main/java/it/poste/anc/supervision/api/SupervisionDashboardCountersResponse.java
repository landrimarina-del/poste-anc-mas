package it.poste.anc.supervision.api;

public record SupervisionDashboardCountersResponse(
        long activities,
        long activePractices,
        long closedPractices
) {
}
