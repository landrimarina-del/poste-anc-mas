package it.poste.anc.workflow.api;

import java.time.Instant;
import java.util.List;

/**
 * DTO risposta per GET /tasks/{id}.
 * Sprint 12 — GAP-US-03 (intakeStep) e GAP-US-04 (sidebarState).
 */
public record TaskDetailResponse(
        Long taskId,
        Long practiceId,
        String practiceNumber,
        String requestId,
        String idWorkItem,
        String taskState,
        String practiceState,
        String ownerUsername,
        Instant createdAt,
        Instant acceptedAt,
        String intakeStep,
        String documentType,
        Instant slaDueDate,
        String slaStatus,
        SidebarState sidebarState
) {

    /**
     * Stato della sidebar di navigazione lavorazione (3 step fissi).
     */
    public record SidebarState(
            String currentStep,
            List<StepInfo> steps
    ) {
    }

    /**
     * Singolo step della sidebar.
     */
    public record StepInfo(
            String id,
            String label,
            boolean enabled,
            boolean completed
    ) {
    }
}
