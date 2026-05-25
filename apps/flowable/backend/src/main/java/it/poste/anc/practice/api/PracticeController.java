package it.poste.anc.practice.api;

import it.poste.anc.practice.application.PracticeQueryService;
import it.poste.anc.shared.common.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(path = "/api/v1/practices", produces = MediaType.APPLICATION_JSON_VALUE)
public class PracticeController {

    private final PracticeQueryService practiceQueryService;

    public PracticeController(PracticeQueryService practiceQueryService) {
        this.practiceQueryService = practiceQueryService;
    }

    @GetMapping
    public ApiResponse<PracticeListPage> listPractices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String practiceNumber,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String openedFrom,
            @RequestParam(required = false) String openedTo,
            @RequestParam(required = false) String closedFrom,
            @RequestParam(required = false) String closedTo,
            @RequestParam(required = false) String lastModifiedFrom,
            @RequestParam(required = false) String lastModifiedTo,
            @RequestParam(required = false) String sdOutcome
    ) {
        PracticeListQuery query = new PracticeListQuery(
                page,
                size,
                sort,
                practiceNumber,
                state,
                openedFrom,
                openedTo,
                closedFrom,
                closedTo,
                lastModifiedFrom,
                lastModifiedTo,
                sdOutcome
        );
        return ApiResponse.ok(practiceQueryService.getPracticeList(query));
    }

        @GetMapping(path = "/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        public ResponseEntity<?> exportPractices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String practiceNumber,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String openedFrom,
            @RequestParam(required = false) String openedTo,
            @RequestParam(required = false) String closedFrom,
            @RequestParam(required = false) String closedTo,
            @RequestParam(required = false) String lastModifiedFrom,
            @RequestParam(required = false) String lastModifiedTo,
            @RequestParam(required = false) String sdOutcome
        ) {
        try {
            PracticeListQuery query = new PracticeListQuery(
                page,
                size,
                sort,
                practiceNumber,
                state,
                openedFrom,
                openedTo,
                closedFrom,
                closedTo,
                lastModifiedFrom,
                lastModifiedTo,
                sdOutcome
            );
            byte[] body = practiceQueryService.exportPracticeListExcel(query);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pratiche-anc.xlsx")
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(body);
        } catch (IllegalStateException ex) {
            return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(2505, "Errore generazione export Excel"));
        }
        }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PracticeDetailResponse>> getPracticeDetail(@PathVariable Long id) {
        Optional<PracticeDetailResponse> detail = practiceQueryService.getPracticeDetail(id);
        if (detail.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error(2004, "Pratica non trovata"));
        }
        return ResponseEntity.ok(ApiResponse.ok(detail.get()));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<java.util.List<PracticeHistoryItem>>> getPracticeHistory(@PathVariable Long id) {
        if (!practiceQueryService.practiceExists(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error(2004, "Pratica non trovata"));
        }
        return ResponseEntity.ok(ApiResponse.ok(practiceQueryService.getPracticeHistory(id)));
    }

    @GetMapping("/{id}/states")
    public ResponseEntity<ApiResponse<java.util.List<PracticeStateItem>>> getPracticeStates(@PathVariable Long id) {
        if (!practiceQueryService.practiceExists(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error(2004, "Pratica non trovata"));
        }
        return ResponseEntity.ok(ApiResponse.ok(practiceQueryService.getPracticeStates(id)));
    }

    @GetMapping("/{id}/related-actions")
    public ResponseEntity<ApiResponse<java.util.List<PracticeRelatedActionItem>>> getRelatedActions(@PathVariable Long id) {
        if (!practiceQueryService.practiceExists(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error(2004, "Pratica non trovata"));
        }
        return ResponseEntity.ok(ApiResponse.ok(practiceQueryService.getRelatedActions(id)));
    }
}