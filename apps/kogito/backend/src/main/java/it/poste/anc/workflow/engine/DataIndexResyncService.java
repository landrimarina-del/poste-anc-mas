package it.poste.anc.workflow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Pubblica al Data Index le istanze di processo esistenti al momento dell'avvio.
 *
 * <p>Il Data Index non ha memoria persistente degli eventi: ogni volta che il
 * container viene riavviato, il suo DB PostgreSQL viene ricreato da zero
 * (QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=create). Questo componente
 * risolve il problema ripubblicando tutte le istanze attive all'avvio.
 *
 * <p>Viene eseguito in modo asincrono dopo che il contesto Spring è pronto,
 * per non rallentare lo startup del backend.
 */
@Service
public class DataIndexResyncService {

    private static final Logger log = LoggerFactory.getLogger(DataIndexResyncService.class);

    private static final List<String> PROCESS_KEYS = List.of("anc_pratica");

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private final String dataIndexUrl;
    private final String serviceUrl;
    private final RestTemplate restTemplate;

    public DataIndexResyncService(
            ApplicationContext applicationContext,
            ObjectMapper objectMapper,
            @Value("${kogito.dataindex.http.url:}") String dataIndexUrl,
            @Value("${kogito.service.url:http://localhost:8081}") String serviceUrl) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
        this.dataIndexUrl = dataIndexUrl;
        this.serviceUrl = serviceUrl;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Al termine dello startup Spring, ripubblica tutte le process instances
     * attive verso il Data Index.
     * Eseguito asincronamente per non bloccare lo startup.
     */
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void resyncOnStartup() {
        if (dataIndexUrl == null || dataIndexUrl.isBlank()) {
            log.info("DataIndexResyncService: dataIndexUrl non configurato, skip resync.");
            return;
        }
        log.info("DataIndexResyncService: avvio resync verso {}", dataIndexUrl);

        // Attesa breve per permettere al Data Index di essere completamente avviato
        try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        int total = 0;
        int errors = 0;
        for (String processKey : PROCESS_KEYS) {
            try {
                ensureProcessDefinition(processKey);
                total += resyncProcess(processKey);
            } catch (Exception e) {
                log.warn("DataIndexResyncService: errore resync processo '{}': {}", processKey, e.getMessage());
                errors++;
            }
        }
        log.info("DataIndexResyncService: resync completato. Pubblicati={} errori-processo={}", total, errors);
    }

    /**
     * Assicura che il process definition esista nel Data Index via POST /definitions.
     * L'endpoint /definitions è il path HTTP corretto (confermato con http-events-support
     * in Apache KIE Kogito Data Index 10.2.0 — non /processdefinitions che ritorna 404).
     */
    private void ensureProcessDefinition(String processKey) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", processKey);
        data.put("name", processKey);
        data.put("version", "1.0");
        data.put("type", "SW");
        data.put("endpoint", serviceUrl + "/" + processKey);
        data.put("addons", List.of());
        data.put("annotations", List.of());
        data.put("nodes", List.of());
        data.put("roles", List.of());

        Map<String, Object> cloudEvent = new HashMap<>();
        cloudEvent.put("specversion", "1.0");
        cloudEvent.put("type", "ProcessDefinitionEvent");
        cloudEvent.put("source", serviceUrl + "/" + processKey);
        cloudEvent.put("id", UUID.randomUUID().toString());
        cloudEvent.put("time", Instant.now().toString());
        cloudEvent.put("datacontenttype", "application/json");
        cloudEvent.put("kogitoprocid", processKey);
        cloudEvent.put("data", data);

        try {
            String payload = objectMapper.writeValueAsString(cloudEvent);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/cloudevents+json"));
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(dataIndexUrl + "/definitions", entity, Void.class);
            log.info("DataIndexResyncService: ProcessDefinition '{}' pubblicato via /definitions", processKey);
        } catch (Exception e) {
            log.warn("DataIndexResyncService: impossibile pubblicare ProcessDefinition '{}': {}", processKey, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private int resyncProcess(String processKey) {
        Process<? extends Model> process;
        try {
            process = applicationContext.getBean(processKey, Process.class);
        } catch (Exception e) {
            log.debug("Processo '{}' non trovato nel contesto Spring, skip.", processKey);
            return 0;
        }

        List<? extends ProcessInstance<? extends Model>> instances;
        try {
            instances = process.instances().stream().collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Impossibile leggere le istanze del processo '{}': {}", processKey, e.getMessage());
            return 0;
        }

        int published = 0;
        for (ProcessInstance<? extends Model> instance : instances) {
            try {
                publishProcessInstanceState(processKey, instance);
                published++;
            } catch (Exception e) {
                log.warn("Impossibile pubblicare istanza '{}' del processo '{}': {}",
                        instance.id(), processKey, e.getMessage());
            }
        }
        log.info("DataIndexResyncService: resync '{}' — {} istanze pubblicate", processKey, published);
        return published;
    }

    private void publishProcessInstanceState(String processKey, ProcessInstance<? extends Model> instance) {
        String instanceId = instance.id();
        int state = instance.status(); // 1=ACTIVE, 2=COMPLETED, 3=ABORTED

        // Costruisce il data payload del CloudEvent ProcessInstanceState
        Map<String, Object> data = new HashMap<>();
        data.put("id", instanceId);
        data.put("processId", processKey);
        data.put("processName", processKey);
        data.put("processVersion", "1.0");
        data.put("state", state);
        data.put("businessKey", instance.businessKey());
        data.put("start", Instant.now().toString());
        data.put("end", null);
        data.put("nodes", List.of());
        data.put("variables", Map.of());
        data.put("milestones", List.of());
        data.put("roles", List.of());
        data.put("error", null);
        data.put("slaDueDate", null);

        // Costruisce il CloudEvent envelope
        // Il type deve contenere "ProcessInstanceState" per essere riconosciuto dal Data Index
        Map<String, Object> cloudEvent = new HashMap<>();
        cloudEvent.put("specversion", "1.0");
        cloudEvent.put("type", "ProcessInstanceStateDataEvent");
        cloudEvent.put("source", serviceUrl + "/" + processKey);
        cloudEvent.put("id", UUID.randomUUID().toString());
        cloudEvent.put("time", Instant.now().toString());
        cloudEvent.put("datacontenttype", "application/json");
        cloudEvent.put("kogitoprocid", processKey);
        cloudEvent.put("kogitoprocinstanceid", instanceId);
        cloudEvent.put("kogitobusinesskey", instance.businessKey());
        cloudEvent.put("data", data);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(cloudEvent);
        } catch (Exception e) {
            throw new RuntimeException("Serializzazione CloudEvent fallita: " + e.getMessage(), e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/cloudevents+json"));
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        String url = dataIndexUrl + "/processes";

        restTemplate.postForEntity(url, entity, Void.class);
        log.debug("DataIndexResyncService: pubblicata istanza '{}' (state={}) → {}", instanceId, state, url);
    }
}
