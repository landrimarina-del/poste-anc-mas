package it.poste.anc.signals.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignalCreateRequest(
        @NotNull Long practiceId,
        @NotBlank @Size(max = 120) String subject,
        @NotBlank @Size(max = 2000) String description
) {
}
