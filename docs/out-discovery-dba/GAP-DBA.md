# GAP-DBA — Patch Database per il Gruppo di Sviluppo

> **Scopo**: Documento di patch DB pronto per lo sviluppo.  
> Elenca tutte le modifiche al modello dati richieste da `GAP_Architettura.md` (Parte A e Parte B)  
> confrontate con lo schema baseline in `docs/out-discovery-dba/03_Schema_DDL.md`.  
>
> **Fonte**: `docs/out-discovery-architect/GAP_Architettura.md` (2026-05-21)  
> **Schema baseline**: `docs/out-discovery-dba/03_Schema_DDL.md` (V1)  
> **DBMS target**: MariaDB 10.11 | Engine: InnoDB | charset: utf8mb4_unicode_ci  
> **Convenzioni**: naming `snake_case`, no ENUM, no trigger DB, script Flyway in `infra/db/migrations/`  
> **⚠️ Versioning Flyway**: le migration V1–V14 sono già applicate (V14 = Sprint 10).  
> Le nuove migration partono da **V15**. Non riusare versioni già esistenti.  
> Sprint 4 V9 ha già aggiunto `ingestion_status` su `attachment`: `download_status` non va riaggiunto (vedi nota V21).

---

## Riepilogo Patch

| Migration | File | Tabella | Tipo | GAP Ref | Sprint |
|---|---|---|---|---|---|
| V15 | `V15__gap_us02_bpm_outbound_retry.sql` | `bpm_outbound_message` | ALTER ADD COLUMNS | GAP-US-02 | Sprint 5 |
| V16 | `V16__gap_us01_practice_ticket.sql` | `practice` | ALTER ADD COLUMN | GAP-US-01 | Sprint 5 |
| V17 | `V17__gap_us08_checklist_dependency.sql` | `checklist_item_catalog` | ALTER ADD COLUMNS | GAP-US-08 | Sprint 7 |
| V18 | `V18__gap_us11_causali_checklist.sql` | `ref_causali_checklist` + `checklist_response` | CREATE + ALTER | GAP-US-11 | Sprint 7 |
| V19 | `V19__tecnico_gap_c_case_note.sql` | `case_note` | CREATE TABLE | TECNICO-GAP-C | Sprint 7 |
| V20 | `V20__gap_us05_user_task_filter.sql` | `user_task_filter` | CREATE TABLE | GAP-US-05 | Sprint 8 |
| V21 | `V21__tecnico_gap_a_attachment_linkdownload.sql` | `attachment` | ALTER MODIFY | TECNICO-GAP-A | Sprint 9 |
| V22 | `V22__tecnico_gap_b_task_sla.sql` | `task` | ALTER ADD COLUMN | TECNICO-GAP-B | Sprint 9 |

**Non richiedono patch DB**: GAP-US-03, GAP-US-04, GAP-US-06, GAP-US-07, GAP-US-09, GAP-US-10, TECNICO-GAP-D, TECNICO-GAP-E, TECNICO-GAP-F  
(colonne già presenti nel baseline, oppure impatto solo Backend/BPMN/Config)

---

## Verifica Pre-Patch — Differenze Schema Baseline

Prima di applicare le migration, annotare le seguenti discrepanze già esistenti tra il DDL baseline e il GAP:

| # | Tabella | Campo baseline | Campo richiesto da GAP | Azione |
|---|---|---|---|---|
| D1 | `bpm_outbound_message` | `send_status VARCHAR(16)` + `ack_status VARCHAR(16)` (pattern callback) | `stato_invio TINYINT` 0/1/2/3 (pattern retry sincrono) | V2: aggiungere colonne retry; `send_status`/`ack_status` rimangono (backward compat) |
| D2 | `checklist_response` | `note VARCHAR(500)` + `ko_reason_code VARCHAR(64)` | `nota VARCHAR(255)` + `codice_causale_id BIGINT FK` | V6: `note` già soddisfa `nota` (500 ≥ 255); aggiungere solo `codice_causale_id` |
| D3 | `attachment` | `link_download VARCHAR(1024)` | `link_download VARCHAR(2500)` | V7: MODIFY per espandere la lunghezza |
| D4 | `practice` | `data_inserimento_richiesta DATETIME(3) NOT NULL` | già richiesto da GAP-US-09 | ✅ già presente — nessuna patch |
| D5 | `practice_state_history` | presente con `actor_username VARCHAR(64)` | richiesto da GAP-US-10 con `actor VARCHAR(100)` | ✅ campo equivalente — nessuna patch |
| D6 | `checklist_item_catalog` | nome differente da `ref_checklist_item` usato nel GAP | stesso oggetto logico | usare `checklist_item_catalog` come nome fisico reale |

