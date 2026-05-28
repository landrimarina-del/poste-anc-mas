-- Crea il gruppo Supervisori ANC
INSERT INTO user_group (code, name)
VALUES ('GRUPPO_SUPERVISORE_ANC', 'Gruppo Supervisori ANC');

-- Assegna tutti gli utenti con ruolo SUPERVISORE_ANC al nuovo gruppo
INSERT INTO user_group_member (user_id, group_id)
SELECT u.id, ug.id
FROM app_user u
JOIN user_role ur ON ur.user_id = u.id
JOIN role r ON r.id = ur.role_id
JOIN user_group ug ON ug.code = 'GRUPPO_SUPERVISORE_ANC'
WHERE r.code = 'SUPERVISORE_ANC'
  AND u.active = 1;

-- Rimuove i supervisori dal gruppo operatori (ruoli mutualmente esclusivi)
DELETE ugm FROM user_group_member ugm
JOIN user_group ug ON ug.id = ugm.group_id
JOIN user_role ur ON ur.user_id = ugm.user_id
JOIN role r ON r.id = ur.role_id
WHERE ug.code = 'GRUPPO_OPERATORE_ANC'
  AND r.code = 'SUPERVISORE_ANC';
