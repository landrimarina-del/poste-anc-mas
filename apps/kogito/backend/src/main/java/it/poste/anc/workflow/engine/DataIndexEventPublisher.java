package it.poste.anc.workflow.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventPublisher;
import org.kie.kogito.event.process.ProcessInstanceDataEvent;
import org.kie.kogito.event.process.UserTaskInstanceDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Pubblica gli eventi di processo Kogito verso il Data Index via HTTP.
 *
 * <p>Viene raccolta automaticamente da {@code ProcessConfig} (generato da Kogito)
 * che la inietta nell'EventManager tramite {@code List<EventPublisher>}.
 *
 * <p>Il Data Index (http-events-support profile) espone:
 * <ul>
 *   <li>POST /processes — ProcessInstanceDataEvent</li>
 *   <li>POST /tasks    — UserTaskInstanceDataEvent</li>
 * </ul>
 */
@Component
public class DataIndexEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DataIndexEventPublisher.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String dataIndexUrl;
    private final String serviceUrl;

    public DataIndexEventPublisher(
            ObjectMapper objectMapper,
            @Value("${kogito.dataindex.http.url:}") String dataIndexUrl,
            @Value("${kogito.service.url:http://localhost:8081}") String serviceUrl) {
        this.objectMapper = objectMapper;
        this.dataIndexUrl = dataIndexUrl;
        this.serviceUrl = serviceUrl;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
        log.info("DataIndexEventPublisher created, Data Index URL: {}, Service URL: {}", dataIndexUrl, serviceUrl);
    }

    @Override
    public void publish(DataEvent<?> event) {
        if (dataIndexUrl == null || dataIndexUrl.isBlank()) {
            log.debug("Data Index URL not configured, skipping event publish");
            return;
        }

        String endpoint = resolveEndpoint(event);
        if (endpoint == null) {
            log.debug("No Data Index endpoint for event type: {}", event.getClass().getSimpleName());
            return;
        }

        // Fire-and-forget: non bloccare il thread del processo
        CompletableFuture.runAsync(() -> doPublish(event, endpoint));
    }

    private void doPublish(DataEvent<?> event, String endpoint) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            // Data Index 1.44.1 chiama event.getSource() (URI) per costruire l'endpoint REST.
            // Nel vecchio Kogito 2.44.0.Alpha il campo 'source' può risultare null se
            // kogito.service.url non è propagato nell'evento: patch esplicita.
            if (event instanceof UserTaskInstanceDataEvent) {
                JsonNode node = objectMapper.readTree(payload);

                // Data Index 1.44.1 chiama getEndpoint(source, processInstanceId, taskName, taskId)
                // e fa escaper.escape(taskName) — Guava lancia NPE se taskName è null.
                // Il vecchio Kogito 2.44.0.Alpha non popola taskName; usiamo referenceName come fallback.
                boolean modified = false;
                ObjectNode obj = (ObjectNode) node;

                JsonNode dataNode = node.path("data");
                if (dataNode.isObject()) {
                    ObjectNode data = (ObjectNode) dataNode;
                    if (data.path("taskName").isNull() || data.path("taskName").isMissingNode()) {
                        String refName = data.path("referenceName").asText(null);
                        data.put("taskName", refName != null ? refName : "");
                        modified = true;
                        log.info("Patched null taskName → '{}'", refName);
                    }
                }

                // Patch source se per qualsiasi motivo fosse null/blank
                String src = node.has("source") ? node.get("source").asText(null) : null;
                if (src == null || src.isBlank() || "null".equals(src)) {
                    obj.put("source", serviceUrl);
                    modified = true;
                    log.info("Patched null source → {}", serviceUrl);
                }

                if (modified) {
                    payload = objectMapper.writeValueAsString(obj);
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/cloudevents+json"));
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            String url = dataIndexUrl + endpoint;
            restTemplate.postForEntity(url, entity, Void.class);
            log.info("Published {} to Data Index: {}", event.getClass().getSimpleName(), url);
        } catch (Exception e) {
            log.warn("Failed to publish event {} to Data Index: {}", event.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void publish(Collection<DataEvent<?>> events) {
        events.forEach(this::publish);
    }

    private String resolveEndpoint(DataEvent<?> event) {
        if (event instanceof ProcessInstanceDataEvent) {
            return "/processes";
        }
        if (event instanceof UserTaskInstanceDataEvent) {
            return "/tasks";
        }
        return null;
    }
}


