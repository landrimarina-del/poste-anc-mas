# Sprint Execution Plan — Sprint 12

> **Titolo**: Lavorazione Task — Step e Sidebar  
> **Sprint**: 12  
> **Data**: 2026-05-21  
> **Owner**: develop-coordinator  
> **Stato**: IN_PROGRESS

---

## 1. Scope

Sprint 12 implementa la navigazione multi-step del task di lavorazione:

- Sidebar collassabile (NARROW / EXTRA_NARROW) con 3 step fissi
- Step "Riepilogo" condizionalmente abilitato (dipende da `esitoSD`)
- Barra di avanzamento fasi (MilestoneBar / PhaseProgressBar)
- Bottoni footer condizionali (Salva e prosegui / chiudi pratica / Modifica)

**Nessuna migration DB** prevista in questo sprint.

**GAP coperti**: GAP-US-03, GAP-US-04, GAP-US-06

---

## 2. Precondizioni

| Precondizione | Stato |
|---|---|
| Sprint 11 completato con GO | ✅ |
| Flyway V1÷V14, V101, V102 applicati success=1 | ✅ |
| Stack 7 container healthy (anc-db, anc-minio, anc-bpm-outbound-stub, anc-bpm-stub, anc-backend, anc-frontend, anc-reverse-proxy) | ✅ |
| Mock BPM stub in modalità KO (default per i test) | ✅ |

---

## 3. Stream di Lavoro

### STREAM-1: Backend (develop-backend)

**Task S12-BE-1** — Aggiungere `intakeStep` al response di `GET /tasks/{id}`

**GAP-ID**: GAP-US-03  
**Ref spec**: GAP_Architettura.md §GAP-US-03 — Impatto Backend  

Derivazione:
- `document_type IS NULL` → `intakeStep = "VERIFICA"`
- `document_type IS NOT NULL` e checklist non consolidata → `intakeStep = "CHECKLIST"`
- checklist `stato = BOZZA` → `intakeStep = "RIEPILOGO"`

**AC da verificare**: `GET /tasks/{id}` restituisce il campo `intakeStep` valorizzato correttamente in base allo stato della pratica.

---

**Task S12-BE-2** — Aggiungere `sidebarState` al response di `GET /tasks/{id}`

**GAP-ID**: GAP-US-04  
**Ref spec**: GAP_Architettura.md §GAP-US-04 — Impatto Backend  

Struttura attesa:
```json
{
  "sidebarState": {
    "currentStep": "VERIFICA_DOCUMENTO",
    "steps": [
      { "id": "DATI_PRATICA",       "label": "Dati Pratica",       "enabled": true,  "completed": true  },
      { "id": "VERIFICA_DOCUMENTO", "label": "Verifica Documento",  "enabled": true,  "completed": false },
      { "id": "RIEPILOGO",          "label": "Riepilogo",           "enabled": false, "completed": false }
    ]
  }
}
```

Regola: `RIEPILOGO.enabled = true` se `checklist_response.stato IN (BOZZA, RIAPERTA, CONSOLIDATA)`, `false` altrimenti.

**AC da verificare**: `GET /tasks/{id}` restituisce `sidebarState.steps[2].enabled = false` se esitoSD null; `= true` se esitoSD valorizzato.

---

**Task S12-BE-3** — Aggiungere campo `fase` al response di `GET /practices/{id}`

**GAP-ID**: GAP-US-06  
**Ref spec**: GAP_Architettura.md §GAP-US-06 — Impatto Backend  

Regola derivazione:
- `stato = APERTA` → `fase = "RACCOLTA_INPUT"`
- `stato = IN_LAVORAZIONE` → `fase = "LAVORAZIONE"`
- `stato IN (IN_ATTESA_CONFERMA_BPM, CHIUSA_OK, CHIUSA_KO)` → `fase = "CHIUSURA_PRATICA"`

**AC da verificare**: `GET /practices/{id}` per pratica IN_LAVORAZIONE → `fase = "LAVORAZIONE"`.

---

### STREAM-2: Frontend (develop-frontend)

> **Dipendenza**: STREAM-1 deve essere completato prima di avviare STREAM-2 per i componenti che consumano `sidebarState` e `fase`.

**Task S12-FE-1** — Componente `WorkflowSidebar`

**GAP-ID**: GAP-US-04  
**Ref spec**:
- GAP_Architettura.md §GAP-US-04 — Impatto Frontend
- GAP-UX.md §4.1 — Sidebar Navigazione
- GAP-UI.md §2.4 — Task Lavorazione

