package it.poste.anc.supervision.api;

import it.poste.anc.supervision.application.SupervisionTaskService;
import it.poste.anc.workflow.application.TaskOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupervisionTaskController.class)
class SupervisionTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupervisionTaskService supervisionTaskService;

    @Test
    @WithMockUser(username = "sup.verdi")
    void listReturnsSupervisionTasks() throws Exception {
        when(supervisionTaskService.listSupervisionTasks(eq("sup.verdi"), eq(null), eq(null), eq(null), eq(null)))
                .thenReturn(List.of(new SupervisionTaskListItem(
                        90L,
                        500L,
                        "PRAT-500",
                        "IN_CODA",
                        "IN_LAVORAZIONE",
                        null,
                        "GRUPPO_OPERATORE_ANC",
                        null
                )));

        mockMvc.perform(get("/api/v1/supervision/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].practiceNumber").value("PRAT-500"));
    }

    @Test
    @WithMockUser(username = "op.rossi")
    void listReturnsForbiddenWhenServiceRejectsRole() throws Exception {
        when(supervisionTaskService.listSupervisionTasks(eq("op.rossi"), any(), any(), any(), any()))
                .thenThrow(new TaskOperationException(HttpStatus.FORBIDDEN, 6001,
                        "Utente non autorizzato: ruolo SUPERVISORE_ANC richiesto"));

        mockMvc.perform(get("/api/v1/supervision/tasks"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value(6001));
    }

    @Test
    @WithMockUser(username = "sup.verdi")
    void reassignUserReturnsOk() throws Exception {
        when(supervisionTaskService.reassignToUser(90L, "sup.verdi", "op.bianchi", "bilanciamento"))
                .thenReturn(new SupervisionTaskReassignResponse(
                        90L,
                        500L,
                        "REASSIGN_USER",
                        "IN_CARICO",
                        "op.bianchi",
                        "op.bianchi",
                        null
                ));

        mockMvc.perform(post("/api/v1/supervision/tasks/90/reassign-user")
                        .contentType("application/json")
                        .content("{\"username\":\"op.bianchi\",\"reason\":\"bilanciamento\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details.assignmentType").value("REASSIGN_USER"))
                .andExpect(jsonPath("$.details.ownerUsername").value("op.bianchi"));
    }
}
