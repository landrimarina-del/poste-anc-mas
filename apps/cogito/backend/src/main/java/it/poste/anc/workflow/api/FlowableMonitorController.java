package it.poste.anc.workflow.api;

import it.poste.anc.shared.common.ApiResponse;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Dashboard di monitoring Flowable per il POC ANC.
 * Espone statistiche su processi, istanze attive e task tramite la Flowable Java API.
 */
@RestController
@RequestMapping(path = "/api/v1/monitor/flowable", produces = MediaType.APPLICATION_JSON_VALUE)
public class FlowableMonitorController {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;

    public FlowableMonitorController(
            RepositoryService repositoryService,
            RuntimeService runtimeService,
            TaskService taskService,
            HistoryService historyService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
    }

    /**
     * Riepilogo globale: definizioni, istanze attive, task aperti, storici completati.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> summary() {
        long activeInstances = runtimeService.createProcessInstanceQuery().count();
        long openTasks = taskService.createTaskQuery().count();
        long completedInstances = historyService.createHistoricProcessInstanceQuery().finished().count();
        long processDefinitions = repositoryService.createProcessDefinitionQuery().count();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("processDefinitions", processDefinitions);
        data.put("activeInstances", activeInstances);
        data.put("openTasks", openTasks);
        data.put("completedInstances", completedInstances);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * Lista le definizioni di processo deployate.
     */
    @GetMapping("/process-definitions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> processDefinitions() {
        List<ProcessDefinition> defs = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion().desc()
                .list();
        List<Map<String, Object>> result = defs.stream().map(d -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("key", d.getKey());
            m.put("name", d.getName());
            m.put("version", d.getVersion());
            m.put("deploymentId", d.getDeploymentId());
            m.put("suspended", d.isSuspended());
            return m;
        }).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Lista le istanze di processo attive.
     */
    @GetMapping("/instances")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> activeInstances(
            @RequestParam(name = "processKey", required = false) String processKey,
            @RequestParam(name = "businessKey", required = false) String businessKey) {

        var query = runtimeService.createProcessInstanceQuery().orderByStartTime().desc();
        if (processKey != null && !processKey.isBlank()) {
            query.processDefinitionKey(processKey);
        }
        if (businessKey != null && !businessKey.isBlank()) {
            query.processInstanceBusinessKey(businessKey);
        }

        List<ProcessInstance> instances = query.list();
        List<Map<String, Object>> result = instances.stream().map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", i.getId());
            m.put("processDefinitionId", i.getProcessDefinitionId());
            m.put("processDefinitionKey", i.getProcessDefinitionKey());
            m.put("processDefinitionName", i.getProcessDefinitionName());
            m.put("businessKey", i.getBusinessKey());
            m.put("startTime", formatDate(i.getStartTime()));
            m.put("suspended", i.isSuspended());
            return m;
        }).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Lista i task aperti, opzionalmente filtrati per processo o assegnatario.
     */
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> openTasks(
            @RequestParam(name = "processInstanceId", required = false) String processInstanceId,
            @RequestParam(name = "assignee", required = false) String assignee) {

        var query = taskService.createTaskQuery().orderByTaskCreateTime().desc();
        if (processInstanceId != null && !processInstanceId.isBlank()) {
            query.processInstanceId(processInstanceId);
        }
        if (assignee != null && !assignee.isBlank()) {
            query.taskAssignee(assignee);
        }

        List<Task> tasks = query.list();
        List<Map<String, Object>> result = tasks.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("assignee", t.getAssignee());
            m.put("processInstanceId", t.getProcessInstanceId());
            m.put("processDefinitionId", t.getProcessDefinitionId());
            m.put("createTime", formatDate(t.getCreateTime()));
            m.put("dueDate", formatDate(t.getDueDate()));
            m.put("priority", t.getPriority());
            return m;
        }).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Storico processi completati (ultimi N, default 50).
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> history(
            @RequestParam(name = "processKey", required = false) String processKey,
            @RequestParam(name = "businessKey", required = false) String businessKey,
            @RequestParam(name = "limit", defaultValue = "50") int limit) {

        var query = historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime().desc();
        if (processKey != null && !processKey.isBlank()) {
            query.processDefinitionKey(processKey);
        }
        if (businessKey != null && !businessKey.isBlank()) {
            query.processInstanceBusinessKey(businessKey);
        }

        List<HistoricProcessInstance> history = query.listPage(0, Math.min(limit, 200));
        List<Map<String, Object>> result = history.stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", h.getId());
            m.put("processDefinitionKey", h.getProcessDefinitionKey());
            m.put("processDefinitionName", h.getProcessDefinitionName());
            m.put("businessKey", h.getBusinessKey());
            m.put("startTime", formatDate(h.getStartTime()));
            m.put("endTime", formatDate(h.getEndTime()));
            m.put("deleteReason", h.getDeleteReason());
            m.put("durationInMillis", h.getDurationInMillis());
            boolean completed = h.getEndTime() != null;
            m.put("state", completed ? "COMPLETED" : "ACTIVE");
            return m;
        }).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Dettaglio variabili di un'istanza di processo attiva.
     */
    @GetMapping("/instances/{instanceId}/variables")
    public ResponseEntity<ApiResponse<Map<String, Object>>> instanceVariables(
            @PathVariable String instanceId) {
        Map<String, Object> vars = runtimeService.getVariables(instanceId);
        return ResponseEntity.ok(ApiResponse.ok(vars));
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
