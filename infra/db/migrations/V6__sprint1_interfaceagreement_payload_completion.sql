-- =====================================================================
-- V6 - Sprint 1: completamento persistenza payload InterfaceAgreement
-- Obiettivo: coprire i campi mancanti mantenendo compatibilita' con schema attuale
-- Strategia: solo cambi additive + backfill non distruttivo
-- =====================================================================

-- ROOT: campo mancante CODICE_CLIENTE
ALTER TABLE practice
    ADD COLUMN codice_cliente VARCHAR(64) NULL AFTER cf_cliente,
    ADD KEY idx_practice_codice_cliente (codice_cliente);

-- CLIENTE: estensione anagrafica e indirizzo di residenza
ALTER TABLE client_data
    ADD COLUMN sesso CHAR(1) NULL AFTER nome,
    ADD COLUMN comune_nascita VARCHAR(128) NULL AFTER data_nascita,
    ADD COLUMN provincia_nascita VARCHAR(4) NULL AFTER comune_nascita,
    ADD COLUMN nazione_nascita VARCHAR(64) NULL AFTER provincia_nascita,
    ADD COLUMN cittadinanza VARCHAR(64) NULL AFTER nazione_nascita,
    ADD COLUMN cellulare VARCHAR(32) NULL AFTER cittadinanza,
    ADD COLUMN telefono VARCHAR(32) NULL AFTER cellulare,
    ADD COLUMN residenza_luogo VARCHAR(255) NULL AFTER telefono,
    ADD COLUMN residenza_comune VARCHAR(128) NULL AFTER residenza_luogo,
    ADD COLUMN residenza_provincia VARCHAR(4) NULL AFTER residenza_comune,
    ADD COLUMN residenza_nazione VARCHAR(64) NULL AFTER residenza_provincia,
    ADD COLUMN residenza_cap VARCHAR(16) NULL AFTER residenza_nazione,
    ADD COLUMN residenza_civico VARCHAR(16) NULL AFTER residenza_cap,
    ADD KEY idx_client_codice_fiscale (codice_fiscale);

-- DATI_CARTA_BLOCCATA: nuovi campi richiesti da InterfaceAgreement
ALTER TABLE card_data
    ADD COLUMN numero_carta VARCHAR(34) NULL AFTER pan_masked,
    ADD COLUMN tipo_carta VARCHAR(32) NULL AFTER numero_carta,
    ADD COLUMN intestatario_carta VARCHAR(128) NULL AFTER tipo_carta,
    ADD KEY idx_card_numero_carta (numero_carta);

-- DOCUMENTI.CONTENUTI: metadati mancanti
ALTER TABLE attachment
    ADD COLUMN estensione VARCHAR(16) NULL AFTER file_name,
    ADD COLUMN id_doc VARCHAR(128) NULL AFTER estensione,
    ADD COLUMN link_download VARCHAR(1024) NULL AFTER id_doc,
    ADD KEY idx_att_practice_doc_code (practice_id, codice_doc_id),
    ADD KEY idx_att_id_doc (id_doc),
    ADD UNIQUE KEY uk_att_practice_id_doc (practice_id, id_doc);

-- =====================================================================
-- Backfill non distruttivo da dati legacy, dove possibile
-- =====================================================================

UPDATE card_data
SET
    tipo_carta = COALESCE(tipo_carta, card_type),
    numero_carta = COALESCE(numero_carta, pan_masked)
WHERE
    tipo_carta IS NULL
    OR numero_carta IS NULL;

UPDATE attachment
SET
    estensione = COALESCE(
        estensione,
        CASE
            WHEN file_name IS NOT NULL AND LOCATE('.', file_name) > 0
                THEN LOWER(SUBSTRING_INDEX(file_name, '.', -1))
            ELSE NULL
        END
    ),
    link_download = COALESCE(link_download, storage_uri)
WHERE
    estensione IS NULL
    OR link_download IS NULL;
