package it.poste.anc.document.api;

import it.poste.anc.document.application.DocumentOperationException;
import it.poste.anc.document.application.IntakeTypingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IntakeTypingController.class)
class IntakeTypingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IntakeTypingService intakeTypingService;

    @Test
    @WithMockUser(username = "operatore.anc")
    void confirmTypingReturnsOkWhenValid() throws Exception {
        when(intakeTypingService.confirmTyping(eq(100L), eq("Verbale"), eq("operatore.anc")))
                .thenReturn(new IntakeTypingResponse(100L, "VERBALE", false));

        mockMvc.perform(post("/api/v1/practices/100/intake/typing")
                        .contentType("application/json")
                        .content("{\"documentType\":\"Verbale\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details.documentType").value("VERBALE"));
    }

    @Test
    @WithMockUser(username = "operatore.anc")
    void confirmTypingReturnsBusinessErrorWhenChangingTypeAfterConfirmation() throws Exception {
        when(intakeTypingService.confirmTyping(eq(100L), eq("Carta"), eq("operatore.anc")))
                .thenThrow(new DocumentOperationException(HttpStatus.CONFLICT, 4012,
                        "Tipo documento gia confermato in modo irreversibile: VERBALE"));

        mockMvc.perform(post("/api/v1/practices/100/intake/typing")
                        .contentType("application/json")
                        .content("{\"documentType\":\"Carta\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value(4012));
    }

    @Test
    @WithMockUser(username = "operatore.altro")
    void confirmTypingReturnsForbiddenWhenUserIsNotTaskOwner() throws Exception {
        when(intakeTypingService.confirmTyping(eq(100L), eq("Verbale"), eq("operatore.altro")))
                .thenThrow(new DocumentOperationException(HttpStatus.FORBIDDEN, 4014,
                        "Tipizzazione non autorizzata: task non in carico all'utente corrente"));

        mockMvc.perform(post("/api/v1/practices/100/intake/typing")
                        .contentType("application/json")
                        .content("{\"documentType\":\"Verbale\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value(4014));
    }

    @Test
    @WithMockUser(username = "operatore.anc")
    void confirmTypingReturnsConflictWhenPracticeIsNotInLavorazione() throws Exception {
        when(intakeTypingService.confirmTyping(eq(101L), eq("Verbale"), eq("operatore.anc")))
                .thenThrow(new DocumentOperationException(HttpStatus.CONFLICT, 4010,
                        "Tipizzazione consentita solo per pratiche in stato IN_LAVORAZIONE"));

        mockMvc.perform(post("/api/v1/practices/101/intake/typing")
                        .contentType("application/json")
                        .content("{\"documentType\":\"Verbale\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value(4010));
    }
}
