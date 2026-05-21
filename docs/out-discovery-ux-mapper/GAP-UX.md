# GAP-UX — Specifica Navigazione e Flussi Utente

> **Scopo**: Documento di riferimento UX per il gruppo di sviluppo frontend.  
> Definisce routing, flussi di navigazione, user journey e regole di transizione schermata  
> ricavati dal reverse engineering dell'applicazione Appian originale (`05_ux-ui.md`).  
>
> **Fonte di verità**: `docs/reverse/attivazione-nuova-carta/v20260506/05_ux-ui.md`  
> **Data analisi reverse**: 2026-05-20 | **Data documento**: 2026-05-21  
> **Target tecnologico**: React SPA + React Router + Flowable human task

---

## 1. Struttura Applicativa e Routing

### 1.1 Due Applicazioni per Ruolo

L'applicazione originale ha **due siti separati** con URL distinte. Nella POC React SPA devono essere implementati come due layout distinti condizionati dal ruolo utente.

| Sito Originale | Ruolo | URL Stub Originale | Route React equivalente | Layout |
|---|---|---|---|---|
| Scrivania Digitale Operatore ANC | Operatore ANC | `/scrivania-digitale-operatore-anc` | `/operatore` | `OperatoreLayout` |
| Scrivania Digitale Supervisore ANC | Supervisore ANC | `/scrivania-digitale-supervisore-anc` | `/supervisore` | `SupervisoreLayout` |

**Regola**: al login, il redirect automatico avviene verso `/operatore/home` o `/supervisore/home` in base al ruolo.

---

### 1.2 Mappa Routing Completa

```
/operatore
  /home                          → DashboardOperatore
  /attivita                      → ListaAttivita
  /pratiche                      → ListaPratiche (RecordType)
  /pratiche/:id                  → DettaglioPratica (Summary)
  /pratiche/:id/tab/:tab         → DettaglioPratica con tab attivo (Pratica|Lavorazione|Esito)
  /task/:taskId                  → TaskLavorazione (form multi-step)
  /task/:taskId/tipizzazione     → TaskTipizzazione (form separato — avviato da processo)

/supervisore
  /home                          → DashboardSupervisore
  /pratiche                      → ListaPratiche (RecordType — stesso componente)
  /pratiche/:id                  → DettaglioPratica (Summary — stesso componente)
  /riassegna-attivita            → RiassegnazioneTask (avviato da processo)
```

---

### 1.3 Struttura Topbar (Navbar)

Entrambi i siti hanno topbar con stile `TOPBAR`. La topbar **non mostra il nome del sito** (`showName: false`).

**Logo**: `https://www.poste.it/img/1453895043057/2X/logo-poste-italiane.png` (alt: "PosteItaliane")

**Voci Navbar Operatore**:

| Voce | Icon (FontAwesome) | Route |
|---|---|---|
| Home | `fa-home` (f015) | `/operatore/home` |
| Attività | `fa-list-ul` (f0ca) | `/operatore/attivita` |
| Pratiche | `fa-file` (f016) | `/operatore/pratiche` |

**Voci Navbar Supervisore**:

| Voce | Icon (FontAwesome) | Route |
|---|---|---|
| Home | `fa-home` (f015) | `/supervisore/home` |
| Pratiche | `fa-file` (f016) | `/supervisore/pratiche` |
| Riassegna Attività | `fa-group` (f0c0) | `/supervisore/riassegna-attivita` |

**Nota sviluppo**: tutte le pagine hanno width `WIDE`. Il contenuto si estende a piena larghezza all'interno del layout.

---

## 2. User Journey — Operatore ANC

### 2.1 Flusso Principale: Lavorazione Pratica

```
[LOGIN] → /operatore/home
                │
                │ (sidebar "Attività" o da lista attività)
                ▼
[/operatore/attivita]   Lista Attività
                │
                │ click riga nella griglia attività
                ▼
[/operatore/task/:taskId]   Task Lavorazione ← FORM MULTI-STEP
                │
                ├── TAB 1: Dati Pratica      (sidebar voce 1)
                │         └── readonly
                │
                ├── TAB 2: Verifica Documento (sidebar voce 2)
                │         ├── dati cliente + carta + contenuti + checklist (sx)
                │         └── allegati/viewer (dx)
                │         └── [BUTTON] "Salva e prosegui" → sblocca TAB 3
                │
                └── TAB 3: Riepilogo         (sidebar voce 3 — cliccabile solo dopo "Salva e prosegui")
                          ├── esito calcolato + motivazioni
                          └── [BUTTON] "chiudi pratica" → POST /intake/close → pratica IN_ATTESA_CONFERMA_BPM
```