---

## V15 — `bpm_outbound_message`: aggiunta colonne retry sincrono

**File**: `infra/db/migrations/V15__gap_us02_bpm_outbound_retry.sql`  
**GAP ref**: GAP-US-02 (chiamata BPM sincrona con retry) | **Sprint**: 5  
**Nota**: La tabella esiste già nel baseline con le colonne del pattern callback (`send_status`, `ack_status`).  
Questo script aggiunge le colonne del pattern retry sincrono. Le colonne precedenti rimangono per backward compat durante la migrazione.

```sql
-- ============================================================
-- V15 — GAP-US-02: colonne retry sincrono su bpm_outbound_message
-- (V1-V14 già applicati; prossima versione disponibile: V15)
-- ============================================================

ALTER TABLE bpm_outbound_message
    ADD COLUMN retry_count      INT           NOT NULL DEFAULT 0
        COMMENT '0=mai tentato; N=numero tentativi effettuati'
        AFTER correlation_id,
    ADD COLUMN max_retry        INT           NOT NULL DEFAULT 3
        COMMENT 'Numero massimo tentativi configurato al momento della creazione'
        AFTER retry_count,
    ADD COLUMN stato_invio      TINYINT       NOT NULL DEFAULT 0
        COMMENT '0=attesa 1=inviato_ok 2=errore_transiente 3=scartato(retry esauriti)'
        AFTER max_retry,
    ADD COLUMN response_json    TEXT          NULL
        COMMENT 'Ultima risposta BPM ricevuta (JSON grezzo)'
        AFTER stato_invio,
    ADD COLUMN error_message    VARCHAR(1000) NULL
        COMMENT 'Descrizione errore ultimo tentativo fallito'
        AFTER response_json,
    ADD COLUMN last_attempt_at  DATETIME(3)   NULL
        COMMENT 'Timestamp ultimo tentativo di invio'
        AFTER error_message;

-- Indice su stato_invio per il polling del retry scheduler
CREATE INDEX idx_bom_stato_invio ON bpm_outbound_message (stato_invio);
```

**Transizioni `stato_invio` attese**:

| Da | A | Quando |
|---|---|---|
| 0 | 1 | BPM risponde con `esito=true/false` nel timeout |
| 0 | 2 | Timeout o errore HTTP (ancora retryable: `retry_count < max_retry`) |
| 2 | 1 | Retry successivo riuscito |
| 2 | 3 | `retry_count >= max_retry` — pratica rimane `IN_ATTESA_CONFERMA_BPM` |

---

## V16 — `practice`: aggiunta ticket_id

**File**: `infra/db/migrations/V16__gap_us01_practice_ticket.sql`  
**GAP ref**: GAP-US-01 (mock integrazione sistema di ticketing) | **Sprint**: 5

```sql
-- ============================================================
-- V16 — GAP-US-01: campo ticket_id su practice
-- ============================================================

ALTER TABLE practice
    ADD COLUMN ticket_id VARCHAR(100) NULL
        COMMENT 'ID ticket sistema di ticketing esterno (mock in POC: MOCK-TICKET-{uuid})'
        AFTER bpm_inbound_msg_id;

CREATE INDEX idx_practice_ticket ON practice (ticket_id);
```

**Comportamento atteso**:
- Valorizzato da `svc.openPractice` dopo chiamata al mock `POST /ticketing/open-ticket`.
- `NULL` ammesso: un fallimento del mock non blocca la creazione pratica.
- Non esposto nella griglia lista pratiche (confermato da GAP-US-09).

---

## V20 — `user_task_filter`: filtri Lista Attività salvati

**File**: `infra/db/migrations/V20__gap_us05_user_task_filter.sql`  
**GAP ref**: GAP-US-05 (filtri Lista Attività salvati su DB per utente) | **Sprint**: 8

