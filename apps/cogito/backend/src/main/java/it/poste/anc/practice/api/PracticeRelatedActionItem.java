package it.poste.anc.practice.api;

public record PracticeRelatedActionItem(
        Long actionId,
        String actionCode,
        String actionLabel,
        String targetUrl
) {
}