### 2.2 Flusso Tipizzazione Documento (separato)

```
[/operatore/task/:taskId]   Task Lavorazione
          │
          │ (se documento non ancora tipizzato — processo separato avviato automaticamente)
          ▼
[/operatore/task/:taskId/tipizzazione]   Task Tipizzazione
          ├── col-sx: dropdown tipo documento + [BUTTON] "Conferma" (con dialog di conferma)
          └── col-dx: preview documento
          │
          │ su "Conferma"  → POST /intake/typing → irreversibile
          ▼
[/operatore/task/:taskId]   torna a Task Lavorazione (con tipo documento impostato)
```

### 2.3 Flusso Consultazione Pratica

```
[/operatore/pratiche]   Lista Pratiche
          │
          │ click su "Pratica N." (link) o click riga
          ▼
[/operatore/pratiche/:id]   Dettaglio Pratica (Summary)
          ├── TAB "Dati Pratica"     → sezione dati pratica readonly
          ├── TAB "Dati Lavorazione" → sezione verifica documenti readonly
          └── TAB "Esito"            → esito SD + data esito + note (se presenti)
```

### 2.4 Flusso Filtri Salvati Lista Attività

```
[/operatore/attivita]
  ├── form filtri → [BUTTON] "Applica Filtri" → filtra griglia (no save)
  ├── form filtri → [BUTTON] "Applica e Salva Filtri" → POST /tasks/filters/saved + filtra griglia
  └── griglia "Ultimi N Filtri Salvati" → click riga → popola form filtri (no apply automatico)
                                                       → poi "Applica Filtri" per applicare
```

---

## 3. User Journey — Supervisore ANC

### 3.1 Flusso Riassegnazione Task

```
[LOGIN] → /supervisore/home
                │
                │ (topbar "Riassegna Attività")
                ▼
[/supervisore/riassegna-attivita]   RiassegnazioneTask
                ├── BOX "Dettagli riassegnazione"
                │     ├── radio: "Riassegna al Gruppo Operatore"
                │     └── radio: "Riassegna a Utenti" → picker utenti (visibile solo se scelto)
                ├── Filtri attività (per filtrare la griglia)
                ├── Griglia attività (multi-selezione)
                └── [BUTTON] "Riassegna" → POST /supervision/tasks/{id}/reassign-group o reassign-user
```

### 3.2 Flusso Consultazione Pratiche (uguale a Operatore)

Il Supervisore accede alla stessa lista e al dettaglio pratica. Non ha accesso al task di lavorazione (non compare nella worklist del Supervisore).

---

## 4. Task Lavorazione — Navigazione Multi-Step (dettaglio)

### 4.1 Sidebar Navigazione

La sidebar è la struttura di navigazione del task di lavorazione. Ha 3 voci fisse.

```
SIDEBAR (col-sinistra)
├── [TOGGLE COLLAPSE] richTextIcon angle-double-left / angle-double-right
│     → sidebar passa da NARROW a EXTRA_NARROW (icone sole senza label)
│
├── [1] icon: briefcase      "Dati Pratica"        → activeSection = 1
├── [2] icon: check-square-o "Verifica Documento"  → activeSection = 2
└── [3] icon: address-card-o "Riepilogo"           → activeSection = 3
          ABILITATO SOLO SE: pratica.esitoSD IS NOT NULL
          (= solo dopo "Salva e prosegui")
          DISABILITATO: mostra in stile STANDARD (grigio) → non cliccabile
```

### 4.2 Regola di Abilitazione Step 3

La navigazione al tab "Riepilogo" è condizionata:

```
pratica.esitoSD IS NOT NULL  →  voce 3 cliccabile  (ACCENT, navigabile)
pratica.esitoSD IS NULL      →  voce 3 non cliccabile  (STANDARD, grigio, disabled)
```

