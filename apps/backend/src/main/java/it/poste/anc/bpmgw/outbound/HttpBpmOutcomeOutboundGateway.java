package it.poste.anc.bpmgw.outbound;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpBpmOutcomeOutboundGateway implements BpmOutcomeOutboundGateway {

    private final RestClient restClient;
    private final String outcomeUrl;

    public HttpBpmOutcomeOutboundGateway(
            @Value("${bpm.base-url:http://bpm-outbound-stub:8090}") String bpmBaseUrl,
            @Value("${bpm.receive-outcome-path:/receive-outcome}") String receiveOutcomePath
    ) {
        this.restClient = RestClient.create();
        this.outcomeUrl = bpmBaseUrl + receiveOutcomePath;
    }

    @Override
    public String sendOutcome(String payloadJson) {
        return restClient.post()
                .uri(outcomeUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadJson)
                .retrieve()
                .body(String.class);
    }
}
