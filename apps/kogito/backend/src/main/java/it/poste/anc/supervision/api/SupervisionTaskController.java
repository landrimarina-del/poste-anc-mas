package it.poste.anc.supervision.api;

import it.poste.anc.shared.common.ApiResponse;
import it.poste.anc.supervision.application.SupervisionTaskService;
import it.poste.anc.workflow.application.TaskOperationException;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/supervision/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
public class SupervisionTaskController {

    private final SupervisionTaskService supervisionTaskService;

    public SupervisionTaskController(SupervisionTaskService supervisionTaskService) {
        this.supervisionTaskService = supervisionTaskService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupervisionTaskListItem>>> listTasks(
            @RequestParam(name = "practiceNumber", required = false) String practiceNumber,
            @RequestParam(name = "assignmentDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assignmentDate,
            @RequestParam(name = "owner", required = false) String owner,
            @RequestParam(name = "assigneeGroup", required = false) String assigneeGroup,
            Authentication authentication
    ) {
        try {
            List<SupervisionTaskListItem> details = supervisionTaskService.listSupervisionTasks(
                    authentication.getName(),
                    practiceNumber,
                    assignmentDate,
                    owner,
                    assigneeGroup
            );
            return ResponseEntity.ok(ApiResponse.ok(details));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @GetMapping("/operators")
    public ResponseEntity<ApiResponse<List<String>>> listOperators(Authentication authentication) {
        try {
            List<String> operators = supervisionTaskService.listOperatorUsernames(authentication.getName());
            return ResponseEntity.ok(ApiResponse.ok(operators));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

        @PostMapping(path = "/{id}/reassign-group")
    public ResponseEntity<ApiResponse<SupervisionTaskReassignResponse>> reassignToGroup(
            @PathVariable("id") Long taskId,
            @RequestBody(required = false) SupervisionTaskReassignGroupRequest request,
            Authentication authentication
    ) {
        try {
            SupervisionTaskReassignGroupRequest payload = request != null
                    ? request
                    : new SupervisionTaskReassignGroupRequest(null);
            return ResponseEntity.ok(ApiResponse.ok(
                    supervisionTaskService.reassignToOperatorGroup(taskId, authentication.getName(), payload.reason())
            ));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @PostMapping(path = "/{id}/reassign-user", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SupervisionTaskReassignResponse>> reassignToUser(
            @PathVariable("id") Long taskId,
            @Valid @RequestBody SupervisionTaskReassignUserRequest request,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    supervisionTaskService.reassignToUser(taskId, authentication.getName(), request.username(), request.reason())
            ));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus().value())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }
}
