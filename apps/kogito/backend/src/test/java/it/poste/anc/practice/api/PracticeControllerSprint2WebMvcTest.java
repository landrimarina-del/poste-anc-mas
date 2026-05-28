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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PracticeController.class)
@AutoConfigureMockMvc(addFilters = false)
class PracticeControllerSprint2WebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PracticeQueryService practiceQueryService;

    @Test
    void listPracticesSupportsFiltersSortAndPagination() throws Exception {
        PracticeListItem item = new PracticeListItem(
                101L,
                "PR-2026-0001",
                "REQ-2026-0001",
                "WI-2026-0001",
                "APERTA",
                "OK",
                Instant.parse("2026-05-10T10:15:30Z"),
                null,
                Instant.parse("2026-05-12T12:00:00Z"),
                null, null, null, null, 0
        );
        when(practiceQueryService.getPracticeList(any()))
                .thenReturn(new PracticeListPage(List.of(item), 1L, 0, 20));

        mockMvc.perform(get("/api/v1/practices")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "openedAt,desc")
                        .param("practiceNumber", "PR-2026")
                        .param("state", "APERTA")
                        .param("openedFrom", "2026-05-01")
                        .param("openedTo", "2026-05-31")
                        .param("sdOutcome", "OK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details.total").value(1))
                .andExpect(jsonPath("$.details.items[0].practiceId").value(101))
                .andExpect(jsonPath("$.details.items[0].practiceNumber").value("PR-2026-0001"))
                .andExpect(jsonPath("$.details.items[0].state").value("APERTA"));
    }

    @Test
    void detailReturns404WhenPracticeDoesNotExist() throws Exception {
        when(practiceQueryService.getPracticeDetail(eq(999L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/practices/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value(2004));
    }

    @Test
    void historyReturnsItemsWhenPracticeExists() throws Exception {
        when(practiceQueryService.practiceExists(101L)).thenReturn(true);
        when(practiceQueryService.getPracticeHistory(101L)).thenReturn(List.of(
                new PracticeHistoryItem(10L, "PRACTICE_OPENED", "BPM_SYSTEM", "WI-2026-0001",
                        Instant.parse("2026-05-10T10:15:30Z"), null)
        ));

        mockMvc.perform(get("/api/v1/practices/101/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].eventType").value("PRACTICE_OPENED"));
    }

    @Test
    void statesReturns404WhenPracticeDoesNotExist() throws Exception {
        when(practiceQueryService.practiceExists(888L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/practices/888/states"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value(2004));

        verify(practiceQueryService).practiceExists(888L);
    }
}
