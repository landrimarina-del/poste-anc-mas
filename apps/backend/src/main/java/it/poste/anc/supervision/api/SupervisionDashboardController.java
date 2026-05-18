package it.poste.anc.supervision.api;

import it.poste.anc.shared.common.ApiResponse;
import it.poste.anc.supervision.application.SupervisionDashboardService;
import it.poste.anc.workflow.application.TaskOperationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/supervision/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
public class SupervisionDashboardController {

    private final SupervisionDashboardService supervisionDashboardService;

    public SupervisionDashboardController(SupervisionDashboardService supervisionDashboardService) {
        this.supervisionDashboardService = supervisionDashboardService;
    }

    @GetMapping("/counters")
    public ResponseEntity<ApiResponse<SupervisionDashboardCountersResponse>> counters(Authentication authentication) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    supervisionDashboardService.loadCounters(authentication.getName())
            ));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @GetMapping("/daily-opened")
    public ResponseEntity<ApiResponse<List<SupervisionDailyOpenedPoint>>> dailyOpened(
            @RequestParam("month") String month,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    supervisionDashboardService.loadDailyOpened(authentication.getName(), parseMonth(month))
            ));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @GetMapping("/daily-worked")
    public ResponseEntity<ApiResponse<List<SupervisionDailyWorkedPoint>>> dailyWorked(
            @RequestParam("month") String month,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    supervisionDashboardService.loadDailyWorked(authentication.getName(), parseMonth(month))
            ));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @GetMapping("/by-state")
    public ResponseEntity<ApiResponse<List<SupervisionPracticeByStatePoint>>> byState(Authentication authentication) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    supervisionDashboardService.loadPracticesByState(authentication.getName())
            ));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    private YearMonth parseMonth(String value) {
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException ex) {
            throw new TaskOperationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    6102,
                    "Parametro month non valido: formato atteso YYYY-MM"
            );
        }
    }
}
