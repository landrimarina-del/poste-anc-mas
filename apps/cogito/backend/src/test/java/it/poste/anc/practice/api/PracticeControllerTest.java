package it.poste.anc.practice.api;

import it.poste.anc.practice.application.PracticeQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PracticeController.class)
class PracticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PracticeQueryService practiceQueryService;

    @Test
    @WithMockUser(username = "operatore.anc")
    void relatedActionsReturnsEnvelopeWithRows() throws Exception {
        when(practiceQueryService.practiceExists(100L)).thenReturn(true);
        when(practiceQueryService.getRelatedActions(100L)).thenReturn(List.of(
                new PracticeRelatedActionItem(10L, "SIGNAL_DETAIL", "Segnalazione #10 (IN_CODA)", "/signals/10")
        ));

        mockMvc.perform(get("/api/v1/practices/100/related-actions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].actionId").value(10))
                .andExpect(jsonPath("$.details[0].actionCode").value("SIGNAL_DETAIL"));
    }

    @Test
    @WithMockUser(username = "operatore.anc")
    void exportReturnsXlsxAttachment() throws Exception {
        when(practiceQueryService.exportPracticeListExcel(any(PracticeListQuery.class)))
                .thenReturn(new byte[]{0x01, 0x02, 0x03});

        mockMvc.perform(get("/api/v1/practices/export")
                        .param("state", "IN_LAVORAZIONE"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=pratiche-anc.xlsx"));
    }

    @Test
    @WithMockUser(username = "operatore.anc")
    void historyReturnsCombinedAuditTrail() throws Exception {
        when(practiceQueryService.practiceExists(100L)).thenReturn(true);
        when(practiceQueryService.getPracticeHistory(eq(100L))).thenReturn(List.of(
                new PracticeHistoryItem(
                        900L,
                        "STATE_CHANGED",
                        "operatore.anc",
                        "TASK_ACCEPT_22",
                        Instant.parse("2026-05-16T10:00:00Z"),
                        "APERTA -> IN_LAVORAZIONE"
                )
        ));

        mockMvc.perform(get("/api/v1/practices/100/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].eventType").value("STATE_CHANGED"));
    }
}
