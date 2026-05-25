package it.poste.anc.bpmgw.inbound;

import it.poste.anc.shared.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/bpm", produces = MediaType.APPLICATION_JSON_VALUE)
public class BpmOutcomeAckController {

    private final BpmOutcomeAckService bpmOutcomeAckService;

    public BpmOutcomeAckController(BpmOutcomeAckService bpmOutcomeAckService) {
        this.bpmOutcomeAckService = bpmOutcomeAckService;
    }

    @PostMapping(path = "/outcome-ack", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<BpmOutcomeAckResponse>> receiveOutcomeAck(@RequestBody BpmOutcomeAckRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(bpmOutcomeAckService.receiveAck(request)));
        } catch (BpmAckOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }
}
