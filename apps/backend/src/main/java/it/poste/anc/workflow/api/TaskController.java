package it.poste.anc.workflow.api;

import it.poste.anc.shared.common.ApiResponse;
import it.poste.anc.workflow.application.TaskManagementService;
import it.poste.anc.workflow.application.TaskOperationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskController {

    private final TaskManagementService taskManagementService;

    public TaskController(TaskManagementService taskManagementService) {
        this.taskManagementService = taskManagementService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskListItem>>> listTasks(
            @RequestParam(name = "practiceNumber", required = false) String practiceNumber,
            @RequestParam(name = "taskState", required = false) String taskState,
            Authentication authentication) {
        try {
            List<TaskListItem> tasks = taskManagementService.listTasksForCurrentOperator(
                    authentication.getName(),
                    practiceNumber,
                    taskState
            );
            return ResponseEntity.ok(ApiResponse.ok(tasks));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<TaskAcceptResponse>> acceptTask(@PathVariable("id") Long taskId,
                                                                       Authentication authentication) {
        try {
            TaskAcceptResponse details = taskManagementService.acceptTask(taskId, authentication.getName());
            return ResponseEntity.ok(ApiResponse.ok(details));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }
}