```sql
-- ============================================================
-- V20 — GAP-US-05: set filtri Lista Attività persistiti per utente
-- ============================================================

CREATE TABLE user_task_filter (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    filter_name      VARCHAR(100) NULL
        COMMENT 'Etichetta libera opzionale assegnata dall utente al set',
    stato            VARCHAR(50)  NULL,
    tipo_pratica     VARCHAR(50)  NULL,
    pratica_numero   VARCHAR(50)  NULL,
    nome_attivita    VARCHAR(100) NULL,
    data_scadenza_da DATE         NULL,
    data_scadenza_a  DATE         NULL,
    assegnatario     VARCHAR(100) NULL,
    utente_in_carico VARCHAR(100) NULL,
    created_at       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_utf_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    KEY idx_utf_user_created (user_id, created_at DESC)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Set di filtri Lista Attività salvati per utente (max N per utente, gestito a livello applicativo)';
```

**Vincolo applicativo** (gestito dal Backend, non dal DB):
- Massimo 5 set per utente (configurabile: `tasks.saved-filters.max-per-user=5`).
- Al salvataggio del 6° set → eliminazione automatica del più vecchio (`created_at ASC LIMIT 1`).

---

## V17 — `checklist_item_catalog`: dipendenze tra item

**File**: `infra/db/migrations/V17__gap_us08_checklist_dependency.sql`  
**GAP ref**: GAP-US-08 (visibilità condizionale item checklist per `idDipendenza`) | **Sprint**: 7  
**Nota naming**: il GAP_Architettura usa il nome `ref_checklist_item`; il nome fisico nel DDL baseline è `checklist_item_catalog`.  
Questo script opera su `checklist_item_catalog` (nome fisico reale).

```sql
-- ============================================================
-- V17 — GAP-US-08: dipendenze tra item checklist
-- ============================================================

ALTER TABLE checklist_item_catalog
    ADD COLUMN id_dipendenza           BIGINT     NULL
        COMMENT 'FK a checklist_item_catalog.id: item padre che condiziona la visibilità'
        AFTER active,
    ADD COLUMN valore_attivo_dipendenza VARCHAR(10) NULL
        COMMENT 'Valore risposta del padre che ABILITA la visibilità di questo item (es. SI, NO, NA)'
        AFTER id_dipendenza,
    ADD CONSTRAINT fk_cic_dipendenza
        FOREIGN KEY (id_dipendenza) REFERENCES checklist_item_catalog(id);
```

**Regola di visibilità calcolata lato Backend** (non persistita su DB):

```
item.id_dipendenza IS NULL                          → visible = true
item.id_dipendenza IS NOT NULL:
    risposta_padre = item.valore_attivo_dipendenza  → visible = true
    risposta_padre ≠ item.valore_attivo_dipendenza  → visible = false
    risposta_padre IS NULL (non risposta)           → visible = false
```

---

## V18 — `ref_causali_checklist` + `checklist_response.codice_causale_id`

**File**: `infra/db/migrations/V18__gap_us11_causali_checklist.sql`  
**GAP ref**: GAP-US-11 (doppio meccanismo motivazioni: nota libera + codice causale formale) | **Sprint**: 7  
**Nota**: Il campo `note VARCHAR(500)` in `checklist_response` (baseline) soddisfa già la colonna `nota VARCHAR(255)` richiesta dal GAP (capacità 500 ≥ 255). Questo script aggiunge solo la FK al catalogo causali.

```sql
-- ============================================================
-- V18 — GAP-US-11: catalogo causali KO e FK su checklist_response
-- ============================================================

-- Tabella catalogo causali KO
CREATE TABLE ref_causali_checklist (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    codice      VARCHAR(20)  NOT NULL,
    descrizione VARCHAR(500) NOT NULL,
    categoria   VARCHAR(16)  NOT NULL
        COMMENT 'VERBALE | CARTA — corrisponde al document_type',
    attivo      TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_causali_codice_cat (codice, categoria),
    CONSTRAINT chk_causali_categoria CHECK (categoria IN ('VERBALE','CARTA'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Catalogo causali KO checklist per tipo documento';

-- Aggiunta FK causale su checklist_response
-- Nota: il campo note (già presente, 500 chars) soddisfa il requisito nota libera
ALTER TABLE checklist_response
    ADD COLUMN codice_causale_id BIGINT NULL
        COMMENT 'FK a ref_causali_checklist.id — causale KO formale (opzionale)'
        AFTER note,
    ADD CONSTRAINT fk_cr_causale
        FOREIGN KEY (codice_causale_id) REFERENCES ref_causali_checklist(id);

-- ============================================================
-- SEED: inserire i codici causali reali da MatriciControlli.xlsx
-- (placeholder — sostituire con valori reali in sede di implementazione)
-- ============================================================
INSERT INTO ref_causali_checklist (codice, descrizione, categoria, attivo) VALUES
    ('VERB_DOC_ILLEGGIBILE',   'Documento illeggibile o parzialmente leggibile', 'VERBALE', 1),
    ('VERB_FIRMA_MANCANTE',    'Firma del dichiarante assente',                  'VERBALE', 1),
    ('VERB_DATI_DISCORDANTI',  'Dati anagrafici discordanti con i sistemi',      'VERBALE', 1),
    ('CARTA_DOC_SCADUTO',      'Documento di identità scaduto',                  'CARTA',   1),
    ('CARTA_DOC_ILLEGGIBILE',  'Documento di identità illeggibile',              'CARTA',   1),
    ('CARTA_FIRMA_MANCANTE',   'Firma del titolare assente',                     'CARTA',   1);
-- TODO: integrare con lista completa da MatriciControlli.xlsx
```

