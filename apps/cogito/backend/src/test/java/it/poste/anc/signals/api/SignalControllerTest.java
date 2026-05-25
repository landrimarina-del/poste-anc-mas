package it.poste.anc.signals.api;

import it.poste.anc.signals.application.SignalOperationException;
import it.poste.anc.signals.application.SignalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignalController.class)
class SignalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignalService signalService;

    @Test
    @WithMockUser(username = "op.rossi")
    void createSignalReturnsEnvelope() throws Exception {
        when(signalService.createSignal(eq("op.rossi"), any(SignalCreateRequest.class)))
                .thenReturn(new SignalCreateResponse(44L, 100L, "IN_CODA", "op.rossi", Instant.parse("2026-05-15T10:15:30Z")));

        mockMvc.perform(post("/api/v1/signals")
                        .contentType("application/json")
                        .content("""
                                {
                                  "practiceId": 100,
                                  "subject": "Documento non leggibile",
                                  "description": "Il file allegato risulta illeggibile"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details.signalId").value(44))
                .andExpect(jsonPath("$.details.state").value("IN_CODA"));
    }

    @Test
    @WithMockUser(username = "sup.verdi")
    void listSignalsReturnsForbiddenWhenNotAuthorized() throws Exception {
        when(signalService.listSignalsForSupervisor("sup.verdi", null, "IN_CODA", null,
                LocalDate.parse("2026-05-01"), LocalDate.parse("2026-05-31")))
                .thenThrow(new SignalOperationException(HttpStatus.FORBIDDEN, 7013,
                        "Utente non autorizzato: ruolo SUPERVISORE_ANC richiesto"));

        mockMvc.perform(get("/api/v1/signals")
                        .param("state", "IN_CODA")
                        .param("fromDate", "2026-05-01")
                        .param("toDate", "2026-05-31"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value(7013));
    }

    @Test
    @WithMockUser(username = "sup.verdi")
    void listMySignalsReturnsData() throws Exception {
        when(signalService.listMySignals("sup.verdi", null, null, null)).thenReturn(List.of(
                new SignalListItem(
                        10L,
                        100L,
                        "PRAT-2026-0001",
                        "IN_LAVORAZIONE",
                        "sup.verdi",
                        "op.rossi",
                        "Anomalia workflow",
                        null,
                        Instant.parse("2026-05-15T09:00:00Z"),
                        Instant.parse("2026-05-15T09:05:00Z")
                )
        ));

        mockMvc.perform(get("/api/v1/signals/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].signalId").value(10))
                .andExpect(jsonPath("$.details[0].state").value("IN_LAVORAZIONE"));
    }
}
