package it.poste.anc.signals.api;

import it.poste.anc.shared.common.ApiResponse;
import it.poste.anc.signals.application.SignalOperationException;
import it.poste.anc.signals.application.SignalService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/signals", produces = MediaType.APPLICATION_JSON_VALUE)
public class SignalController {

    private final SignalService signalService;

    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SignalCreateResponse>> createSignal(
            @Valid @RequestBody SignalCreateRequest request,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    signalService.createSignal(authentication.getName(), request)
            ));
        } catch (SignalOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @GetMapping(path = "/me")
    public ResponseEntity<ApiResponse<List<SignalListItem>>> listMySignals(
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    signalService.listMySignals(authentication.getName(), state, fromDate, toDate)
            ));
        } catch (SignalOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SignalListItem>>> listSignals(
            @RequestParam(name = "id", required = false) Long signalId,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "operator", required = false) String operator,
            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    signalService.listSignalsForSupervisor(
                            authentication.getName(),
                            signalId,
                            state,
                            operator,
                            fromDate,
                            toDate
                    )
            ));
        } catch (SignalOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @PostMapping(path = "/{id}/reassign", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SignalReassignResponse>> reassignSignal(
            @PathVariable("id") Long signalId,
            @Valid @RequestBody SignalReassignRequest request,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    signalService.reassignSignal(signalId, authentication.getName(), request)
            ));
        } catch (SignalOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @PostMapping(path = "/{id}/forward-sinergia")
    public ResponseEntity<ApiResponse<SignalForwardResponse>> forwardToSinergia(
            @PathVariable("id") Long signalId,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    signalService.forwardToSinergia(signalId, authentication.getName())
            ));
        } catch (SignalOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }
}