**Comportamento atteso**:
- `nota` (= campo `note` esistente): testo libero, opzionale, visibile solo se `answer = 'NO'`.
- `codice_causale_id`: FK opzionale, indipendente dalla nota. Entrambi possono essere null.
- Il dropdown causali nel FE si popola da `GET /practices/{id}/intake/checklist/causali?categoria=`.

---

## V21 — `attachment`: espansione link_download

**File**: `infra/db/migrations/V21__tecnico_gap_a_attachment_linkdownload.sql`  
**GAP ref**: TECNICO-GAP-A (download documenti da URL remoto) | **Sprint**: 9  
**⚠️ Nota Sprint 4**: la colonna `ingestion_status` (PENDING|AVAILABLE|FAILED) è già stata aggiunta dalla migration V9 (Sprint 4 PATCH GAP-BLOCKER-001).  
Questo script aggiunge **solo** l'espansione di `link_download` da VARCHAR(1024) a VARCHAR(2500).  
Non aggiungere `download_status`: mappato su `ingestion_status` già presente.

```sql
-- ============================================================
-- V21 — TECNICO-GAP-A: espansione link_download da 1024 a 2500 chars
-- NOTA: ingestion_status già aggiunto da V9 (Sprint 4) — non riaggiungere.
-- ============================================================

-- Espansione link_download: da VARCHAR(1024) a VARCHAR(2500)
-- Fonte: CDT BOA_ANC_ContenutiDenuncia — campo LINKDOWNLOAD fino a 2500 chars
ALTER TABLE attachment
    MODIFY COLUMN link_download VARCHAR(2500) NOT NULL
        COMMENT 'URL sorgente originale del documento (LINKDOWNLOAD nel payload JSON BPM — max 2500 chars)';
```

**Colonne lifecycle già presenti (V9 Sprint 4 — non riaggiungere)**:

| Colonna (V9) | Valori | Equivalente GAP |
|---|---|---|
| `ingestion_status` | `PENDING\|AVAILABLE\|FAILED` | `download_status` descritto nel GAP |

**Impatto viewer FE**: se `ingestion_status = 'FAILED'` (colonna già presente da V9) → mostrare placeholder "Documento non disponibile" al posto del viewer inline.

---

## V22 — `task`: aggiunta sla_due_date

**File**: `infra/db/migrations/V22__tecnico_gap_b_task_sla.sql`  
**GAP ref**: TECNICO-GAP-B (SLA task 5 giorni lavorativi) | **Sprint**: 9

```sql
-- ============================================================
-- V22 — TECNICO-GAP-B: campo SLA target su task
-- ============================================================

ALTER TABLE task
    ADD COLUMN sla_due_date DATETIME(3) NULL
        COMMENT 'Data/ora target completamento SLA (5 giorni lavorativi dalla created_at) — solo monitoring, no escalation automatica'
        AFTER completed_at;
```

**Note implementative**:
- Valorizzato dal `SlaTaskListener` Flowable al momento di creazione del task (`event=create`).
- Calcolo: `created_at + 5 giorni lavorativi` (escludendo sabato, domenica; non considera festività).
- Il campo `slaStatus` (`IN_TEMPO | SCADUTO`) è **derivato** nel DTO del Backend (`now > sla_due_date`) — non persistito.
- Non scatena azioni automatiche (confermato dal reverse: `enabled=false` su tutte le deadline Appian).

