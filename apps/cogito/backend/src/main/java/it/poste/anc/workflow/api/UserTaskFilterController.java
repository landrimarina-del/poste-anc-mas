package it.poste.anc.workflow.api;

import it.poste.anc.shared.common.ApiResponse;
import it.poste.anc.workflow.application.UserTaskFilterService;
import it.poste.anc.workflow.application.TaskOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/tasks/filters/saved", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserTaskFilterController {

    private final UserTaskFilterService userTaskFilterService;

    public UserTaskFilterController(UserTaskFilterService userTaskFilterService) {
        this.userTaskFilterService = userTaskFilterService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserTaskFilterDto>>> listFilters(Authentication auth) {
        List<UserTaskFilterDto> filters = userTaskFilterService.listFilters(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(filters));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserTaskFilterDto>> saveFilter(
            @RequestBody UserTaskFilterRequest request,
            Authentication auth) {
        try {
            UserTaskFilterDto saved = userTaskFilterService.saveFilter(
                    auth.getName(),
                    request.filterName(),
                    request.filterJson()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFilter(
            @PathVariable Long id,
            Authentication auth) {
        try {
            userTaskFilterService.deleteFilter(id, auth.getName());
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (TaskOperationException ex) {
            return ResponseEntity.status(ex.getHttpStatus())
                    .body(ApiResponse.error(ex.getResultCode(), ex.getMessage()));
        }
    }
}
