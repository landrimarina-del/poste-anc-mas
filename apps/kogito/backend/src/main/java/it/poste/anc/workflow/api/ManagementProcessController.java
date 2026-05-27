package it.poste.anc.workflow.api;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoint richiesti dalla Kogito Management Console per il drill-down
 * del dettaglio di un'istanza di processo.
 *
 * <p>La Management Console (1.44.x) chiama:
 * <ul>
 *   <li>GET /management/processes/{processId}/source  → sorgente BPMN (XML)</li>
 *   <li>GET /management/processes/{processId}/nodes   → elenco nodi del processo</li>
 * </ul>
 *
 * <p>Nel runtime Spring Boot di Kogito questi endpoint NON vengono generati
 * automaticamente (a differenza del runtime Quarkus), quindi li esponiamo
 * manualmente basandoci sui file BPMN2 nel classpath.
 *
 * <p>I file BPMN sono in {@code src/main/resources/processes/}
 * e vengono inclusi nel JAR sotto {@code BOOT-INF/classes/processes/}.
 */
@RestController
@RequestMapping("/management/processes")
public class ManagementProcessController {

    /** Mapping processId → nome file BPMN2 nel classpath. */
    private static final Map<String, String> PROCESS_BPMN_FILES = Map.of(
            "anc_pratica", "processes/anc_pratica.bpmn2"
    );

    /**
     * Nodi statici del processo ANC.
     * Devono corrispondere agli id degli elementi BPMN2.
     * La console usa questi dati per evidenziare il nodo corrente sul diagramma.
     */
    private static final List<Map<String, Object>> ANC_PRATICA_NODES = buildAncPraticaNodes();

    private final ApplicationContext applicationContext;

    public ManagementProcessController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // ------------------------------------------------------------------
    // GET /management/processes/{processId}/source
    // ------------------------------------------------------------------

    /**
     * Restituisce il sorgente BPMN2 del processo richiesto come XML.
     * La Management Console usa questo endpoint per renderizzare il diagramma
     * SVG del processo nel pannello di dettaglio.
     */
    @GetMapping(value = "/{processId}/source", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getProcessSource(@PathVariable String processId) {
        String bpmnFile = PROCESS_BPMN_FILES.get(processId);
        if (bpmnFile == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            ClassPathResource resource = new ClassPathResource(bpmnFile);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String content = FileCopyUtils.copyToString(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ------------------------------------------------------------------
    // GET /management/processes/{processId}/nodes
    // ------------------------------------------------------------------

    /**
     * Restituisce la lista dei nodi del processo nel formato atteso dalla
     * Management Console 1.44.x:
     * <pre>
     * [
     *   { "nodeDefinitionId": "_id", "name": "...", "type": "...", "uniqueId": "_id" },
     *   ...
     * ]
     * </pre>
     */
    @GetMapping(value = "/{processId}/nodes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getProcessNodes(@PathVariable String processId) {
        if (!PROCESS_BPMN_FILES.containsKey(processId)) {
            return ResponseEntity.notFound().build();
        }
        if ("anc_pratica".equals(processId)) {
            return ResponseEntity.ok(ANC_PRATICA_NODES);
        }
        // Per altri processi futuri: restituire lista vuota (console non mostra diagram)
        return ResponseEntity.ok(List.of());
    }

    // ------------------------------------------------------------------
    // Costruzione statica nodi ANC_PRATICA
    // ------------------------------------------------------------------

    private static List<Map<String, Object>> buildAncPraticaNodes() {
        List<Map<String, Object>> nodes = new ArrayList<>();

        nodes.add(nodeEntry("start",              "Pratica Aperta",    "StartNode"));
        nodes.add(nodeEntry("task_lavorazione",   "Lavorazione Pratica", "HumanTaskNode"));
        nodes.add(nodeEntry("end_pratica",        "Pratica Chiusa",    "EndNode"));

        return nodes;
    }

    private static Map<String, Object> nodeEntry(String id, String name, String type) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("nodeDefinitionId", "_" + id);
        node.put("name", name);
        node.put("type", type);
        node.put("uniqueId", "_" + id);
        return node;
    }
}