Questa condizione è identica a `checklist_response.stato = BOZZA` nell'architettura OSS.  
**Implementazione React**: il campo derivato `sidebarState.steps[2].enabled` (da `GET /tasks/{id}`)  
controlla la navigabilità della voce 3 (vedere `GAP_Architettura.md §GAP-US-04`).

### 4.3 Bottoni nel Footer del Task

I bottoni del task cambiano in base allo step attivo:

| Bottone | Tipo | ShowWhen | Disabled | Azione |
|---|---|---|---|---|
| "Salva e prosegui" | PRIMARY | `activeSection = 2` | `pratica.esitoSD IS NOT NULL` | POST SalvataggioDati → activeSection = 3 |
| "chiudi pratica" | PRIMARY/SUBMIT | `activeSection = 3` | mai | POST /intake/close → chiude pratica |
| "Modifica" | SECONDARY | sempre | mai | POST reset esitoSD → riabilita sezione 2 |

> **"Modifica"** è presente sempre (anche nella sidebar): consente di tornare indietro dallo step 3 a step 2. Resetta `esitoSD` e riabilita la checklist.

---

## 5. Mappa Completa Interazioni (Tutti gli Schermi)

| Schermata | Elemento interattivo | Tipo | Effetto / Route target |
|---|---|---|---|
| DashboardOperatore | Link azione (forEach `nomeAzione`) | start-process | Avvia processo dinamico del gruppo |
| DashboardOperatore | Icona "edit" link favorito | toggle | Apre card form inline gestione link |
| DashboardOperatore | Icona "remove" link favorito | delete | Rimuove link favorito |
| DashboardOperatore | Icona "plus-square-o" Aggiungi link | toggle | Apre card form inline nuovo link |
| DashboardOperatore | Button "Esci" (card form) | toggle | Chiude card form (no save) |
| DashboardOperatore | Button "Salva" (card form) | submit | POST salva link favorito → reload |
| DashboardOperatore | Testo link favorito | navigate | `safeLink` → URL favorito (interno/esterno/legacy) |
| ListaAttivita | Button "Applica Filtri" | filter | Aggiorna griglia attività (no save) |
| ListaAttivita | Button "Applica e Salva Filtri" | submit | POST /tasks/filters/saved + aggiorna griglia |
| ListaAttivita | Riga griglia filtri salvati | select | Popola form filtri (NO apply automatico) |
| ListaAttivita | Checkbox "Visualizza attività a me assegnate" | filter | Filtra griglia per utente corrente |
| ListaAttivita | Riga griglia attività | navigate | → `/operatore/task/:taskId` |
| TaskLavorazione | Voce sidebar "Dati Pratica" | tab-navigate | activeSection = 1 |
| TaskLavorazione | Voce sidebar "Verifica Documento" | tab-navigate | activeSection = 2 |
| TaskLavorazione | Voce sidebar "Riepilogo" (se abilitata) | tab-navigate | activeSection = 3 |
| TaskLavorazione | Icona collapse/expand sidebar | toggle | Sidebar NARROW ↔ EXTRA_NARROW |
| TaskLavorazione | Button "Salva e prosegui" | submit | POST → activeSection = 3 |
| TaskLavorazione | Button "chiudi pratica" | submit+validate | POST /intake/close → fine task |
| TaskLavorazione | Button "Modifica" | submit | POST reset esitoSD → step 2 riabilitato |
| VerificaDocumenti | Button "nascondi/mostra allegati" | visibility-toggle | col-destra visibile/nascosta |
| VerificaDocumenti | Button "nascondi/mostra sezione" | visibility-toggle | col-sinistra visibile/nascosta |
| SezioneDocumenti | Link "QUI" (download fallback) | start-process | POST download singolo documento |
| TaskTipizzazione | Button "Conferma" | submit+confirm-dialog | → dialog modale "ATTENZIONE" → POST /intake/typing |
| DettaglioPratica | Tab "Dati Pratica" | tab-navigate | `selezione = "Pratica"` |
| DettaglioPratica | Tab "Dati Lavorazione" | tab-navigate | `selezione = "Video"` |
| DettaglioPratica | Tab "Esito" | tab-navigate | `selezione = "Esito"` |
| ListaPratiche | Link "Pratica N." (recordLink) | navigate | → `/pratiche/:id` |
| ListaPratiche | Click riga | navigate | → `/pratiche/:id` |
| RiassegnazioneTask | Radio "Riassegna al Gruppo" | radio-select | Nasconde picker utenti |
| RiassegnazioneTask | Radio "Riassegna a Utenti" | radio-select | Mostra picker utenti (required) |
| RiassegnazioneTask | Riga griglia attività | multi-select | Accumula selezione (checkbox multi) |
| RiassegnazioneTask | Button "Riassegna" | submit | POST /supervision/tasks/{id}/reassign-* |

