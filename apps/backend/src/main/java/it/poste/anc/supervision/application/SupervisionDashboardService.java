package it.poste.anc.supervision.application;

import it.poste.anc.supervision.api.SupervisionDailyOpenedPoint;
import it.poste.anc.supervision.api.SupervisionDailyWorkedPoint;
import it.poste.anc.supervision.api.SupervisionDashboardCountersResponse;
import it.poste.anc.supervision.api.SupervisionPracticeByStatePoint;
import it.poste.anc.workflow.application.TaskOperationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupervisionDashboardService {

    private static final String SUPERVISORE_ROLE_CODE = "SUPERVISORE_ANC";

    private final JdbcTemplate jdbcTemplate;

    public SupervisionDashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public SupervisionDashboardCountersResponse loadCounters(String username) {
        Long supervisorId = findActiveUserId(username);
        ensureUserIsSupervisor(supervisorId);

        long activities = queryCount("SELECT COUNT(1) FROM task WHERE stato IN ('IN_CODA', 'IN_CARICO')");
        long activePractices = queryCount(
                "SELECT COUNT(1) FROM practice WHERE stato IN ('APERTA', 'IN_LAVORAZIONE', 'IN_ATTESA_CONFERMA_BPM')"
        );
        long closedPractices = queryCount("SELECT COUNT(1) FROM practice WHERE stato IN ('CHIUSA_OK', 'CHIUSA_KO')");

        return new SupervisionDashboardCountersResponse(activities, activePractices, closedPractices);
    }

    @Transactional(readOnly = true)
    public List<SupervisionDailyOpenedPoint> loadDailyOpened(String username, YearMonth month) {
        Long supervisorId = findActiveUserId(username);
        ensureUserIsSupervisor(supervisorId);

        LocalDate firstDay = month.atDay(1);
        LocalDate firstDayNextMonth = month.plusMonths(1).atDay(1);

        Map<Integer, Long> countsByDay = new HashMap<>();
        jdbcTemplate.query(
                "SELECT DATE(psh.occurred_at) AS day_ref, COUNT(1) AS opened_count "
                        + "FROM practice_state_history psh "
                        + "WHERE psh.to_state = 'APERTA' "
                        + "AND psh.occurred_at >= ? "
                        + "AND psh.occurred_at < ? "
                        + "GROUP BY DATE(psh.occurred_at)",
                rs -> {
                    LocalDate day = toLocalDate(rs.getDate("day_ref"));
                    if (day != null) {
                        countsByDay.put(day.getDayOfMonth(), rs.getLong("opened_count"));
                    }
                },
                Timestamp.valueOf(firstDay.atStartOfDay()),
                Timestamp.valueOf(firstDayNextMonth.atStartOfDay())
        );

        List<SupervisionDailyOpenedPoint> points = new ArrayList<>();
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            points.add(new SupervisionDailyOpenedPoint(day, countsByDay.getOrDefault(day, 0L)));
        }
        return points;
    }

    @Transactional(readOnly = true)
    public List<SupervisionDailyWorkedPoint> loadDailyWorked(String username, YearMonth month) {
        Long supervisorId = findActiveUserId(username);
        ensureUserIsSupervisor(supervisorId);

        LocalDate firstDay = month.atDay(1);
        LocalDate firstDayNextMonth = month.plusMonths(1).atDay(1);

        Map<Integer, DailyWorkedAccumulator> countsByDay = new HashMap<>();
        jdbcTemplate.query(
                "SELECT DATE(psh.occurred_at) AS day_ref, "
                        + "SUM(CASE WHEN psh.to_state = 'CHIUSA_OK' THEN 1 ELSE 0 END) AS ok_count, "
                        + "SUM(CASE WHEN psh.to_state = 'CHIUSA_KO' THEN 1 ELSE 0 END) AS ko_count "
                        + "FROM practice_state_history psh "
                        + "WHERE psh.to_state IN ('CHIUSA_OK', 'CHIUSA_KO') "
                        + "AND psh.occurred_at >= ? "
                        + "AND psh.occurred_at < ? "
                        + "GROUP BY DATE(psh.occurred_at)",
                rs -> {
                    LocalDate day = toLocalDate(rs.getDate("day_ref"));
                    if (day != null) {
                        countsByDay.put(
                                day.getDayOfMonth(),
                                new DailyWorkedAccumulator(rs.getLong("ok_count"), rs.getLong("ko_count"))
                        );
                    }
                },
                Timestamp.valueOf(firstDay.atStartOfDay()),
                Timestamp.valueOf(firstDayNextMonth.atStartOfDay())
        );

        List<SupervisionDailyWorkedPoint> points = new ArrayList<>();
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            DailyWorkedAccumulator accumulator = countsByDay.get(day);
            if (accumulator == null) {
                points.add(new SupervisionDailyWorkedPoint(day, 0L, 0L));
                continue;
            }
            points.add(new SupervisionDailyWorkedPoint(day, accumulator.okCount(), accumulator.koCount()));
        }
        return points;
    }

    @Transactional(readOnly = true)
    public List<SupervisionPracticeByStatePoint> loadPracticesByState(String username) {
        Long supervisorId = findActiveUserId(username);
        ensureUserIsSupervisor(supervisorId);

        return jdbcTemplate.query(
                "SELECT stato, COUNT(1) AS total FROM practice GROUP BY stato ORDER BY stato",
                (rs, rowNum) -> new SupervisionPracticeByStatePoint(rs.getString("stato"), rs.getLong("total"))
        );
    }

    private Long findActiveUserId(String username) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM app_user WHERE username = ? AND active = 1",
                    Long.class,
                    username
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new TaskOperationException(HttpStatus.UNAUTHORIZED, 1002, "Utente non autenticato");
        }
    }

    private void ensureUserIsSupervisor(Long userId) {
        Integer membership = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) "
                        + "FROM user_role ur "
                        + "JOIN role r ON r.id = ur.role_id "
                        + "WHERE ur.user_id = ? AND r.code = ?",
                Integer.class,
                userId,
                SUPERVISORE_ROLE_CODE
        );
        if (membership == null || membership == 0) {
            throw new TaskOperationException(HttpStatus.FORBIDDEN, 6101,
                    "Utente non autorizzato: ruolo SUPERVISORE_ANC richiesto");
        }
    }

    private long queryCount(String sql) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count == null ? 0L : count;
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toLocalDate();
    }

    private record DailyWorkedAccumulator(long okCount, long koCount) {
    }
}
