package it.poste.anc.workflow.application;

import it.poste.anc.workflow.api.UserTaskFilterDto;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserTaskFilterService {

    private static final int MAX_FILTERS_PER_USER = 5;

    private final JdbcTemplate jdbcTemplate;

    public UserTaskFilterService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<UserTaskFilterDto> listFilters(String username) {
        return jdbcTemplate.query(
                "SELECT id, filter_name, filter_json, created_at "
                        + "FROM user_task_filter WHERE username = ? ORDER BY created_at DESC LIMIT ?",
                (rs, rowNum) -> new UserTaskFilterDto(
                        rs.getLong("id"),
                        rs.getString("filter_name"),
                        rs.getString("filter_json"),
                        rs.getString("created_at")
                ),
                username,
                MAX_FILTERS_PER_USER
        );
    }

    @Transactional
    public UserTaskFilterDto saveFilter(String username, String filterName, String filterJson) {
        if (filterJson == null || filterJson.isBlank()) {
            throw new TaskOperationException(HttpStatus.BAD_REQUEST, 5001,
                    "filterJson obbligatorio");
        }

        jdbcTemplate.update(
                "INSERT INTO user_task_filter (username, filter_name, filter_json) VALUES (?, ?, ?)",
                username, filterName, filterJson
        );

        Long newId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_task_filter WHERE username = ? ORDER BY created_at DESC LIMIT 1",
                Long.class,
                username
        );

        // FIFO: se supera il limite, elimina il più vecchio
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_task_filter WHERE username = ?",
                Integer.class,
                username
        );
        if (count != null && count > MAX_FILTERS_PER_USER) {
            jdbcTemplate.update(
                    "DELETE FROM user_task_filter WHERE username = ? "
                            + "ORDER BY created_at ASC LIMIT ?",
                    username,
                    count - MAX_FILTERS_PER_USER
            );
        }

        return jdbcTemplate.queryForObject(
                "SELECT id, filter_name, filter_json, created_at FROM user_task_filter WHERE id = ?",
                (rs, rowNum) -> new UserTaskFilterDto(
                        rs.getLong("id"),
                        rs.getString("filter_name"),
                        rs.getString("filter_json"),
                        rs.getString("created_at")
                ),
                newId
        );
    }

    @Transactional
    public void deleteFilter(Long id, String username) {
        int deleted = jdbcTemplate.update(
                "DELETE FROM user_task_filter WHERE id = ? AND username = ?",
                id, username
        );
        if (deleted == 0) {
            throw new TaskOperationException(HttpStatus.NOT_FOUND, 5002,
                    "Filtro non trovato o non di proprietà dell'utente");
        }
    }
}
