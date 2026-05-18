-- ============================================================================
-- Sprint 1 - Bootstrap / Sanity Check (MariaDB)
-- Eseguire su database: anc
-- Scopo:
-- 1) Verificare baseline IAM (tabelle + dati seed)
-- 2) Ottenere conteggi rapidi
-- 3) Visualizzare join utenti/ruoli/gruppi
-- ============================================================================

USE anc;

-- 0) Contesto runtime
SELECT DATABASE() AS current_db, VERSION() AS mariadb_version;

-- 1) Presenza tabelle cross-cutting IAM
SELECT
  t.table_name,
  CASE WHEN t.table_name IS NOT NULL THEN 'OK' ELSE 'MISSING' END AS status
FROM (
  SELECT 'app_user' AS table_name
  UNION ALL SELECT 'role'
  UNION ALL SELECT 'user_role'
  UNION ALL SELECT 'user_group'
  UNION ALL SELECT 'user_group_member'
) expected
LEFT JOIN information_schema.tables t
  ON t.table_schema = DATABASE()
 AND t.table_name = expected.table_name
ORDER BY expected.table_name;

-- 2) Conteggi record principali
SELECT 'app_user' AS entity, COUNT(*) AS total FROM app_user
UNION ALL
SELECT 'role' AS entity, COUNT(*) AS total FROM role
UNION ALL
SELECT 'user_role' AS entity, COUNT(*) AS total FROM user_role
UNION ALL
SELECT 'user_group' AS entity, COUNT(*) AS total FROM user_group
UNION ALL
SELECT 'user_group_member' AS entity, COUNT(*) AS total FROM user_group_member;

-- 3) Seed users attesi Sprint 0 (check esistenza)
SELECT
  username,
  full_name,
  email,
  active,
  CASE WHEN password_hash IS NOT NULL AND CHAR_LENGTH(password_hash) > 20 THEN 'OK' ELSE 'CHECK' END AS password_hash_status
FROM app_user
WHERE username IN ('op.rossi', 'op.bianchi', 'sup.verdi', 'admin')
ORDER BY username;

-- 4) Join utenti -> ruoli + gruppi (vista consolidata)
SELECT
  u.id,
  u.username,
  u.full_name,
  u.active,
  COALESCE(GROUP_CONCAT(DISTINCT r.code ORDER BY r.code SEPARATOR ', '), '-') AS roles,
  COALESCE(GROUP_CONCAT(DISTINCT g.code ORDER BY g.code SEPARATOR ', '), '-') AS groups_code
FROM app_user u
LEFT JOIN user_role ur ON ur.user_id = u.id
LEFT JOIN role r ON r.id = ur.role_id
LEFT JOIN user_group_member ugm ON ugm.user_id = u.id
LEFT JOIN user_group g ON g.id = ugm.group_id
GROUP BY u.id, u.username, u.full_name, u.active
ORDER BY u.username;

-- 5) Utenti senza ruoli (anomalia)
SELECT u.id, u.username, u.full_name
FROM app_user u
LEFT JOIN user_role ur ON ur.user_id = u.id
WHERE ur.user_id IS NULL
ORDER BY u.username;

-- 6) Utenti senza gruppi (anomalia, admin puo' essere senza gruppo)
SELECT u.id, u.username, u.full_name
FROM app_user u
LEFT JOIN user_group_member ugm ON ugm.user_id = u.id
WHERE ugm.user_id IS NULL
ORDER BY u.username;

-- 7) Distribuzione utenti per ruolo
SELECT
  r.code AS role_code,
  COUNT(ur.user_id) AS users_count
FROM role r
LEFT JOIN user_role ur ON ur.role_id = r.id
GROUP BY r.code
ORDER BY r.code;

-- 8) Distribuzione utenti per gruppo
SELECT
  g.code AS group_code,
  COUNT(ugm.user_id) AS users_count
FROM user_group g
LEFT JOIN user_group_member ugm ON ugm.group_id = g.id
GROUP BY g.code
ORDER BY g.code;
