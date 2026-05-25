package it.poste.anc.document.api;

import it.poste.anc.document.application.AttachmentQueryService;
import it.poste.anc.document.application.AttachmentReadResult;
import it.poste.anc.document.application.DocumentOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttachmentController.class)
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttachmentQueryService attachmentQueryService;

    @Test
    @WithMockUser(username = "operatore.anc")
    void previewAttachmentReturnsInlineBinary() throws Exception {
        when(attachmentQueryService.readAttachmentForPreview(11L)).thenReturn(
                new AttachmentReadResult("verbale.pdf", "application/pdf", "PDF".getBytes(), null)
        );

        mockMvc.perform(get("/api/v1/attachments/11/preview"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @WithMockUser(username = "operatore.anc")
    void downloadAttachmentReturnsRedirectFallback() throws Exception {
        when(attachmentQueryService.readAttachmentForDownload(11L)).thenReturn(
                new AttachmentReadResult("verbale.pdf", "application/pdf", null, "https://example/download/11")
        );

        mockMvc.perform(get("/api/v1/attachments/11/download"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example/download/11"));
    }

    @Test
    @WithMockUser(username = "operatore.anc")
    void previewAttachmentMissingReturnsUiFriendlyTechnicalMessage() throws Exception {
        when(attachmentQueryService.readAttachmentForPreview(99L)).thenThrow(
                new DocumentOperationException(
                        HttpStatus.NOT_FOUND,
                        4004,
                        "Errore tecnico nel recupero allegato. Usa il download fallback e prosegui con la tipizzazione."
                )
        );

        mockMvc.perform(get("/api/v1/attachments/99/preview"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value(4004))
                .andExpect(jsonPath("$.resultMessage").value("Errore tecnico nel recupero allegato. Usa il download fallback e prosegui con la tipizzazione."));
    }

    @Test
    @WithMockUser(username = "operatore.anc")
    void listPracticeAttachmentsReturnsMetadata() throws Exception {
        when(attachmentQueryService.listPracticeAttachments(100L)).thenReturn(List.of(
                new AttachmentListItem(1L, "verbale.pdf", "pdf", "application/pdf", 1024L)
        ));

        mockMvc.perform(get("/api/v1/practices/100/attachments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details[0].attachmentId").value(1));
    }
}
