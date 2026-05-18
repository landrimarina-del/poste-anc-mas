#!/usr/bin/env bash
# Verifica DDL V9: colonna, indice, CHECK constraint, UNIQUE.
set -euo pipefail
echo "--- SHOW COLUMNS ingestion_status ---"
docker exec anc-db mariadb -uanc -panc anc -e "SHOW COLUMNS FROM attachment LIKE 'ingestion_status';"
echo "--- SHOW INDEX idx_att_ingest_status ---"
docker exec anc-db mariadb -uanc -panc anc -e "SHOW INDEX FROM attachment WHERE Key_name='idx_att_ingest_status';"
echo "--- CHECK constraint chk_att_ingestion_status_s4 ---"
docker exec anc-db mariadb -uanc -panc anc -e "SELECT constraint_name, check_clause FROM information_schema.check_constraints WHERE table_name='attachment' AND constraint_name='chk_att_ingestion_status_s4';"
echo "--- UNIQUE uk_att_practice_id_doc ---"
docker exec anc-db mariadb -uanc -panc anc -e "SHOW INDEX FROM attachment WHERE Key_name='uk_att_practice_id_doc';"
