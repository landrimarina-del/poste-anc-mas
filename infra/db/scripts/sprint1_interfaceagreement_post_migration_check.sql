-- =====================================================================
-- Sprint 1 - Verifiche post-migrazione InterfaceAgreement
-- =====================================================================

-- 1) Verifica presenza nuove colonne
SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
       (table_name = 'practice' AND column_name IN ('codice_cliente'))
    OR (table_name = 'client_data' AND column_name IN (
        'sesso','comune_nascita','provincia_nascita','nazione_nascita','cittadinanza',
        'cellulare','telefono','residenza_luogo','residenza_comune','residenza_provincia',
        'residenza_nazione','residenza_cap','residenza_civico'
    ))
    OR (table_name = 'card_data' AND column_name IN ('numero_carta','tipo_carta','intestatario_carta'))
    OR (table_name = 'attachment' AND column_name IN ('estensione','id_doc','link_download'))
  )
ORDER BY table_name, column_name;

-- 2) Verifica presenza indici/unique attesi
SELECT table_name, index_name, non_unique, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS index_columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name IN ('practice', 'client_data', 'card_data', 'attachment')
  AND index_name IN (
      'idx_practice_codice_cliente',
      'idx_client_codice_fiscale',
      'idx_card_numero_carta',
      'idx_att_practice_doc_code',
      'idx_att_id_doc',
      'uk_att_practice_id_doc'
  )
GROUP BY table_name, index_name, non_unique
ORDER BY table_name, index_name;

-- 3) Verifica backfill card_data (legacy -> nuovi campi)
SELECT
    COUNT(*) AS card_rows,
    SUM(CASE WHEN card_type IS NOT NULL AND tipo_carta IS NULL THEN 1 ELSE 0 END) AS missing_tipo_carta_after_backfill,
    SUM(CASE WHEN pan_masked IS NOT NULL AND numero_carta IS NULL THEN 1 ELSE 0 END) AS missing_numero_carta_after_backfill
FROM card_data;

-- 4) Verifica backfill attachment (legacy -> nuovi campi)
SELECT
    COUNT(*) AS attachment_rows,
    SUM(CASE WHEN file_name IS NOT NULL AND estensione IS NULL THEN 1 ELSE 0 END) AS missing_estensione_after_backfill,
    SUM(CASE WHEN storage_uri IS NOT NULL AND link_download IS NULL THEN 1 ELSE 0 END) AS missing_link_after_backfill
FROM attachment;

-- 5) Verifica duplicati su chiave semantica documento (solo id_doc valorizzato)
SELECT practice_id, id_doc, COUNT(*) AS cnt
FROM attachment
WHERE id_doc IS NOT NULL
GROUP BY practice_id, id_doc
HAVING COUNT(*) > 1;

-- 6) Verifica allineamento root minimo su practice
SELECT
    COUNT(*) AS practice_rows,
    SUM(CASE WHEN canale IS NULL THEN 1 ELSE 0 END) AS null_canale,
    SUM(CASE WHEN id_work_item IS NULL THEN 1 ELSE 0 END) AS null_id_work_item,
    SUM(CASE WHEN num_pratica IS NULL THEN 1 ELSE 0 END) AS null_num_pratica,
    SUM(CASE WHEN cf_cliente IS NULL THEN 1 ELSE 0 END) AS null_cf_cliente,
    SUM(CASE WHEN data_inserimento_richiesta IS NULL THEN 1 ELSE 0 END) AS null_data_ins_rich
FROM practice;
