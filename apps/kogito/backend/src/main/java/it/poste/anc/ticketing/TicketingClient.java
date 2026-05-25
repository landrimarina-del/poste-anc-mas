package it.poste.anc.ticketing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client verso il sistema di ticketing esterno (mock in POC).
 * Sprint 11 — GAP-US-01.
 */
@Component
public class TicketingClient {

    private static final Logger log = LoggerFactory.getLogger(TicketingClient.class);

    private final RestClient restClient;
    private final String openTicketUrl;
    private final ObjectMapper objectMapper;

    public TicketingClient(
            @Value("${ticketing.base-url:http://localhost:18080}") String baseUrl,
            @Value("${ticketing.open-ticket-path:/ticketing/open-ticket}") String openTicketPath,
            ObjectMapper objectMapper
    ) {
        this.restClient = RestClient.create();
        this.openTicketUrl = baseUrl + openTicketPath;
        this.objectMapper = objectMapper;
    }

    /**
     * Apre un ticket nel sistema di ticketing.
     * In caso di errore logga WARN e ritorna null senza rilanciare.
     *
     * @param idWorkItem ID del work item BPM
     * @param canale     canale di provenienza della pratica
     * @return ticketId oppure null se la chiamata fallisce
     */
    public String openTicket(String idWorkItem, String canale) {
        try {
            Map<String, String> bodyMap = new LinkedHashMap<>();
            bodyMap.put("idWorkItem", idWorkItem);
            bodyMap.put("canale", canale);
            String body = objectMapper.writeValueAsString(bodyMap);

            String response = restClient.post()
                    .uri(openTicketUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            if (response == null) {
                log.warn("Risposta vuota da ticketing per idWorkItem={}", idWorkItem);
                return null;
            }

            JsonNode node = objectMapper.readTree(response);
            JsonNode ticketIdNode = node.get("ticketId");
            if (ticketIdNode == null || ticketIdNode.isNull()) {
                log.warn("Campo ticketId assente nella risposta ticketing per idWorkItem={}", idWorkItem);
                return null;
            }
            return ticketIdNode.asText();
        } catch (Exception ex) {
            log.warn("Errore chiamata ticketing per idWorkItem={}: {}", idWorkItem, ex.getMessage());
            return null;
        }
    }
}
