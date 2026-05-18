package it.poste.anc.bpmgw.outbound;

public interface BpmOutcomeOutboundGateway {

    String sendOutcome(String payloadJson);
}
