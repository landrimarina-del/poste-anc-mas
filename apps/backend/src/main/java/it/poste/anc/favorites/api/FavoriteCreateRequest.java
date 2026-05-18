package it.poste.anc.favorites.api;

public record FavoriteCreateRequest(
        String titolo,
        String url,
        String tipo
) {
}
