package it.poste.anc.supervision.api;

import it.poste.anc.supervision.application.SupervisionDashboardService;
import it.poste.anc.workflow.application.TaskOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.time.YearMonth;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupervisionDashboardController.class)
class SupervisionDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupervisionDashboardService supervisionDashboardService;

    @Test
    @WithMockUser(username = "sup.verdi")
    void countersReturnsDashboardNumbers() throws Exception {
        when(supervisionDashboardService.loadCounters("sup.verdi"))
                .thenReturn(new SupervisionDashboardCountersResponse(8, 4, 10));

        mockMvc.perform(get("/api/v1/supervision/dashboard/counters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details.activities").value(8))
                .andExpect(jsonPath("$.details.activePractices").value(4))
                .andExpect(jsonPath("$.details.closedPractices").value(10));
    }

    @Test
    @WithMockUser(username = "sup.verdi")
    void dailyOpenedReturnsMonthlyHistogram() throws Exception {
        when(supervisionDashboardService.loadDailyOpened(eq("sup.verdi"), eq(YearMonth.of(2026, 5))))
                .thenReturn(List.of(
                        new SupervisionDailyOpenedPoint(1, 2),
                        new SupervisionDailyOpenedPoint(2, 0)
                ));

        mockMvc.perform(get("/api/v1/supervision/dashboard/daily-opened").param("month", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].day").value(1))
                .andExpect(jsonPath("$.details[0].openedPractices").value(2));
    }

    @Test
    @WithMockUser(username = "sup.verdi")
    void dailyWorkedReturnsOkKoHistogram() throws Exception {
        when(supervisionDashboardService.loadDailyWorked(eq("sup.verdi"), eq(YearMonth.of(2026, 5))))
                .thenReturn(List.of(new SupervisionDailyWorkedPoint(1, 3, 1)));

        mockMvc.perform(get("/api/v1/supervision/dashboard/daily-worked").param("month", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].okPractices").value(3))
                .andExpect(jsonPath("$.details[0].koPractices").value(1));
    }

    @Test
    @WithMockUser(username = "sup.verdi")
    void byStateReturnsHistogram() throws Exception {
        when(supervisionDashboardService.loadPracticesByState(eq("sup.verdi"), isNull()))
                .thenReturn(List.of(new SupervisionPracticeByStatePoint("IN_LAVORAZIONE", 7)));

        mockMvc.perform(get("/api/v1/supervision/dashboard/by-state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].state").value("IN_LAVORAZIONE"));
    }

    @Test
    @WithMockUser(username = "sup.verdi")
    void byStateWithMonthFiltersData() throws Exception {
        when(supervisionDashboardService.loadPracticesByState(eq("sup.verdi"), eq(YearMonth.of(2026, 5))))
                .thenReturn(List.of(new SupervisionPracticeByStatePoint("APERTA", 3)));

        mockMvc.perform(get("/api/v1/supervision/dashboard/by-state?month=2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].state").value("APERTA"));
    }

    @Test
    @WithMockUser(username = "op.rossi")
    void countersRejectsNonSupervisor() throws Exception {
        when(supervisionDashboardService.loadCounters("op.rossi"))
                .thenThrow(new TaskOperationException(HttpStatus.FORBIDDEN, 6101,
                        "Utente non autorizzato: ruolo SUPERVISORE_ANC richiesto"));

        mockMvc.perform(get("/api/v1/supervision/dashboard/counters"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value(6101));
    }

    @Test
    @WithMockUser(username = "sup.verdi")
    void dailyOpenedRejectsInvalidMonth() throws Exception {
        mockMvc.perform(get("/api/v1/supervision/dashboard/daily-opened").param("month", "05-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value(6102));
    }
}
