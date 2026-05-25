package it.poste.anc.document.api;

import jakarta.validation.constraints.NotBlank;

public record IntakeTypingRequest(
        @NotBlank(message = "documentType obbligatorio")
        String documentType
) {
}
