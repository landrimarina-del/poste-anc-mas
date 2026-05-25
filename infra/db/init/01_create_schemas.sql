-- ---------------------------------------------------------------
-- Init script eseguito da MariaDB al primo avvio del container.
-- Crea gli schemi separati per Flowable engine e Flowable UI.
-- ---------------------------------------------------------------

-- Schema usato dal backend Flowable 7.0.1 (tabelle ACT_*, FLW_*)
CREATE DATABASE IF NOT EXISTS flowable
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Schema usato da Flowable UI 6.8.0 (tabelle proprie, versione isolata)
CREATE DATABASE IF NOT EXISTS flowable_ui
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Grant all'utente applicativo su entrambi gli schemi
GRANT ALL PRIVILEGES ON flowable.*    TO 'anc'@'%';
GRANT ALL PRIVILEGES ON flowable_ui.* TO 'anc'@'%';

FLUSH PRIVILEGES;
