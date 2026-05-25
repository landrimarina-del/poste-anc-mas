package it.poste.anc.supervision.api;

import jakarta.validation.constraints.NotBlank;

public record SupervisionTaskReassignUserRequest(@NotBlank String username, String reason) {
}