---

## 6. Regole di Navigazione Condizionale

Queste regole **devono essere implementate lato frontend** esattamente come nel reverse.

| Regola | Condizione | Effetto |
|---|---|---|
| N-01 | `pratica.esitoSD IS NULL` | Voce sidebar "Riepilogo" → non cliccabile (disabled/gray) |
| N-02 | `pratica.esitoSD IS NOT NULL` | Voce sidebar "Riepilogo" → cliccabile |
| N-03 | `pratica.esitoSD IS NOT NULL` | Button "Salva e prosegui" → disabled |
| N-04 | `activeSection ≠ 3` | Button "chiudi pratica" → hidden |
| N-05 | `activeSection ≠ 2` | Button "Salva e prosegui" → hidden |
| N-06 | `local!processo IS NULL` | BOX "Dettagli riassegnazione" → visibile ma radio + picker nascosti |
| N-07 | `tipoRiassegnazione ≠ "Utenti"` | Picker utenti riassegnazione → hidden |
| N-08 | `tipoRiassegnazione = "Utenti"` | Picker utenti riassegnazione → visible + required |
| N-09 | `attachments.length = 0` | Sezione allegati → card info "Nessun contenuto associato" |
| N-10 | `attachments.length > 0` | Sezione allegati → mostra lista file con viewer/download |
| N-11 | `contenuti IS NULL` (tipizzazione) | Form tipizzazione → hidden, card info visibile |
| N-12 | `contenuti IS NOT NULL` (tipizzazione) | Form tipizzazione → visible, card info hidden |
| N-13 | `local!tipoDocSelected IS NULL` | Button "Conferma" tipizzazione → disabled |
| N-14 | `local!myTasksTipoProcesso IS NULL` | Checkbox "Visualizza attività a me assegnate" → disabled |
| N-15 | `isValidURL(link) = false` | Campo "Link" favoriti → validazione "Inserire un link valido." |

---

## 7. Dialog Modale di Conferma

### 7.1 Conferma Tipizzazione Documento

Al click di "Conferma" nella schermata Tipizzazione, **prima di inviare la richiesta** deve comparire un dialog modale di conferma.

```
DIALOG — Conferma Tipizzazione
Header:  "ATTENZIONE"
Body:    "E' stato selezionato {tipoDocumentoSelezionato.descrizione} come tipologia.
          Attenzione: non sarà possibile modificare il tipo documento in futuro.
          Confermare la selezione?"
Button:  [ANNULLA]  → chiude dialog, non invia
Button:  [CONFERMA] → invia POST /intake/typing
```

**Vincolo**: il bottone "Conferma" è disabilitato se nessun tipo documento è selezionato (`tipoDocSelected IS NULL`).

---

## 8. Screen Flow BOA_ANC_Summary — Milestone Header

Il componente `BOA_ANC_Header` (header del dettaglio pratica) contiene un **`milestoneField`** che visualizza gli stati della pratica in sequenza.

```
Header Pratica_ANC
├── [billboard SHORT, fullOverlay]
│   ├── milestoneField → stati pratica in sequenza (progress bar)
│   └── info pratica (2 colonne):
│       ├── col-sx: avatar app + "Attivazione Nuova Carta" + "Pratica N. {n}"
│       └── col-dx: stato pratica + canale + CF cliente
```

Il `milestoneField` corrisponde alla **linea di avanzamento fasi** documentata in `GAP_Architettura.md §GAP-US-06`:  
`RACCOLTA_INPUT → LAVORAZIONE → CHIUSURA_PRATICA`

---

