package it.poste.anc.document.api;

import it.poste.anc.document.application.DocumentOperationException;
import it.poste.anc.document.application.IntakeTypingService;
import it.poste.anc.shared.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/practices/{id}/intake", produces = MediaType.APPLICATION_JSON_VALUE)
public class IntakeTypingController {

    private final IntakeTypingService intakeTypingService;

    public IntakeTypingController(IntakeTypingService intakeTypingService) {
        this.intakeTypingService = intakeTypingService;
    }

    @PostMapping(path = "/typing", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<IntakeTypingResponse>> confirmTyping(@PathVariable("id") Long practiceId,
                                                                            @Valid @RequestBody IntakeTypingRequest request,
                                                                            Authentication authentication) {
        try {
            IntakeTypingResponse response = intakeTypingService.confirmTyping(
                    practiceId,
                    request.documentType(),
                    authentication.getName()
            );
            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }
}
