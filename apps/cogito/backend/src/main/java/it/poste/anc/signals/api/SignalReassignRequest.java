package it.poste.anc.signals.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignalReassignRequest(
        @NotBlank String targetType,
        String username,
        @Size(max = 255) String reason
) {
}
