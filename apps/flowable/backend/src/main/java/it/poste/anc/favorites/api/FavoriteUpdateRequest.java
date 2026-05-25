package it.poste.anc.favorites.api;

public record FavoriteUpdateRequest(
        String titolo,
        String url,
        String tipo
) {
}
