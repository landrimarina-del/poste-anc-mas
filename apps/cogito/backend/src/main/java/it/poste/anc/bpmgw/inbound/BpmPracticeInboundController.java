package it.poste.anc.bpmgw.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import it.poste.anc.shared.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/bpm/practices", produces = MediaType.APPLICATION_JSON_VALUE)
public class BpmPracticeInboundController {

    private final BpmPracticeInboundService inboundService;

    public BpmPracticeInboundController(BpmPracticeInboundService inboundService) {
        this.inboundService = inboundService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<BpmPracticeOpenResponse> openPractice(@RequestBody(required = false) JsonNode payload) {
        return inboundService.openPractice(payload);
    }
}