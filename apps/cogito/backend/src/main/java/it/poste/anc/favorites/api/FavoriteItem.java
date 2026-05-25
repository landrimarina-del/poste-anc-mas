package it.poste.anc.favorites.api;

import java.time.Instant;

public record FavoriteItem(
        Long id,
        String titolo,
        String url,
        FavoriteType tipo,
        Instant createdAt,
        Instant updatedAt
) {
}
