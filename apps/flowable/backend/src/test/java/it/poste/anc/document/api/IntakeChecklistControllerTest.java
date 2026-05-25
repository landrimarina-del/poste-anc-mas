package it.poste.anc.document.api;

import it.poste.anc.document.application.DocumentOperationException;
import it.poste.anc.document.application.IntakeChecklistHelpService;
import it.poste.anc.document.application.IntakeChecklistService;
import it.poste.anc.document.application.IntakePracticeCloseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IntakeChecklistController.class)
class IntakeChecklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IntakeChecklistService intakeChecklistService;

    @MockBean
    private IntakeChecklistHelpService intakeChecklistHelpService;

    @MockBean
    private IntakePracticeCloseService intakePracticeCloseService;

    @Test
    @WithMockUser(username = "operatore.anc")
    void getChecklistHelpReturnsHelpDescription() throws Exception {
        when(intakeChecklistHelpService.getHelp(100L, "FORMALOK", "operatore.anc"))
                .thenReturn(new IntakeChecklistHelpResponse(
                        100L,
                        "VERBALE",
                        "FORMALOK",
                        "Conformita formale",
                        "Verifica che il verbale rispetti i requisiti formali previsti dalla checklist ANC."
                ));

        mockMvc.perform(get("/api/v1/practices/100/intake/checklist/help/FORMALOK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details.itemId").value("FORMALOK"));
    }

    @Test
    @WithMockUser(username = "operatore.anc")
    void getChecklistHelpReturnsNotFoundWhenItemMissing() throws Exception {
        when(intakeChecklistHelpService.getHelp(eq(100L), eq("UNKNOWN"), eq("operatore.anc")))
                .thenThrow(new DocumentOperationException(HttpStatus.NOT_FOUND, 4041,
                        "Descrizione help non disponibile per item UNKNOWN"));

        mockMvc.perform(get("/api/v1/practices/100/intake/checklist/help/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value(4041));
    }
}
