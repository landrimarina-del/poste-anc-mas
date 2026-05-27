package it.poste.anc.document.api;

import it.poste.anc.document.application.CaseNoteService;
import it.poste.anc.document.application.DocumentOperationException;
import it.poste.anc.shared.common.ApiResponse;
import org.springframework.http.HttpStatus;
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

import java.util.List;

/**
 * Controller REST per le note intermediate di pratica.
 * GET  /api/v1/practices/{id}/notes  → lista note ordinate per created_at DESC
 * POST /api/v1/practices/{id}/notes  → crea nota tipo LAVORAZIONE
 */
@RestController
@RequestMapping(path = "/api/v1/practices/{id}/notes", produces = MediaType.APPLICATION_JSON_VALUE)
public class CaseNoteController {

    private final CaseNoteService caseNoteService;

    public CaseNoteController(CaseNoteService caseNoteService) {
        this.caseNoteService = caseNoteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CaseNoteDto>>> listNotes(@PathVariable("id") Long practiceId) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(caseNoteService.listByPractice(practiceId)));
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CaseNoteDto>> createNote(@PathVariable("id") Long practiceId,
                                                               @RequestBody CaseNoteRequest request,
                                                               Authentication authentication) {
        try {
            CaseNoteDto dto = caseNoteService.createNote(practiceId, request.testo(), authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto));
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CaseNoteDto>> upsertNote(@PathVariable("id") Long practiceId,
                                                               @RequestBody CaseNoteRequest request,
                                                               Authentication authentication) {
        try {
            CaseNoteDto dto = caseNoteService.upsertNote(practiceId, request.testo(), authentication.getName());
            return ResponseEntity.ok(ApiResponse.ok(dto));
        } catch (DocumentOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }
}
