package it.poste.anc.workflow.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WorkflowReadinessController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkflowReadinessEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationContext applicationContext;

    @Test
    void readinessReturnsTechnicalFallbackWhenEngineBeansAreNotAvailable() throws Exception {
        when(applicationContext.containsBean("processEngine")).thenReturn(false);

        mockMvc.perform(get("/api/v1/technical/workflow/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.resultMessage").value("OK"))
                .andExpect(jsonPath("$.details.engineActive").value(false))
                .andExpect(jsonPath("$.details.placeholderProcessDeployed").value(false))
                .andExpect(jsonPath("$.details.fallback").isNotEmpty());
    }
}
