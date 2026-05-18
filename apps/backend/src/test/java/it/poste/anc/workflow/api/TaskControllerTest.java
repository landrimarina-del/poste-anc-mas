package it.poste.anc.workflow.api;

import it.poste.anc.workflow.application.TaskManagementService;
import it.poste.anc.workflow.application.TaskOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskManagementService taskManagementService;

    @Test
    @WithMockUser(username = "operatore.anc")
    void listTasksReturnsQueueForCurrentOperator() throws Exception {
        when(taskManagementService.listTasksForCurrentOperator("operatore.anc", null, null)).thenReturn(List.of(
                new TaskListItem(
                        10L,
                        100L,
                        "PRAT-2026-0001",
                        "REQ-001",
                        "WI-001",
                        "IN_CODA",
                        "APERTA",
                        null,
                        null,
                        null
                )
        ));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].taskId").value(10))
                .andExpect(jsonPath("$.details[0].practiceState").value("APERTA"));
    }

    @Test
    @WithMockUser(username = "operatore.anc")
    void acceptTaskUpdatesStateToInLavorazione() throws Exception {
        when(taskManagementService.acceptTask(10L, "operatore.anc"))
                .thenReturn(new TaskAcceptResponse(10L, 100L, "IN_LAVORAZIONE", "operatore.anc"));

        mockMvc.perform(post("/api/v1/tasks/10/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details.practiceState").value("IN_LAVORAZIONE"))
                .andExpect(jsonPath("$.details.ownerUsername").value("operatore.anc"));
    }

    @Test
    @WithMockUser(username = "operatore.anc")
    void acceptTaskReturnsConflictWhenAlreadyTaken() throws Exception {
        when(taskManagementService.acceptTask(anyLong(), eq("operatore.anc")))
                .thenThrow(new TaskOperationException(HttpStatus.CONFLICT, 3008,
                        "Task non piu disponibile per la presa in carico"));

        mockMvc.perform(post("/api/v1/tasks/10/accept"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value(3008));
    }
}
