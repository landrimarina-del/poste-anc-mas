package it.poste.anc.practice.api;

import it.poste.anc.practice.application.PracticeQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PracticeController.class)
@AutoConfigureMockMvc(addFilters = false)
class PracticeControllerRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PracticeQueryService practiceQueryService;

    @Test
    void listPracticesReturnsStableContract() throws Exception {
        Instant openedAt = Instant.parse("2026-05-13T10:15:30Z");
        when(practiceQueryService.getPracticeList(any()))
            .thenReturn(new PracticeListPage(
                List.of(new PracticeListItem(11L, "PR-11", "REQ-11", "WI-11", "APERTA", "OK", openedAt, null, openedAt, null, null, null, null, 0)),
                1L,
                0,
                20
            ));

        mockMvc.perform(get("/api/v1/practices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.resultMessage").value("OK"))
            .andExpect(jsonPath("$.details.items[0].practiceId").value(11))
            .andExpect(jsonPath("$.details.items[0].requestId").value("REQ-11"))
            .andExpect(jsonPath("$.details.items[0].idWorkItem").value("WI-11"))
            .andExpect(jsonPath("$.details.items[0].state").value("APERTA"))
            .andExpect(jsonPath("$.details.items[0].openedAt").value("2026-05-13T10:15:30Z"));
    }
}