Specifiche:
- 3 voci fisse: `briefcase "Dati Pratica"` | `check-square-o "Verifica Documento"` | `address-card-o "Riepilogo"`
- Toggle collapse: icona `angle-double-left` (espanso) / `angle-double-right` (collassato)
- Stato NARROW (label + icone) / EXTRA_NARROW (solo icone)
- Stato collapse persistito in `localStorage`
- Voce 3 "Riepilogo": cliccabile solo se `sidebarState.steps[2].enabled = true`; altrimenti grigio/disabled
- Colore voce attiva: ACCENT `#0047BB`

**AC da verificare**:
- AC-S6-03: voce "Riepilogo" non cliccabile (grigio/disabled) finché esitoSD null
- AC-S6-04: voce "Riepilogo" cliccabile dopo esitoSD valorizzato
- AC-S6-09: toggle angle-double-left/right → NARROW ↔ EXTRA_NARROW

---

**Task S12-FE-2** — Componente `VerificaDocumentiStep` (Step 0 — read-only)

**GAP-ID**: GAP-US-03  
**Ref spec**:
- GAP_Architettura.md §GAP-US-03 — Impatto Frontend
- GAP-UI.md §2.5 — Verifica Documenti

Specifiche:
- Riusa componenti esistenti `SezioneDatiCliente`, `SezioneDatiCarta`, `SezioneDocumenti` in modalità read-only
- Button bar in cima: "nascondi allegati"/"mostra allegati" (icon: eye-slash/eye) | "nascondi sezione"/"mostra sezione"
- Layout 2 colonne: col-sx (dati cliente + carta + contenuti + checklist readonly) | col-dx (allegati/viewer)
- Questa è la sezione `activeSection=2` del task lavorazione prima della tipizzazione

**AC da verificare**: Dati cliente, carta, allegati visibili in read-only nello step Verifica Documento.

---

**Task S12-FE-3** — Componente `ClassificazioneStep` (Step 1 — tipizzazione con confirm dialog)

**GAP-ID**: GAP-US-03  
**Ref spec**:
- GAP-UX.md §2.2 e §7.1
- GAP-UI.md §2.11 — Task Tipizzazione

Specifiche:
- Layout 2 colonne: col-sx (dropdown tipo documento + bottone "Conferma") | col-dx (preview documento)
- Bottone "Conferma": disabled se nessun tipo selezionato
- Confirm dialog prima dell'invio:
  - Header: "ATTENZIONE"
  - Body: "E' stato selezionato {tipo} come tipologia. Attenzione: non sarà possibile modificare il tipo documento in futuro. Confermare la selezione?"
  - Bottoni: [ANNULLA] chiude dialog | [CONFERMA] invia POST /intake/typing

**AC da verificare**: AC-S6-07 — dialog mostra testo verbatim "ATTENZIONE / non sarà possibile modificare il tipo documento".

---

**Task S12-FE-4** — Componente `PhaseProgressBar` / `MilestoneBar`

**GAP-ID**: GAP-US-06  
**Ref spec**:
- GAP_Architettura.md §GAP-US-06 — Impatto Frontend
- GAP-UI.md §6 (MilestoneBar)
- GAP-UX.md §8 (milestoneField)

Specifiche:
- 3 fasi fisse: `RACCOLTA_INPUT → LAVORAZIONE → CHIUSURA_PRATICA`
- Fase corrente evidenziata: colore ACCENT `#0047BB`
- Fasi precedenti: completed (checkmark o colore diverso)
- Non cliccabile
- Riutilizzato in: header `DettaglioPratica` + task `TaskLavorazione`
- Consume campo `fase` da `GET /practices/{id}`

