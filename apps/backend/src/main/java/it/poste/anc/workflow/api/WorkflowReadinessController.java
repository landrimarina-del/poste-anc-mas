package it.poste.anc.workflow.api;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.poste.anc.shared.common.ApiResponse;

/**
 * Endpoint tecnico di readiness per validare baseline Flowable nello Sprint 0.
 */
@RestController
@RequestMapping("/api/v1/technical/workflow")
public class WorkflowReadinessController {

    private static final String PLACEHOLDER_PROCESS_KEY = "tech.foundation.placeholder";
    private static final String FLOWABLE_PROCESS_ENGINE_CLASS = "org.flowable.engine.ProcessEngine";

    private final ApplicationContext applicationContext;

    public WorkflowReadinessController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping("/readiness")
    public ResponseEntity<ApiResponse<Map<String, Object>>> readiness() {
        boolean flowableOnClasspath = ClassUtils.isPresent(
                FLOWABLE_PROCESS_ENGINE_CLASS,
                getClass().getClassLoader()
        );

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("flowableOnClasspath", flowableOnClasspath);

        if (!flowableOnClasspath || !applicationContext.containsBean("processEngine")
                || !applicationContext.containsBean("repositoryService")) {
            details.put("engineActive", false);
            details.put("engineName", "N/A");
            details.put("engineVersion", "N/A");
            details.put("deployedProcessDefinitions", 0L);
            details.put("placeholderProcessDeployed", false);
            details.put("fallback", "Flowable non disponibile: readiness tecnica in modalita non bloccante Sprint 0");
            return ResponseEntity.ok(ApiResponse.ok(details));
        }

        try {
            Object processEngine = applicationContext.getBean("processEngine");
            Object repositoryService = applicationContext.getBean("repositoryService");

            long deployedDefinitions = countAllDefinitions(repositoryService);
            long placeholderDefinitions = countDefinitionsByKey(repositoryService, PLACEHOLDER_PROCESS_KEY);

            details.put("engineActive", true);
            details.put("engineName", invokeString(processEngine, "getName"));
            details.put("engineVersion", resolveEngineVersion(processEngine));
            details.put("deployedProcessDefinitions", deployedDefinitions);
            details.put("placeholderProcessDeployed", placeholderDefinitions > 0L);
        } catch (ReflectiveOperationException ex) {
            details.put("engineActive", false);
            details.put("engineName", "N/A");
            details.put("engineVersion", "N/A");
            details.put("deployedProcessDefinitions", 0L);
            details.put("placeholderProcessDeployed", false);
            details.put("fallback", "Flowable presente ma introspezione non riuscita: " + ex.getClass().getSimpleName());
        }

        return ResponseEntity.ok(ApiResponse.ok(details));
    }

    private long countAllDefinitions(Object repositoryService) throws ReflectiveOperationException {
        Object query = invoke(repositoryService, "createProcessDefinitionQuery");
        return invokeLong(query, "count");
    }

    private long countDefinitionsByKey(Object repositoryService, String key) throws ReflectiveOperationException {
        Object query = invoke(repositoryService, "createProcessDefinitionQuery");
        Object filteredQuery = query.getClass().getMethod("processDefinitionKey", String.class).invoke(query, key);
        return invokeLong(filteredQuery, "count");
    }

    private String resolveEngineVersion(Object processEngine) {
        try {
            return String.valueOf(processEngine.getClass().getField("VERSION").get(null));
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            return "UNKNOWN";
        }
    }

    private String invokeString(Object target, String methodName) throws ReflectiveOperationException {
        return String.valueOf(invoke(target, methodName));
    }

    private long invokeLong(Object target, String methodName) throws ReflectiveOperationException {
        Object value = invoke(target, methodName);
        return ((Number) value).longValue();
    }

    private Object invoke(Object target, String methodName) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }
}
