package it.poste.anc.document.api;

import it.poste.anc.document.application.DocumentOperationException;
import it.poste.anc.document.application.IntakeChecklistService;
import it.poste.anc.document.application.IntakeChecklistHelpService;
import it.poste.anc.document.application.IntakePracticeCloseService;
import it.poste.anc.shared.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/practices/{id}/intake", produces = MediaType.APPLICATION_JSON_VALUE)
public class IntakeChecklistController {

    private final IntakeChecklistService intakeChecklistService;
    private final IntakeChecklistHelpService intakeChecklistHelpService;
    private final IntakePracticeCloseService intakePracticeCloseService;

    public IntakeChecklistController(IntakeChecklistService intakeChecklistService,
                                     IntakeChecklistHelpService intakeChecklistHelpService,
                                     IntakePracticeCloseService intakePracticeCloseService) {
        this.intakeChecklistService = intakeChecklistService;
        this.intakeChecklistHelpService = intakeChecklistHelpService;
        this.intakePracticeCloseService = intakePracticeCloseService;
    }

    @GetMapping(path = "/checklist")
    public ResponseEntity<ApiResponse<IntakeChecklistResponse>> getChecklist(@PathVariable("id") Long practiceId) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(intakeChecklistService.loadChecklist(practiceId)));
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @PutMapping(path = "/checklist", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<IntakeChecklistResponse>> saveChecklistDraft(@PathVariable("id") Long practiceId,
                                                                                    @RequestBody IntakeChecklistRequest request,
                                                                                    Authentication authentication) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    intakeChecklistService.saveDraft(practiceId, request, authentication.getName())
            ));
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @PostMapping(path = "/checklist/edit")
    public ResponseEntity<ApiResponse<IntakeChecklistEditResponse>> reopenChecklist(@PathVariable("id") Long practiceId,
                                                                                     Authentication authentication) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    intakeChecklistService.reopenDraft(practiceId, authentication.getName())
            ));
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @PostMapping(path = "/close")
    public ResponseEntity<ApiResponse<IntakeCloseResponse>> closePractice(@PathVariable("id") Long practiceId,
                                                                           Authentication authentication) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    intakePracticeCloseService.closePractice(practiceId, authentication.getName())
            ));
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @GetMapping(path = "/checklist/help/{itemId}")
    public ResponseEntity<ApiResponse<IntakeChecklistHelpResponse>> getChecklistHelp(@PathVariable("id") Long practiceId,
                                                                                      @PathVariable("itemId") String itemId,
                                                                                      Authentication authentication) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    intakeChecklistHelpService.getHelp(practiceId, itemId, authentication.getName())
            ));
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }
}
