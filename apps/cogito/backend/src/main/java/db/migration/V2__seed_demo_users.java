package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * V2 — Seed utenti demo POC (06_Dati_POC.md §1).
 *
 * Crea 4 utenti con password BCrypt di "Demo1234!" e li associa a ruoli/gruppi.
 * Utilizza una migration Java per generare l'hash BCrypt con il sale corretto a runtime
 * (evita di hardcodare un hash nel SQL).
 */
public class V2__seed_demo_users extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        String hash = new BCryptPasswordEncoder().encode("Demo1234!");

        // Ruoli
        insertRole(conn, "OPERATORE_ANC",   "Operatore ANC");
        insertRole(conn, "SUPERVISORE_ANC", "Supervisore ANC");
        insertRole(conn, "ADMIN",           "Amministratore tecnico");

        // Gruppi
        insertGroup(conn, "GRUPPO_OPERATORE_ANC", "Gruppo Operatori ANC");

        // Utenti
        long opRossiId   = insertUser(conn, "op.rossi",   hash, "Mario Rossi",   "op.rossi@poste.it");
        long opBianchiId = insertUser(conn, "op.bianchi", hash, "Luca Bianchi",  "op.bianchi@poste.it");
        long supVerdiId  = insertUser(conn, "sup.verdi",  hash, "Anna Verdi",    "sup.verdi@poste.it");
        long adminId     = insertUser(conn, "admin",      hash, "Admin POC",     "admin@poste.it");

        long roleOpId    = idByCode(conn, "role",       "OPERATORE_ANC");
        long roleSupId   = idByCode(conn, "role",       "SUPERVISORE_ANC");
        long roleAdminId = idByCode(conn, "role",       "ADMIN");
        long groupOpId   = idByCode(conn, "user_group", "GRUPPO_OPERATORE_ANC");

        // Associazioni ruoli
        linkUserRole(conn, opRossiId,   roleOpId);
        linkUserRole(conn, opBianchiId, roleOpId);
        linkUserRole(conn, supVerdiId,  roleSupId);
        linkUserRole(conn, adminId,     roleAdminId);

        // Associazioni gruppi (admin non e' nel gruppo operatori ANC come da 06_Dati_POC.md)
        linkUserGroup(conn, opRossiId,   groupOpId);
        linkUserGroup(conn, opBianchiId, groupOpId);
        linkUserGroup(conn, supVerdiId,  groupOpId);
    }

    private void insertRole(Connection conn, String code, String name) throws Exception {
        if (existsByCode(conn, "role", code)) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO role (code, name) VALUES (?, ?)")) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    private void insertGroup(Connection conn, String code, String name) throws Exception {
        if (existsByCode(conn, "user_group", code)) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_group (code, name) VALUES (?, ?)")) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    private long insertUser(Connection conn, String username, String hash, String fullName, String email) throws Exception {
        Long existingId = idByUsername(conn, username);
        if (existingId != null) {
            return existingId;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO app_user (username, password_hash, full_name, email, active) VALUES (?, ?, ?, ?, 1)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, fullName);
            ps.setString(4, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new IllegalStateException("Nessuna PK generata per utente " + username);
            }
        }
    }

    private long idByCode(Connection conn, String table, String code) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM " + table + " WHERE code = ?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
                throw new IllegalStateException(table + "." + code + " non trovato");
            }
        }
    }

    private boolean existsByCode(Connection conn, String table, String code) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM " + table + " WHERE code = ?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Long idByUsername(Connection conn, String username) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM app_user WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return null;
            }
        }
    }

    private void linkUserRole(Connection conn, long userId, long roleId) throws Exception {
        if (linkExists(conn, "user_role", "role_id", userId, roleId)) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_role (user_id, role_id) VALUES (?, ?)")) {
            ps.setLong(1, userId);
            ps.setLong(2, roleId);
            ps.executeUpdate();
        }
    }

    private void linkUserGroup(Connection conn, long userId, long groupId) throws Exception {
        if (linkExists(conn, "user_group_member", "group_id", userId, groupId)) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_group_member (user_id, group_id) VALUES (?, ?)")) {
            ps.setLong(1, userId);
            ps.setLong(2, groupId);
            ps.executeUpdate();
        }
    }

    private boolean linkExists(Connection conn, String table, String rightColumn, long userId, long rightId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM " + table + " WHERE user_id = ? AND " + rightColumn + " = ?")) {
            ps.setLong(1, userId);
            ps.setLong(2, rightId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
