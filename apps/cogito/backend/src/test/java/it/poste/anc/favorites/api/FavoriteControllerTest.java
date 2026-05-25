package it.poste.anc.favorites.api;

import it.poste.anc.favorites.application.FavoriteOperationException;
import it.poste.anc.favorites.application.FavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FavoriteController.class)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @Test
    @WithMockUser(username = "op.rossi")
    void listFavoritesReturnsEnvelope() throws Exception {
        when(favoriteService.listFavorites("op.rossi")).thenReturn(List.of(
                new FavoriteItem(
                        11L,
                        "Inbox ANC",
                        "https://anc.poste.it/inbox",
                        FavoriteType.INTERNO,
                        Instant.parse("2026-05-16T08:00:00Z"),
                        Instant.parse("2026-05-16T08:10:00Z")
                )
        ));

        mockMvc.perform(get("/api/v1/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].id").value(11))
                .andExpect(jsonPath("$.details[0].tipo").value("INTERNO"));
    }

    @Test
    @WithMockUser(username = "op.rossi")
    void createFavoriteReturnsCreatedItem() throws Exception {
        when(favoriteService.createFavorite(eq("op.rossi"), any(FavoriteCreateRequest.class)))
                .thenReturn(new FavoriteItem(
                        12L,
                        "Portale Legacy",
                        "https://legacy.poste.it/home",
                        FavoriteType.LEGACY,
                        Instant.parse("2026-05-16T09:00:00Z"),
                        Instant.parse("2026-05-16T09:00:00Z")
                ));

        mockMvc.perform(post("/api/v1/favorites")
                        .contentType("application/json")
                        .content("""
                                {
                                  "titolo": "Portale Legacy",
                                  "url": "https://legacy.poste.it/home",
                                  "tipo": "LEGACY"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details.id").value(12));
    }

    @Test
    @WithMockUser(username = "op.rossi")
    void updateFavoriteReturnsUpdatedItem() throws Exception {
        when(favoriteService.updateFavorite(eq("op.rossi"), eq(12L), any(FavoriteUpdateRequest.class)))
                .thenReturn(new FavoriteItem(
                        12L,
                        "Repository ANC",
                        "https://repo.poste.it/anc",
                        FavoriteType.ESTERNO,
                        Instant.parse("2026-05-16T09:00:00Z"),
                        Instant.parse("2026-05-16T09:05:00Z")
                ));

        mockMvc.perform(put("/api/v1/favorites/12")
                        .contentType("application/json")
                        .content("""
                                {
                                  "titolo": "Repository ANC",
                                  "url": "https://repo.poste.it/anc",
                                  "tipo": "ESTERNO"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details.tipo").value("ESTERNO"));
    }

    @Test
    @WithMockUser(username = "op.rossi")
    void deleteFavoriteReturnsOkEnvelope() throws Exception {
        mockMvc.perform(delete("/api/v1/favorites/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0));
    }

    @Test
    @WithMockUser(username = "op.rossi")
    void createFavoriteReturnsBadRequestOnValidationError() throws Exception {
        when(favoriteService.createFavorite(eq("op.rossi"), any(FavoriteCreateRequest.class)))
                .thenThrow(new FavoriteOperationException(HttpStatus.BAD_REQUEST, 8105,
                        "Campo tipo non valido: valori ammessi INTERNO, ESTERNO, LEGACY"));

        mockMvc.perform(post("/api/v1/favorites")
                        .contentType("application/json")
                        .content("""
                                {
                                  "titolo": "Portale",
                                  "url": "https://poste.it",
                                  "tipo": "ALTRO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value(8105));
    }

    @Test
    @WithMockUser(username = "op.rossi")
    void deleteFavoriteReturnsNotFoundWhenOwnershipMismatch() throws Exception {
        doThrow(new FavoriteOperationException(HttpStatus.NOT_FOUND, 8106, "Favorito non trovato"))
                .when(favoriteService).deleteFavorite(eq("op.rossi"), anyLong());

        mockMvc.perform(delete("/api/v1/favorites/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value(8106));
    }
}
