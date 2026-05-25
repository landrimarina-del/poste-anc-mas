package it.poste.anc.workflow.api;

import it.poste.anc.shared.common.ApiResponse;
import org.kie.kogito.process.Process;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint tecnico di readiness per validare baseline Kogito.
 * Controlla che il bean del processo "anc_pratica" sia stato generato e registrato
 * dal kogito-maven-plugin a compile-time.
 */
@RestController
@RequestMapping("/api/v1/technical/workflow")
public class WorkflowReadinessController {

    private static final String ANC_PROCESS_KEY = "anc_pratica";
    private static final String KOGITO_PROCESS_CLASS = "org.kie.kogito.process.Process";

    private final ApplicationContext applicationContext;

    public WorkflowReadinessController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping("/readiness")
    public ResponseEntity<ApiResponse<Map<String, Object>>> readiness() {
        Map<String, Object> details = new LinkedHashMap<>();

        boolean kogitoOnClasspath = isClassPresent(KOGITO_PROCESS_CLASS);
        details.put("engine", "Kogito");
        details.put("kogitoOnClasspath", kogitoOnClasspath);

        if (!kogitoOnClasspath) {
            details.put("engineActive", false);
            details.put("ancPraticaProcessDeployed", false);
            details.put("fallback", "Kogito non disponibile sul classpath");
            return ResponseEntity.ok(ApiResponse.ok(details));
        }

        boolean ancPraticaBeanPresent = applicationContext.containsBean(ANC_PROCESS_KEY);
        details.put("engineActive", ancPraticaBeanPresent);
        details.put("ancPraticaProcessDeployed", ancPraticaBeanPresent);

        if (ancPraticaBeanPresent) {
            try {
                @SuppressWarnings("rawtypes")
                Process<?> process = (Process<?>) applicationContext.getBean(ANC_PROCESS_KEY);
                details.put("processId", process.id());
                details.put("processVersion", process.version());
            } catch (Exception ex) {
                details.put("processIntrospectionError", ex.getClass().getSimpleName());
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(details));
    }

    private boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
