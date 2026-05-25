package it.poste.anc.bpmgw.inbound;

import it.poste.anc.shared.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BpmPracticeInboundController.class)
@AutoConfigureMockMvc(addFilters = false)
class BpmPracticeInboundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BpmPracticeInboundService inboundService;

    @Test
    void openPracticeReturnsOkForHappyPath() throws Exception {
        when(inboundService.openPractice(any()))
                .thenReturn(ApiResponse.ok(new BpmPracticeOpenResponse(101L, "REQ-100", "APERTA")));

        String payload = """
                {
                  "ID_WORKITEM": "WI-100",
                  "REQUEST_ID": "REQ-100",
                  "DOCUMENTI": [
                    {
                      "CODICE_DOC_ID": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/bpm/practices")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details.requestId").value("REQ-100"))
                .andExpect(jsonPath("$.details.state").value("APERTA"));
    }

    @Test
    void openPracticeReturnsMinus4ForInvalidPayload() throws Exception {
        when(inboundService.openPractice(any()))
                .thenReturn(ApiResponse.error(-4, "Messaggio non valido: payload obbligatorio e sezione DOCUMENTI richiesta"));

        String payload = "{}";

        mockMvc.perform(post("/api/v1/bpm/practices")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(-4));
    }

    @Test
    void openPracticeReturnsMinus5ForDuplicateIdWorkItem() throws Exception {
        when(inboundService.openPractice(any()))
                .thenReturn(ApiResponse.error(-5, "Idempotenza violata: ID_WORKITEM gia' presente"));

        String payload = """
                {
                  "ID_WORKITEM": "WI-DUP",
                  "DOCUMENTI": [
                    {
                      "CODICE_DOC_ID": 2
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/bpm/practices")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(-5));
    }
}
