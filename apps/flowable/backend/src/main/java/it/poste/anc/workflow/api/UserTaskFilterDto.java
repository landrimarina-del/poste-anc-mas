package it.poste.anc.workflow.api;

public record UserTaskFilterDto(
        Long id,
        String filterName,
        String filterJson,
        String createdAt
) {
}