---

## V19 — `case_note`: note intermediate per pratica

**File**: `infra/db/migrations/V19__tecnico_gap_c_case_note.sql`  
**GAP ref**: TECNICO-GAP-C (note intermediate per case — `BOA_ANC_ENTITY_CASENOTE`) | **Sprint**: 7

```sql
-- ============================================================
-- V19 — TECNICO-GAP-C: note intermediate associate alla pratica
-- (V9 già usato da Sprint 4 — questa è V19)
-- ============================================================

CREATE TABLE case_note (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    practice_id BIGINT       NOT NULL,
    autore      VARCHAR(64)  NOT NULL
        COMMENT 'username dell operatore o SYSTEM',
    testo       TEXT         NOT NULL,
    tipo        VARCHAR(20)  NOT NULL
        COMMENT 'LAVORAZIONE | CAMBIO_STATO | CHIUSURA',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_cn_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    CONSTRAINT chk_cn_tipo    CHECK (tipo IN ('LAVORAZIONE','CAMBIO_STATO','CHIUSURA')),
    KEY idx_cn_practice_time (practice_id, created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Note intermediate pratica (lavorazione + cambi stato + chiusura) — equivalente BOA_ANC_ENTITY_CASENOTE';
```

**Semantica per tipo**:

| `tipo` | Scritto da | Quando |
|---|---|---|
| `LAVORAZIONE` | Operatore (input manuale) | `PUT /intake/checklist` con campo nota |
| `CAMBIO_STATO` | Backend (automatico) | Cambio stato pratica (`svc.changeState`) |
| `CHIUSURA` | Operatore (input manuale) | `POST /intake/close` — nota finale (corrisponde all'attuale `practice_outcome.notes`) |

**Impatto sulla tabella `practice_outcome`**: il campo `notes TEXT` in `practice_outcome` rimane per la nota di esito finale (interoperabilità con BPM). La nota di chiusura viene anche replicata in `case_note.tipo=CHIUSURA` per coerenza storica.

---

## Dipendenze tra Migration

```
V1÷V14 (già applicati — non modificare)
  │
  ├── V15 (bpm_outbound_message — tabella presente da V1, dipende da V1)
  ├── V16 (practice — tabella presente da V1, dipende da V1)
  ├── V17 (checklist_item_catalog — tabella presente da V1, FK self-referencing)
  ├── V18 (ref_causali_checklist nuova + checklist_response FK)
  │     └── checklist_response già presente da V1
  ├── V19 (case_note — nuova tabella, FK → practice da V1)
  ├── V20 (user_task_filter — nuova tabella, FK → app_user da V1)
  ├── V21 (attachment — tabella presente da V1, V9 già estesa in Sprint 4)
  └── V22 (task — tabella presente da V3/V7, dipende da V1÷V7)
```

**Ordine esecuzione per sprint**:
- Sprint 5: V15 → V16 (applicate insieme, dipendenze solo da V1)
- Sprint 7: V17 → V18 → V19 (applicate in sequenza)
- Sprint 8: V20 (standalone)
- Sprint 9: V21 → V22 (standalone, non dipendono tra loro)

---

## Script di Seed Dati POC

> Da applicare dopo V1÷V9, come script separato oppure in Flyway `R__` (repeatable):  
> `infra/db/seeds/R__poc_seed_data.sql`

### Utenti e ruoli

```sql
-- Utenti demo POC
INSERT INTO app_user (username, full_name, email, active) VALUES
    ('operatore1',   'Mario Rossi',      'mario.rossi@poste.it',      1),
    ('operatore2',   'Laura Bianchi',    'laura.bianchi@poste.it',     1),
    ('supervisore1', 'Giuseppe Verdi',   'giuseppe.verdi@poste.it',    1);

-- Ruoli
INSERT INTO role (code, name) VALUES
    ('OPERATORE_ANC',   'Operatore Attivazione Nuova Carta'),
    ('SUPERVISORE_ANC', 'Supervisore Attivazione Nuova Carta');

-- Assegnazione ruoli
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u, role r
WHERE (u.username = 'operatore1'   AND r.code = 'OPERATORE_ANC')
   OR (u.username = 'operatore2'   AND r.code = 'OPERATORE_ANC')
   OR (u.username = 'supervisore1' AND r.code = 'SUPERVISORE_ANC');

-- Gruppi Flowable
INSERT INTO user_group (code, name) VALUES
    ('GRUPPO_OPERATORE_ANC',   'Gruppo Operatori ANC'),
    ('GRUPPO_SUPERVISORE_ANC', 'Gruppo Supervisori ANC');

INSERT INTO user_group_member (user_id, group_id)
SELECT u.id, g.id FROM app_user u, user_group g
WHERE (u.username IN ('operatore1','operatore2') AND g.code = 'GRUPPO_OPERATORE_ANC')
   OR (u.username = 'supervisore1'               AND g.code = 'GRUPPO_SUPERVISORE_ANC');
```

### Checklist item catalog (campione VERBALE)

```sql
-- Campione item checklist per VERBALE (estendere con lista completa da MatriciControlli.xlsx)
INSERT INTO checklist_item_catalog (code, document_type, label, triggers_cascade, ordering, active) VALUES
    ('VERB_DOC_PRESENTE',        'VERBALE', 'Documento presente e leggibile',      1, 1, 1),
    ('VERB_INTESTAZIONE_CORR',   'VERBALE', 'Intestazione corretta',                0, 2, 1),
    ('VERB_FIRMA_PRESENTE',      'VERBALE', 'Firma del dichiarante presente',        0, 3, 1),
    ('VERB_DATI_ANAGRAFICI',     'VERBALE', 'Dati anagrafici conformi',             0, 4, 1),
    ('VERB_DATA_COMPILAZIONE',   'VERBALE', 'Data di compilazione presente',         0, 5, 1);

-- Esempio dipendenza: VERB_FIRMA_PRESENTE è visibile solo se VERB_DOC_PRESENTE = SI
UPDATE checklist_item_catalog
SET id_dipendenza = (SELECT id FROM checklist_item_catalog WHERE code = 'VERB_DOC_PRESENTE'),
    valore_attivo_dipendenza = 'SI'
WHERE code = 'VERB_FIRMA_PRESENTE';

-- Campione item per CARTA
INSERT INTO checklist_item_catalog (code, document_type, label, triggers_cascade, ordering, active) VALUES
    ('CARTA_DOC_PRESENTE',   'CARTA', 'Documento di identità presente e leggibile', 1, 1, 1),
    ('CARTA_FIRMA_PRESENTE', 'CARTA', 'Firma del titolare presente',                 0, 2, 1),
    ('CARTA_DATI_CONFORMI',  'CARTA', 'Dati carta conformi all intestazione',        0, 3, 1);
```

---

## Checklist Verifiche per il Developer

Prima di applicare le migration in un ambiente pulito:

- [ ] V1÷V14 applicati correttamente (Flyway `flyway_schema_history` mostra 14 record success=1)
- [ ] `app_user` esiste con i campi `id`, `username` (FK richiesta da V20)
- [ ] `practice` esiste con `id` (FK richiesta da V16, V19)
- [ ] `bpm_outbound_message` esiste con `correlation_id` (V15 aggiunge colonne retry)
- [ ] `checklist_item_catalog` esiste con `id` (FK self-referencing in V17)
- [ ] `checklist_response` esiste con `item_id`, `note` (V18 aggiunge FK causale)
- [ ] `attachment` esiste con `link_download VARCHAR(1024)` + `ingestion_status` da V9 (V21 espande a 2500)
- [ ] `task` esiste con `completed_at` (V22 aggiunge sla_due_date dopo)

Dopo applicazione:

- [ ] `bpm_outbound_message` ha le colonne: `retry_count`, `max_retry`, `stato_invio`, `response_json`, `error_message`, `last_attempt_at` (V15)
- [ ] `practice` ha la colonna `ticket_id` (V16)
- [ ] `checklist_item_catalog` ha `id_dipendenza` con FK self-referencing e `valore_attivo_dipendenza` (V17)
- [ ] `ref_causali_checklist` è creata con seed campione inserito (V18)
- [ ] `checklist_response` ha `codice_causale_id` con FK su `ref_causali_checklist` (V18)
- [ ] `case_note` è creata con FK su `practice` (V19)
- [ ] `user_task_filter` è creata con FK su `app_user` (V20)
- [ ] `attachment.link_download` è `VARCHAR(2500)` (V21)
- [ ] `task.sla_due_date` è presente (V22)