## 9. Elementi UX Non Coperti nei Documenti Architetturali Precedenti

| ID | Elemento | Fonte Reverse | Documento arch | Azione richiesta |
|---|---|---|---|---|
| UX-GAP-01 | Box "Azioni" in dashboard Operatore (forEach azioni gruppo — dinamico) | `05_ux-ui.md §SCREEN DashBoard_Operatore` | Non documentato | Implementare: le azioni sono configurate per gruppo utente. In POC: lista statica azioni per il gruppo `GRUPPO_OPERATORE_ANC`. |
| UX-GAP-02 | Box "Link Favoriti" in dashboard Operatore (CRUD inline) | `05_ux-ui.md §SCREEN DashBoard_Operatore` | `09_Mapping_Architetturale.md C8.3` (opzionale) | Implementare come feature opzionale. API: `GET/POST/DELETE /users/me/favorites`. |
| UX-GAP-03 | Checkbox "Visualizza le attività a me assegnate" | `05_ux-ui.md §BOA_ANC_ListaAttivita` | Non documentato | Aggiungere a `GET /tasks` il filtro `?assignedToMe=true`. |
| UX-GAP-04 | Dashboard Supervisore: colore banner `#FFEC00` (giallo) distingue visivamente da Operatore (`#0047BB` blu) | `05_ux-ui.md §STEP 1` | Non documentato | Implementare nel CSS dei due layout. |
| UX-GAP-05 | `BOA_ANC_Sezione_IndirizzoResidenza_DatiCliente` — sezione indirizzo residenza embedded in Dati Cliente | `05_ux-ui.md §Note Operative` | Non documentato nei dettagli | Implementare come componente figlio di DatiCliente: campi via, civico, CAP, comune, provincia, nazione. |
| UX-GAP-06 | Button "Modifica" nel task lavorazione → resetta `esitoSD` e riabilita step 2 | `05_ux-ui.md §BOA_ANC_Task_Lavorazione` | `05_API_Candidate.md` non ha endpoint "reset" | Aggiungere: `POST /practices/{id}/intake/checklist/edit` (già in arch) con semantic di reset. |
| UX-GAP-07 | Griglia riassegnazione: colonna "Data Scadenza" presente ma `showWhen: false` | `05_ux-ui.md §BOA_ANC_Intertfaccia_RiassegnazioneTask` | Non documentato | La colonna non va visualizzata (conforme al reverse). Includere il dato nel DTO ma non renderizzare. |
| UX-GAP-08 | Tipizzazione: form è un **task separato** (processo separato `BOA_ANC_Processo_TipizzaDoc`) — non è uno step inline del task lavorazione | `05_ux-ui.md §STEP 0` | `GAP_Architettura.md GAP-US-03` lo modella come step 1 di `task.typeAndChecklist` | La route `/task/:taskId/tipizzazione` deve essere un form indipendente. Verificare allineamento con arch. |

---

## 10. Riepilogo Componenti da Implementare (Route → Componente React)

| Route | Componente React | Schermata originale |
|---|---|---|
| `/operatore/home` | `DashboardOperatore` | `BOA_ANC_Interfaccia_DashBoard_Operatore` |
| `/operatore/attivita` | `ListaAttivita` | `BOA_ANC_ListaAttivita` |
| `/operatore/pratiche` | `ListaPratiche` | `Pratica_ANC (Record List View)` |
| `/operatore/pratiche/:id` | `DettaglioPratica` | `BOA_ANC_Summary` |
| `/operatore/task/:taskId` | `TaskLavorazione` | `BOA_ANC_Task_Lavorazione` |
| `/operatore/task/:taskId/tipizzazione` | `TaskTipizzazione` | `BOA_ANC_Task_TipizzazioneDocumenti` |
| `/supervisore/home` | `DashboardSupervisore` | `BOA_ANC_Interfaccia_DashBoard_Supervisore` |
| `/supervisore/pratiche` | `ListaPratiche` | `Pratica_ANC (Record List View)` |
| `/supervisore/pratiche/:id` | `DettaglioPratica` | `BOA_ANC_Summary` |
| `/supervisore/riassegna-attivita` | `RiassegnazioneTask` | `BOA_ANC_Intertfaccia_RiassegnazioneTask` |
