package it.poste.anc.favorites.application;

import it.poste.anc.favorites.api.FavoriteCreateRequest;
import it.poste.anc.favorites.api.FavoriteItem;
import it.poste.anc.favorites.api.FavoriteType;
import it.poste.anc.favorites.api.FavoriteUpdateRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class FavoriteService {

    private final JdbcTemplate jdbcTemplate;

    public FavoriteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<FavoriteItem> listFavorites(String username) {
        Long userId = findActiveUserId(username);
        return jdbcTemplate.query(
                "SELECT id, titolo, url, tipo, created_at, updated_at FROM favorite_link "
                        + "WHERE user_id = ? ORDER BY updated_at DESC, id DESC",
                (rs, rowNum) -> new FavoriteItem(
                        rs.getLong("id"),
                        rs.getString("titolo"),
                        rs.getString("url"),
                        FavoriteType.valueOf(rs.getString("tipo")),
                        toInstant(rs.getTimestamp("created_at")),
                        toInstant(rs.getTimestamp("updated_at"))
                ),
                userId
        );
    }

    @Transactional
    public FavoriteItem createFavorite(String username, FavoriteCreateRequest request) {
        Long userId = findActiveUserId(username);
        String titolo = normalizeMandatory(request.titolo(), "Campo titolo obbligatorio", 8101, 120);
        String url = normalizeAndValidateUrl(request.url());
        FavoriteType tipo = normalizeAndValidateType(request.tipo());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        Instant now = Instant.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO favorite_link (user_id, titolo, url, tipo, created_at, updated_at, version) "
                            + "VALUES (?, ?, ?, ?, ?, ?, 0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, userId);
            ps.setString(2, titolo);
            ps.setString(3, url);
            ps.setString(4, tipo.name());
            ps.setTimestamp(5, Timestamp.from(now));
            ps.setTimestamp(6, Timestamp.from(now));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new FavoriteOperationException(HttpStatus.INTERNAL_SERVER_ERROR, 8107,
                    "Impossibile ottenere la chiave del favorito");
        }

        Long favoriteId = key.longValue();
        insertAuditEvent(username, "FAVORITE_CREATE_" + favoriteId, "FAVORITE_CREATED",
                "{\"favoriteId\":" + favoriteId + "}");

        return getFavoriteByIdForUser(favoriteId, userId);
    }

    @Transactional
    public FavoriteItem updateFavorite(String username, Long favoriteId, FavoriteUpdateRequest request) {
        Long userId = findActiveUserId(username);
        ensureFavoriteExistsForUser(favoriteId, userId);

        String titolo = normalizeMandatory(request.titolo(), "Campo titolo obbligatorio", 8101, 120);
        String url = normalizeAndValidateUrl(request.url());
        FavoriteType tipo = normalizeAndValidateType(request.tipo());

        int updated = jdbcTemplate.update(
                "UPDATE favorite_link SET titolo = ?, url = ?, tipo = ?, updated_at = CURRENT_TIMESTAMP(3), version = version + 1 "
                        + "WHERE id = ? AND user_id = ?",
                titolo,
                url,
                tipo.name(),
                favoriteId,
                userId
        );

        if (updated == 0) {
            throw new FavoriteOperationException(HttpStatus.CONFLICT, 8108,
                    "Favorito non aggiornabile in modo concorrente");
        }

        insertAuditEvent(username, "FAVORITE_UPDATE_" + favoriteId, "FAVORITE_UPDATED",
                "{\"favoriteId\":" + favoriteId + "}");

        return getFavoriteByIdForUser(favoriteId, userId);
    }

    @Transactional
    public void deleteFavorite(String username, Long favoriteId) {
        Long userId = findActiveUserId(username);
        ensureFavoriteExistsForUser(favoriteId, userId);

        int deleted = jdbcTemplate.update(
                "DELETE FROM favorite_link WHERE id = ? AND user_id = ?",
                favoriteId,
                userId
        );

        if (deleted == 0) {
            throw new FavoriteOperationException(HttpStatus.CONFLICT, 8109,
                    "Favorito non eliminabile in modo concorrente");
        }

        insertAuditEvent(username, "FAVORITE_DELETE_" + favoriteId, "FAVORITE_DELETED",
                "{\"favoriteId\":" + favoriteId + "}");
    }

    private FavoriteItem getFavoriteByIdForUser(Long favoriteId, Long userId) {
        List<FavoriteItem> items = jdbcTemplate.query(
                "SELECT id, titolo, url, tipo, created_at, updated_at FROM favorite_link WHERE id = ? AND user_id = ?",
                (rs, rowNum) -> new FavoriteItem(
                        rs.getLong("id"),
                        rs.getString("titolo"),
                        rs.getString("url"),
                        FavoriteType.valueOf(rs.getString("tipo")),
                        toInstant(rs.getTimestamp("created_at")),
                        toInstant(rs.getTimestamp("updated_at"))
                ),
                favoriteId,
                userId
        );

        if (items.isEmpty()) {
            throw new FavoriteOperationException(HttpStatus.NOT_FOUND, 8106, "Favorito non trovato");
        }

        return items.getFirst();
    }

    private void ensureFavoriteExistsForUser(Long favoriteId, Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM favorite_link WHERE id = ? AND user_id = ?",
                Integer.class,
                favoriteId,
                userId
        );

        if (count == null || count == 0) {
            throw new FavoriteOperationException(HttpStatus.NOT_FOUND, 8106, "Favorito non trovato");
        }
    }

    private Long findActiveUserId(String username) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM app_user WHERE username = ? AND active = 1",
                    Long.class,
                    username
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new FavoriteOperationException(HttpStatus.UNAUTHORIZED, 1002, "Utente non autenticato");
        }
    }

    private String normalizeAndValidateUrl(String value) {
        String url = normalizeMandatory(value, "Campo url obbligatorio", 8102, 1000);
        try {
            URI parsed = new URI(url);
            String scheme = parsed.getScheme();
            if (scheme == null) {
                throw new FavoriteOperationException(HttpStatus.BAD_REQUEST, 8103,
                        "Campo url non valido: schema mancante");
            }
            String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
            if (!"http".equals(normalizedScheme) && !"https".equals(normalizedScheme)) {
                throw new FavoriteOperationException(HttpStatus.BAD_REQUEST, 8103,
                        "Campo url non valido: schema ammesso http/https");
            }
        } catch (URISyntaxException ex) {
            throw new FavoriteOperationException(HttpStatus.BAD_REQUEST, 8103, "Campo url non valido");
        }
        return url;
    }

    private FavoriteType normalizeAndValidateType(String value) {
        String raw = normalizeMandatory(value, "Campo tipo obbligatorio", 8104, 30);
        try {
            return FavoriteType.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new FavoriteOperationException(HttpStatus.BAD_REQUEST, 8105,
                    "Campo tipo non valido: valori ammessi INTERNO, ESTERNO, LEGACY");
        }
    }

    private String normalizeMandatory(String value, String message, int resultCode, int maxLen) {
        if (value == null) {
            throw new FavoriteOperationException(HttpStatus.BAD_REQUEST, resultCode, message);
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new FavoriteOperationException(HttpStatus.BAD_REQUEST, resultCode, message);
        }
        if (normalized.length() > maxLen) {
            throw new FavoriteOperationException(HttpStatus.BAD_REQUEST, resultCode,
                    message + ": lunghezza massima " + maxLen);
        }
        return normalized;
    }

    private void insertAuditEvent(String actorUsername,
                                  String correlationId,
                                  String eventType,
                                  String payloadJson) {
        jdbcTemplate.update(
                "INSERT INTO audit_event (occurred_at, actor_username, event_type, practice_id, correlation_id, payload_json) "
                        + "VALUES (CURRENT_TIMESTAMP(3), ?, ?, NULL, ?, ?)",
                actorUsername,
                eventType,
                correlationId,
                payloadJson
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant();
    }
}
