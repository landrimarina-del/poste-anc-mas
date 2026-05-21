# Sprint 13 — Piano di Esecuzione
## Checklist Avanzata: Causali KO + Note Lavorazione

**Data**: 2026-05-21  
**Sprint**: 13 / 16  
**Owner**: develop-coordinator  
**Status**: IN_PROGRESS

---

## Precondizioni verificate

| Check | Stato |
|---|---|
| Sprint 11 GO | ✅ |
| Sprint 12 GO + bug fix | ✅ |
| Flyway V1÷V14, V101, V102 success=1 | ✅ |
| Stack 7 container healthy | ✅ |
| Ultima migration applicata: V102 | ✅ |
| Prossima migration disponibile: V103 | ✅ |

---

## ⚠️ Discrepanza Schema — GAP-US-08 / V17

**Rilevata dal Coordinator**: La migration V17 del GAP-DBA.md esegue `ALTER TABLE checklist_item_catalog` ma tale tabella **non esiste** nel DB del POC. Il POC usa un modello **flat** con due tabelle separate: `checklist_verbale` e `checklist_carta` (introdotte da V10/V11).

**Impatto**:
- AC-S7-01 (V17 checklist_item_catalog) → **N/A per POC**
- AC-S7-04 (campo `visible` per item) → **N/A per POC** (la visibilità condizionale è già embedded nelle business rules del backend flat)
- D7-FE-1 (filtro item per visible) → **N/A per POC** (i campi sono già condizionali in UI)

**Adattamento approvato**:
- V103 → adattato da V18: CREATE `ref_causali_checklist` + ALTER `checklist_verbale` ADD `codice_causale_id` + ALTER `checklist_carta` ADD `codice_causale_id`
- V104 → adattato da V19: CREATE `case_note` (verbatim GAP-DBA)
- V17 originale → **saltato** (tabella non presente)

**Segnalato in**: CrossAgent_Issues_Sprint_13.md

---

## Scope Sprint 13 (adattato)

### Migration Flyway

| Migration POC | Da GAP-DBA | Contenuto |
|---|---|---|
| V103 | V17+V18 adattato | CREATE `ref_causali_checklist` + ALTER `checklist_verbale` ADD `codice_causale_id` FK + ALTER `checklist_carta` ADD `codice_causale_id` FK + SEED causali |
| V104 | V19 verbatim | CREATE `case_note` (practice_id FK, autore, testo, tipo CHECK, created_at) |

### Backend

| ID | Deliverable | GAP ref |
|---|---|---|
| D13-BE-1 | `GET /practices/{id}/intake/checklist/causali?categoria=` — nuovo endpoint | GAP-US-11, GAP_Arch §705 |
| D13-BE-2 | `PUT /practices/{id}/intake/checklist` — accettare `codiceCausaleId` per VERBALE (campo opzionale) | GAP-US-11, GAP_Arch §710 |
| D13-BE-3 | `PUT /practices/{id}/intake/checklist` — CARTA: accettare `codiceCausaleId` opzionale su `cardConformityOk=false` | GAP-US-11 adattato |
| D13-BE-4 | `GET /practices/{id}/notes` — lista note intermediate ordinate per created_at DESC | TECNICO-GAP-C, GAP_Arch §1107 |
| D13-BE-5 | `POST /practices/{id}/notes` — crea nota tipo LAVORAZIONE | TECNICO-GAP-C, GAP_Arch §1108 |

### Frontend

| ID | Deliverable | GAP ref |
|---|---|---|
| D13-FE-1 | VERBALE checklist: per item con risposta `NO` → area espandibile con textarea nota + dropdown causale (da API `causali`) | GAP-US-11, GAP-UI §2.10 |
| D13-FE-2 | CARTA checklist: se `cardConformityOk=false` → area espandibile con textarea nota + dropdown causale CARTA | GAP-US-11 adattato |
| D13-FE-3 | Step Riepilogo: sezione "Note di Lavorazione" — lista note + form aggiunta nota libera (tipo LAVORAZIONE) | TECNICO-GAP-C |

---

## Acceptance Criteria Sprint 13 (adattati per POC)

| AC | Condizione | Owner |
|---|---|---|
| AC-S13-01 | V103 applicata: `ref_causali_checklist` creata con seed 6 causali (3 VERBALE + 3 CARTA) | DBA/BE |
| AC-S13-02 | V103 applicata: `checklist_verbale.codice_causale_id` + `checklist_carta.codice_causale_id` presenti con FK | DBA/BE |
| AC-S13-03 | V104 applicata: `case_note` creata con CHECK tipo IN ('LAVORAZIONE','CAMBIO_STATO','CHIUSURA') | DBA/BE |
| AC-S13-04 | `GET /practices/6/intake/checklist/causali?categoria=VERBALE` → 200 con array ≥3 causali VERBALE | BE |
| AC-S13-05 | `GET /practices/6/intake/checklist/causali?categoria=CARTA` → 200 con array ≥3 causali CARTA | BE |
| AC-S13-06 | `PUT /practices/6/intake/checklist` con `codiceCausaleId` valorizzato → 200, persiste su `codice_causale_id` | BE |
| AC-S13-07 | `GET /practices/6/notes` → 200 con array (inizialmente vuoto) | BE |
| AC-S13-08 | `POST /practices/6/notes` con `{testo:"test", tipo:"LAVORAZIONE"}` → 201, nota creata | BE |
| AC-S13-09 | FE VERBALE: risposta NO su un item → area espandibile visibile con textarea + dropdown causali | FE |
| AC-S13-10 | FE CARTA: `cardConformityOk=false` → area espandibile con textarea + dropdown causali CARTA | FE |
| AC-S13-11 | FE Riepilogo: sezione Note di Lavorazione visibile con form aggiunta | FE |
| AC-S13-12 | Stack 7 container healthy al termine | QA |

---

## Sequenza di Esecuzione

```
Stream DBA/BE (develop-backend):
  1. Migration V103 (ref_causali_checklist + alter verbale/carta)
  2. Migration V104 (case_note)
  3. D13-BE-1 endpoint causali
  4. D13-BE-2/3 PUT checklist con codiceCausaleId
  5. D13-BE-4/5 GET/POST notes

Stream FE (develop-frontend):  [dopo D13-BE-1 disponibile]
  6. D13-FE-1 VERBALE item KO espandibile
  7. D13-FE-2 CARTA conformità KO espandibile
  8. D13-FE-3 Note di Lavorazione in Riepilogo

Stream QA (develop-qa):  [dopo deploy completo]
  9. Verifica AC-S13-01÷12
```

---

## Dipendenze

- V102 già applicata ✅
- `checklist_verbale` e `checklist_carta` presenti ✅
- `practice` con FK per `case_note` presente ✅
- Mock BPM in modalità KO (non impatta Sprint 13)