**AC da verificare**: AC-S6-08 — MilestoneBar mostra fase corrente in ACCENT (#0047BB) e fasi precedenti come completed.

---

**Task S12-FE-5** — Logic bottoni footer `TaskLavorazione`

**GAP-ID**: GAP-US-03, GAP-US-04  
**Ref spec**:
- GAP-UX.md §4.3 — Bottoni nel Footer del Task
- GAP-UX.md §6 — Regole di Navigazione Condizionale N-01÷N-05
- GAP-UI.md §2.4 — Footer ButtonLayout

Specifiche:

| Bottone | ShowWhen | Disabled | Azione |
|---|---|---|---|
| "Salva e prosegui" | `activeSection = 2` | `esitoSD IS NOT NULL` | POST → activeSection = 3 |
| "chiudi pratica" | `activeSection = 3` | mai | POST /intake/close |
| "Modifica" | sempre | mai | POST reset esitoSD → riabilita step 2 |

**AC da verificare**:
- AC-S6-05: "Salva e prosegui" visible solo su step 2, disabled se esitoSD già valorizzato
- AC-S6-06: "chiudi pratica" visible solo su step 3

---

### STREAM-3: QA (develop-qa)

> **Dipendenza**: STREAM-1 e STREAM-2 completati.

**Task S12-QA-1** — Smoke test e verifica AC

Verificare tutti gli AC dello sprint:

| AC | Verifica |
|---|---|
| AC-S6-01 | `GET /tasks/{id}` → response ha `intakeStep` e `sidebarState.steps[2].enabled = false` se esitoSD null |
| AC-S6-02 | `GET /practices/{id}` → `fase = "LAVORAZIONE"` per pratica IN_LAVORAZIONE |
| AC-S6-03 | UI: voce "Riepilogo" sidebar non cliccabile prima di "Salva e prosegui" |
| AC-S6-04 | UI: voce "Riepilogo" cliccabile dopo esitoSD valorizzato |
| AC-S6-05 | UI: "Salva e prosegui" visible su step 2, disabled se esitoSD già valorizzato |
| AC-S6-06 | UI: "chiudi pratica" visible solo su step 3 |
| AC-S6-07 | UI: dialog tipizzazione testo verbatim "ATTENZIONE / non sarà possibile modificare il tipo documento" |
| AC-S6-08 | UI: MilestoneBar fase corrente in `#0047BB`, fasi precedenti completed |
| AC-S6-09 | UI: sidebar toggle angle-double-left/right → NARROW ↔ EXTRA_NARROW |

---

## 4. Sequence e Dipendenze

```
STREAM-1 (Backend)
  S12-BE-1  ──────────────────────────────────────┐
  S12-BE-2  ──────────────────────────────────────┤
  S12-BE-3  ──────────────────────────────────────┤
                                                   ↓
STREAM-2 (Frontend)                      [STREAM-1 OK]
  S12-FE-1  ───────────────────────────────────────┐
  S12-FE-2  ───────────────────────────────────────┤
  S12-FE-3  ───────────────────────────────────────┤
  S12-FE-4  ───────────────────────────────────────┤
  S12-FE-5  ───────────────────────────────────────┤
                                                    ↓
STREAM-3 (QA)                            [STREAM-2 OK]
  S12-QA-1
```

---

## 5. Acceptance Criteria Sprint 12 (riepilogo)

| AC | Fonte GAP | Condizione |
|---|---|---|
| AC-S6-01 | GAP-US-03, GAP-US-04 | `GET /tasks/{id}` restituisce `intakeStep` e `sidebarState` con `steps[2].enabled = false` se esitoSD null |
| AC-S6-02 | GAP-US-06 | `GET /practices/{id}` → `fase = "LAVORAZIONE"` per pratica IN_LAVORAZIONE |
| AC-S6-03 | GAP-US-04, UX-GAP §N-01 | Sidebar: voce "Riepilogo" non cliccabile (grigio/disabled) fino a "Salva e prosegui" |
| AC-S6-04 | GAP-US-04, UX-GAP §N-02 | Sidebar: voce "Riepilogo" cliccabile dopo esitoSD valorizzato |
| AC-S6-05 | UX-GAP §N-03, N-05 | "Salva e prosegui" visible solo su step 2, disabled se esitoSD già valorizzato |
| AC-S6-06 | UX-GAP §N-04 | "chiudi pratica" visible solo su step 3 |
| AC-S6-07 | GAP-UX §7.1 | Dialog tipizzazione testo verbatim "ATTENZIONE / non sarà possibile modificare il tipo documento" |
| AC-S6-08 | GAP-US-06, GAP-UI §6 | MilestoneBar mostra fase corrente in ACCENT (#0047BB), fasi precedenti completed |
| AC-S6-09 | GAP-US-04, GAP-UX §4.1 | Sidebar collassabile: toggle → NARROW ↔ EXTRA_NARROW |

---

## 6. Output Attesi

- Backend build Docker aggiornata con nuovi campi `intakeStep`, `sidebarState`, `fase`
- Frontend rebuild con `WorkflowSidebar`, `PhaseProgressBar`, `VerificaDocumentiStep`, `ClassificazioneStep`, footer condizionale
- Stack 7 container healthy al termine
- Sprint_Status_Sprint_12.md con esito GO
