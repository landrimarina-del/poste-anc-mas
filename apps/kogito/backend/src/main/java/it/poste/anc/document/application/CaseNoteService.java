package it.poste.anc.document.application;

import it.poste.anc.document.api.CaseNoteDto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service per le note intermediate di pratica (tabella case_note).
 * Equivalente funzionale di BOA_ANC_ENTITY_CASENOTE.
 */
@Service
public class CaseNoteService {

    private static final String TIPO_LAVORAZIONE = "LAVORAZIONE";
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private final JdbcTemplate jdbcTemplate;

    public CaseNoteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<CaseNoteDto> listByPractice(Long practiceId) {
        verifyPracticeExists(practiceId);
        return jdbcTemplate.query(
                "SELECT id, autore, testo, tipo, created_at FROM case_note "
                        + "WHERE practice_id = ? ORDER BY created_at DESC",
                (rs, rowNum) -> new CaseNoteDto(
                        rs.getLong("id"),
                        rs.getString("autore"),
                        rs.getString("testo"),
                        rs.getString("tipo"),
                        rs.getTimestamp("created_at").toLocalDateTime().format(ISO_FMT)
                ),
                practiceId
        );
    }

    @Transactional
    public CaseNoteDto createNote(Long practiceId, String testo, String autore) {
        verifyPracticeExists(practiceId);

        if (testo == null || testo.isBlank()) {
            throw new DocumentOperationException(HttpStatus.BAD_REQUEST, 5001,
                    "Il testo della nota non può essere vuoto");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO case_note (practice_id, autore, testo, tipo) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, practiceId);
            ps.setString(2, autore);
            ps.setString(3, testo);
            ps.setString(4, TIPO_LAVORAZIONE);
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("Impossibile recuperare l'ID generato per la nota");
        }
        long newId = generatedId.longValue();
        return jdbcTemplate.queryForObject(
                "SELECT id, autore, testo, tipo, created_at FROM case_note WHERE id = ?",
                (rs, rowNum) -> new CaseNoteDto(
                        rs.getLong("id"),
                        rs.getString("autore"),
                        rs.getString("testo"),
                        rs.getString("tipo"),
                        rs.getTimestamp("created_at").toLocalDateTime().format(ISO_FMT)
                ),
                newId
        );
    }

    private void verifyPracticeExists(Long practiceId) {
        try {
            jdbcTemplate.queryForObject("SELECT id FROM practice WHERE id = ?", Long.class, practiceId);
        } catch (EmptyResultDataAccessException ex) {
            throw new DocumentOperationException(HttpStatus.NOT_FOUND, 2004, "Pratica non trovata");
        }
    }

    /**
     * Upsert nota unica per pratica:
     * - se esiste già almeno una nota, aggiorna la più recente
     * - altrimenti crea una nuova nota
     */
    @Transactional
    public CaseNoteDto upsertNote(Long practiceId, String testo, String autore) {
        verifyPracticeExists(practiceId);
        if (testo == null || testo.isBlank()) {
            throw new DocumentOperationException(HttpStatus.BAD_REQUEST, 5001,
                    "Il testo della nota non può essere vuoto");
        }
        List<CaseNoteDto> existing = listByPractice(practiceId);
        if (!existing.isEmpty()) {
            long noteId = existing.get(0).id();
            jdbcTemplate.update(
                    "UPDATE case_note SET testo = ?, autore = ? WHERE id = ?",
                    testo, autore, noteId);
            return jdbcTemplate.queryForObject(
                    "SELECT id, autore, testo, tipo, created_at FROM case_note WHERE id = ?",
                    (rs, rowNum) -> new CaseNoteDto(
                            rs.getLong("id"),
                            rs.getString("autore"),
                            rs.getString("testo"),
                            rs.getString("tipo"),
                            rs.getTimestamp("created_at").toLocalDateTime().format(ISO_FMT)
                    ),
                    noteId);
        }
        return createNote(practiceId, testo, autore);
    }
}
