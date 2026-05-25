package it.poste.anc.favorites.api;

import it.poste.anc.favorites.application.FavoriteOperationException;
import it.poste.anc.favorites.application.FavoriteService;
import it.poste.anc.shared.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteItem>>> listFavorites(Authentication authentication) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(favoriteService.listFavorites(authentication.getName())));
        } catch (FavoriteOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<FavoriteItem>> createFavorite(@RequestBody FavoriteCreateRequest request,
                                                                    Authentication authentication) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(favoriteService.createFavorite(authentication.getName(), request)));
        } catch (FavoriteOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<FavoriteItem>> updateFavorite(@PathVariable("id") Long favoriteId,
                                                                    @RequestBody FavoriteUpdateRequest request,
                                                                    Authentication authentication) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    favoriteService.updateFavorite(authentication.getName(), favoriteId, request)
            ));
        } catch (FavoriteOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFavorite(@PathVariable("id") Long favoriteId,
                                                            Authentication authentication) {
        try {
            favoriteService.deleteFavorite(authentication.getName(), favoriteId);
            return ResponseEntity.ok(ApiResponse.ok());
        } catch (FavoriteOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }
}
