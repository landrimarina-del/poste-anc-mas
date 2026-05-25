package it.poste.anc.workflow.api;

public record UserTaskFilterRequest(
        String filterName,
        String filterJson
) {
}
