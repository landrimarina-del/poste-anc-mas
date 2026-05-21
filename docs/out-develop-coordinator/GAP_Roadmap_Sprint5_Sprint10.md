# GAP — Roadmap Implementativa Sprint 11÷16

> **Scopo**: Piano di implementazione di tutti gli interventi di GAP identificati nei documenti di analisi.  
> Consolida in un unico piano sprint-by-sprint le indicazioni di:
> - `docs/out-discovery-business-analist/10_GAP_Coverage_Review.md` (11 GAP-US)
> - `docs/out-discovery-architect/GAP_Architettura.md` (11 GAP-US + 6 TECNICO-GAP)
> - `docs/out-discovery-dba/GAP-DBA.md` (migration V15÷V22)
> - `docs/out-discovery-ux-mapper/GAP-UX.md` (8 UX-GAP)
> - `docs/out-discovery-ux-mapper/GAP-UI.md` (8 UI-GAP)
>
> **Data**: 2026-05-21  
> **Baseline**: Sprint 10 completato (V1÷V14, GO — 2026-05-16)  
> **Prossima migration disponibile**: V15  
> **Owner orchestrazione**: develop-coordinator

---

## Indice Sprint

| Sprint | Titolo | Team | Migration | GAP coperti |
|---|---|---|---|---|
| **Sprint 11** | BPM Retry + Ticketing Mock | BE + BPM-stub | V15, V16 | GAP-US-01, GAP-US-02, GAP-US-07 |
| **Sprint 12** | Lavorazione Task — Step e Sidebar | BE + FE | — | GAP-US-03, GAP-US-04, GAP-US-06 |
| **Sprint 13** | Checklist Avanzata — Dipendenze e Causali | BE + FE + DBA | V17, V18, V19 | GAP-US-08, GAP-US-11, TECNICO-GAP-C |
| **Sprint 14** | Lista Attività + Pratiche + Filtri | BE + FE + DBA | V20 | GAP-US-05, GAP-US-09, GAP-US-10 |
| **Sprint 15** | Tecnici Trasversali + SLA + Validazioni | BE + DBA + Config | V21, V22 | TECNICO-GAP-A, B, D, E, F |
| **Sprint 16** | UX/UI Gap Residui — Dashboard + Navigazione | FE | — | UX-GAP-01, 03÷08, UI-GAP-01÷08 |

---

## Sprint 11 — BPM Retry + Ticketing Mock

**Priorità**: CRITICA — blocca la verifica del workflow completo di chiusura pratica  
**Durata stimata**: 1 settimana  
**Team**: Backend, BPM-stub (Infra)

### Obiettivo

Chiudere il gap sul pattern di chiamata BPM: sostituire il callback inbound con chiamata sincrona a retry. Aggiungere il mock ticketing. Entrambi i gap impattano il flusso principale `anc.main`.

### Deliverable

| ID | Tipo | Descrizione | Ref spec |
|---|---|---|---|
| D11-DB-1 | Migration V15 | ALTER bpm_outbound_message — retry_count, max_retry, stato_invio, response_json, error_message, last_attempt_at | GAP-DBA §V15 |
| D11-DB-2 | Migration V16 | ALTER practice ADD ticket_id | GAP-DBA §V16 |
| D11-BE-1 | Backend | `BpmOutboundService` con `RetryTemplate` + aggiornamento `stato_invio` | GAP_Arch §GAP-US-02 |
| D5-BE-2 | Backend | `svc.openPractice` — chiamata `TicketingClient.openTicket()` post-persist | GAP_Arch §GAP-US-01 |
| D5-BE-3 | Backend | Config `application-poc.yml` — blocchi `bpm.*` e `ticketing.*` | GAP_Arch §GAP-US-02, §GAP-US-01 |
| D5-STUB-1 | bpm-stub | Nuovo endpoint `POST /receive-outcome` (risposta sincrona configurabile) | GAP_Arch §GAP-US-07 |
| D5-STUB-2 | bpm-stub | Nuovo endpoint `POST /ticketing/open-ticket` | GAP_Arch §GAP-US-01 |
| D5-STUB-3 | bpm-stub | Endpoint admin `GET/PUT /admin/mode` (switch OK/KO a runtime) | GAP_Arch §GAP-US-07 |
| D5-BPMN-1 | BPMN | Rimuovere `evt.waitOutcomeAck` da `anc.main` | GAP_Arch §GAP-US-02 |
| D5-BPMN-2 | BPMN | `svc.sendOutcomeToBpm` → sincrono con retry → `svc.finalizeOnAck` | GAP_Arch §GAP-US-02 |

### Acceptance Criteria

| AC | Condizione |
|---|---|
| AC-S11-01 | V15 applicata: `bpm_outbound_message` ha colonne retry + `stato_invio TINYINT` |
| AC-S5-02 | V16 applicata: `practice.ticket_id` presente e nullable |
| AC-S5-03 | POST `/api/v1/bpm/practices` (happy path) → `practice.ticket_id = 'MOCK-TICKET-*'` valorizzato |
| AC-S5-04 | POST happy path con `BPM_STUB_ESITO_MODE=OK` → pratica `CHIUSA_OK`, `bpm_outbound_message.stato_invio = 1` |
| AC-S5-05 | POST happy path con `BPM_STUB_ESITO_MODE=KO` → pratica `CHIUSA_KO`, `stato_invio = 1` |
| AC-S5-06 | bpm-stub temporaneamente down → dopo `max_retry` tentativi → `stato_invio = 3`, pratica `IN_ATTESA_CONFERMA_BPM` |
| AC-S5-07 | PUT `/admin/mode` con `{"mode":"KO"}` → successiva chiamata BPM restituisce KO |

### Dipendenze

- V14 già applicata (baseline Sprint 10)
- bpm-stub già presente (Docker Compose da Sprint 4)
- Nessuna dipendenza da Sprint 6÷10

---

## Sprint 12 — Lavorazione Task: Step e Sidebar

**Priorità**: ALTA  
**Durata stimata**: 1 settimana  
**Team**: Backend, Frontend

### Obiettivo

Implementare la navigazione multi-step del task di lavorazione: sidebar collassabile con 3 step, abilitazione condizionale step "Riepilogo", visualizzazione fasi operative (milestone).

### Deliverable

| ID | Tipo | Descrizione | Ref spec |
|---|---|---|---|
| D6-BE-1 | Backend | `GET /tasks/{id}` — aggiungere campo `intakeStep` (VERIFICA\|CLASSIFICAZIONE\|CHECKLIST\|RIEPILOGO) | GAP_Arch §GAP-US-03 |
| D6-BE-2 | Backend | `GET /tasks/{id}` — aggiungere campo `sidebarState` con steps[] + enabled/completed | GAP_Arch §GAP-US-04 |
| D6-BE-3 | Backend | `GET /practices/{id}` — aggiungere campo derivato `fase` (RACCOLTA_INPUT\|LAVORAZIONE\|CHIUSURA_PRATICA) | GAP_Arch §GAP-US-06 |
| D6-FE-1 | Frontend | Componente `WorkflowSidebar` — 3 voci, collapse NARROW/EXTRA_NARROW, step 3 condizionale | GAP-UX §4, GAP-UI §2.4 |
| D6-FE-2 | Frontend | Componente `VerificaDocumentiStep` (Step 0 read-only: dati cliente, carta, allegati) | GAP_Arch §GAP-US-03, GAP-UI §2.5 |
| D6-FE-3 | Frontend | Componente `ClassificazioneStep` (Step 1: dropdown tipo + confirm dialog ATTENZIONE) | GAP-UI §2.11 |
| D6-FE-4 | Frontend | Componente `PhaseProgressBar` / `MilestoneBar` — 3 fasi, fase corrente evidenziata ACCENT | GAP_Arch §GAP-US-06, GAP-UI §6 |
| D6-FE-5 | Frontend | Logic bottoni footer task: show/disabled condizionati su activeSection + esitoSD | GAP-UX §4.3 |

### Acceptance Criteria

| AC | Condizione |
|---|---|
| AC-S6-01 | `GET /tasks/{id}` restituisce `intakeStep` e `sidebarState` con steps[2].enabled = false se esitoSD null |
| AC-S6-02 | `GET /practices/{id}` restituisce `fase = "LAVORAZIONE"` per pratica in stato IN_LAVORAZIONE |
| AC-S6-03 | Sidebar: voce "Riepilogo" non cliccabile (grigio/disabled) fino a "Salva e prosegui" |
| AC-S6-04 | Sidebar: voce "Riepilogo" cliccabile dopo che esitoSD è valorizzato |
| AC-S6-05 | Button "Salva e prosegui" visible solo su step 2, disabled se esitoSD già valorizzato |
| AC-S6-06 | Button "chiudi pratica" visible solo su step 3 |
| AC-S6-07 | Confirm dialog tipizzazione mostra testo verbatim "ATTENZIONE / non sarà possibile modificare il tipo documento" |
| AC-S6-08 | MilestoneBar mostra fase corrente in ACCENT (#0047BB) e fasi precedenti come completed |
| AC-S6-09 | Sidebar collassabile: toggle angle-double-left/right → NARROW ↔ EXTRA_NARROW |

### Dipendenze

- Sprint 11 completato (nessuna dipendenza DB da Sprint 12, ma il task di lavorazione dipende da `openPractice` funzionante)

---

## Sprint 13 — Checklist Avanzata: Dipendenze e Causali KO

**Priorità**: ALTA  
**Durata stimata**: 1 settimana  
**Team**: DBA, Backend, Frontend

### Obiettivo

Aggiungere la visibilità condizionale degli item checklist (dipendenze padre-figlio) e il doppio meccanismo di motivazione KO (nota libera + codice causale formale dal catalogo). Aggiungere le note intermediate di lavorazione.

### Deliverable

| ID | Tipo | Descrizione | Ref spec |
|---|---|---|---|
| D7-DB-1 | Migration V17 | ALTER checklist_item_catalog ADD id_dipendenza FK + valore_attivo_dipendenza | GAP-DBA §V17 |
| D7-DB-2 | Migration V18 | CREATE ref_causali_checklist + ALTER checklist_response ADD codice_causale_id FK | GAP-DBA §V18 |
| D7-DB-3 | Migration V19 | CREATE case_note (practice_id, autore, testo, tipo, created_at) | GAP-DBA §V19 |
| D7-BE-1 | Backend | `GET /practices/{id}/intake/checklist` — aggiungere campo `visible` calcolato per item | GAP_Arch §GAP-US-08 |
| D7-BE-2 | Backend | `PUT /practices/{id}/intake/checklist` — accettare `nota` + `codiceCausaleId` per item | GAP_Arch §GAP-US-11 |
| D7-BE-3 | Backend | `GET /practices/{id}/intake/checklist/causali?categoria=` — nuovo endpoint | GAP_Arch §GAP-US-11 |
| D7-BE-4 | Backend | `GET/POST /practices/{id}/notes` — nuovi endpoint note intermediate | GAP_Arch §TECNICO-GAP-C |
| D7-FE-1 | Frontend | Checklist: filtro item per `item.visible == true` prima del rendering | GAP-UI §3 |
| D7-FE-2 | Frontend | Checklist: riga espandibile per item KO — textarea nota + dropdown causale | GAP-UI §2.10 |
| D7-DB-SEED | Seed | Popolare `ref_causali_checklist` con causali reali da `MatriciControlli.xlsx` | GAP-DBA §V18 SEED |

### Acceptance Criteria

| AC | Condizione |
|---|---|
| AC-S7-01 | V17 applicata: `checklist_item_catalog` ha `id_dipendenza` self-FK + `valore_attivo_dipendenza` |
| AC-S7-02 | V18 applicata: `ref_causali_checklist` creata con seed; `checklist_response.codice_causale_id` presente |
| AC-S7-03 | V19 applicata: `case_note` creata con CHECK tipo IN ('LAVORAZIONE','CAMBIO_STATO','CHIUSURA') |
| AC-S7-04 | `GET /intake/checklist` — item con padre non valorizzato → `visible: false` |
| AC-S7-05 | `GET /intake/checklist/causali?categoria=VERBALE` → lista causali filtrata |
| AC-S7-06 | `PUT /intake/checklist` con `nota` e `codiceCausaleId` → persiste entrambi su `checklist_response` |
| AC-S7-07 | FE: item non visibili (visible=false) non renderizzati nel form checklist |
| AC-S7-08 | FE: per item KO → area espansa mostra textarea nota + dropdown causale (entrambi opzionali) |

### Dipendenze

- Sprint 5 completato
- Sprint 6 (opzionale per FE — il rendering checklist può procedere indipendentemente dalla sidebar)

---

## Sprint 14 — Lista Attività + Pratiche + Filtri Salvati

**Priorità**: MEDIA  
**Durata stimata**: 1 settimana  
**Team**: DBA, Backend, Frontend

### Obiettivo

Implementare i filtri salvati per la Lista Attività, la griglia pratiche con 11 colonne fisse, e il tab "Stati" nel dettaglio pratica.

### Deliverable

| ID | Tipo | Descrizione | Ref spec |
|---|---|---|---|
| D8-DB-1 | Migration V20 | CREATE user_task_filter (user_id FK → app_user, campi filtro, created_at) | GAP-DBA §V20 |
| D8-BE-1 | Backend | `GET /tasks/filters/saved` — ultimi N filtri per utente corrente | GAP_Arch §GAP-US-05 |
| D8-BE-2 | Backend | `POST /tasks/filters/saved` — salva set filtri (FIFO se > N) | GAP_Arch §GAP-US-05 |
| D8-BE-3 | Backend | `DELETE /tasks/filters/saved/{id}` — elimina set filtri | GAP_Arch §GAP-US-05 |
| D8-BE-4 | Backend | `GET /practices` — DTO `PracticeListItemDto` con 11 campi fissi + segnalazioni calcolata | GAP_Arch §GAP-US-09 |
| D8-BE-5 | Backend | `GET /practices/{id}/states` — documenta e verifica contratto response | GAP_Arch §GAP-US-10 |
| D8-FE-1 | Frontend | ListaAttivita: sezione "Ultimi N Filtri Salvati" (griglia ROW_HIGHLIGHT + load-on-select) | GAP-UX §2.4, GAP-UI §2.3 |
| D8-FE-2 | Frontend | ListaAttivita: bottoni "Applica Filtri" / "Applica e Salva Filtri" / "Azzera Filtri" | GAP-UI §2.3 |
| D8-FE-3 | Frontend | ListaPratiche: griglia 11 colonne fisse (Id/Canale/DataScadenza hidden), Segnalazioni icona | GAP-UI §2.14, §7.1 |
| D8-FE-4 | Frontend | DettaglioPratica: tab "Stati" separato (griglia Stato/DataOra/Operatore, readonly) | GAP-UX §2.3, GAP-UI §2.12 |

### Acceptance Criteria

| AC | Condizione |
|---|---|
| AC-S8-01 | V20 applicata: `user_task_filter` con FK cascading su `app_user` |
| AC-S8-02 | `POST /tasks/filters/saved` → salva set; sesto save elimina il più vecchio automaticamente |
| AC-S8-03 | `GET /practices` → DTO ha esattamente 11 campi + segnalazioni; colonne Id/Canale/DataScadenza non esposte nella risposta griglia |
| AC-S8-04 | `GET /practices/{id}/states` → response con array `[{stato, dataOraInizio, attore}]` in ordine cronologico |
| AC-S8-05 | FE griglia filtri salvati: click riga popola form filtri (senza apply automatico) |
| AC-S8-06 | FE lista pratiche: colonne Id, Canale, Data Scadenza assenti dalla griglia renderizzata |
| AC-S8-07 | FE tab "Stati": presente nel dettaglio pratica, distinto dal tab "Cronologia" |

### Dipendenze

- Sprint 5 completato
- Sprint 7 (per V17÷V19 numerazione Flyway corretta; V20 non ha dipendenze di dati da V17÷V19)

---

## Sprint 15 — Tecnici Trasversali: SLA, Validazioni, Cleanup

**Priorità**: MEDIA  
**Durata stimata**: 1 settimana  
**Team**: DBA, Backend, Config

### Obiettivo

Chiudere i gap tecnici rilevati dal reverse engineering che non impattano direttamente l'UX operativa: espansione link_download, SLA monitoring, validazione dominio CANALE, flag debug, cleanup Flowable.

### Deliverable

| ID | Tipo | Descrizione | Ref spec |
|---|---|---|---|
| D9-DB-1 | Migration V21 | ALTER attachment MODIFY link_download VARCHAR(1024→2500) | GAP-DBA §V21 |
| D9-DB-2 | Migration V22 | ALTER task ADD sla_due_date DATETIME(3) | GAP-DBA §V22 |
| D9-BE-1 | Backend | `SlaTaskListener` Flowable — calcola dueDate + 5 giorni lavorativi | GAP_Arch §TECNICO-GAP-B |
| D9-BE-2 | Backend | `GET /tasks/{id}` — aggiungere `slaDueDate` e `slaStatus` (IN_TEMPO\|SCADUTO) derivato | GAP_Arch §TECNICO-GAP-B |
| D9-BE-3 | Backend | Validatore `POST /bpm/practices` — validazione CANALE ∈ {APP,WEB}, resultCode=-4 | GAP_Arch §TECNICO-GAP-D |
| D9-BE-4 | Backend | Config `debug.default-codice-doc-id` — solo profilo poc/local | GAP_Arch §TECNICO-GAP-E |
| D9-OPS-1 | Script | `scripts/cleanup-flowable-history.sql` — DELETE ACT_HI_* > 30 giorni | GAP_Arch §TECNICO-GAP-F |
| D9-DOC-1 | Config | `07_Deployment_Locale.md` — nota cleanup Flowable | GAP_Arch §TECNICO-GAP-F |

### Acceptance Criteria

| AC | Condizione |
|---|---|
| AC-S9-01 | V21 applicata: `attachment.link_download` è VARCHAR(2500); URL da 1025 a 2500 chars accettato |
| AC-S9-02 | V22 applicata: `task.sla_due_date` nullable presente |
| AC-S9-03 | Task creato → `sla_due_date` valorizzato a `created_at + 5gg lavorativi` |
| AC-S9-04 | `GET /tasks/{id}` → `slaStatus: "SCADUTO"` se `now > sla_due_date` |
| AC-S9-05 | `POST /bpm/practices` con `CANALE = "FAX"` → HTTP 200 `resultCode=-4` "CANALE non valido" |
| AC-S9-06 | `POST /bpm/practices` con `CANALE = "APP"` → HTTP 200 `resultCode=0` (valido) |
| AC-S9-07 | Con `debug.default-codice-doc-id=3` → `CODICE_DOC_ID` nel payload ignorato, usato 3 |
| AC-S9-08 | `scripts/cleanup-flowable-history.sql` eseguito su DB con istanze > 30gg → `ACT_HI_PROCINST` diminuisce |

### Dipendenze

- V20 (Sprint 8) già applicata
- Backend: nessuna dipendenza da Sprint 6÷8 per i deliverable tecnici

---

## Sprint 16 — UX/UI Gap Residui: Dashboard, Navigazione, Stile

> **Nota**: UX-GAP-02 (Link Favoriti) già implementato in Sprint 10. Verificare con team FE quali altri item UI siano stati implementati indirettamente (C4.11 Espandi/Comprimi, C2.9 Azioni correlate).

**Priorità**: MEDIA/BASSA  
**Durata stimata**: 1 settimana  
**Team**: Frontend

### Obiettivo

Implementare i gap UX/UI identificati dal confronto con il reverse Appian: dashboard azioni dinamiche, link favoriti, colore billboard Supervisore, header pratica completo, griglia con comportamenti corretti.

### Deliverable

| ID | Tipo | Descrizione | Ref spec |
|---|---|---|---|
| D10-FE-1 | Frontend | DashboardOperatore: box "Azioni" con lista azioni gruppo (statica in POC) | GAP-UX §UX-GAP-01, GAP-UI §2.1 |
| D10-FE-2 | Frontend | DashboardOperatore: box "Link Favoriti" con CRUD inline (GET/POST/DELETE /users/me/favorites) | GAP-UX §UX-GAP-02 |
| D10-FE-3 | Frontend | DashboardSupervisore: billboard backgroundColor #FFEC00 (giallo — distingue ruolo) | GAP-UI §1.1, §UX-GAP-04 |
| D10-FE-4 | Frontend | Header pratica (BOA_ANC_Header): BillboardHeader SHORT + avatar app + info 2-col | GAP-UI §2.12, §UI-GAP-02 |
| D10-FE-5 | Frontend | ListaAttivita: checkbox "Visualizza attività a me assegnate" (con filtro `?assignedToMe=true`) | GAP-UX §UX-GAP-03 |
| D10-FE-6 | Frontend | SezioneIndirizzoResidenza: componente figlio di DatiCliente (via, civico, CAP, comune, prov., nazione) | GAP-UX §UX-GAP-05 |
| D10-FE-7 | Frontend | Griglia riassegnazione: selezione multi-row + "Riassegna" abilitato solo se selezione ≥ 1 | GAP-UI §UI-GAP-08 |
| D10-FE-8 | Frontend | Stile globale: buttonShape SQUARED, labels UPPERCASE, BoxPoste con bordo ACCENT #0047BB | GAP-UI §1.2, §1.3 |
| D10-BE-1 | Backend | `GET /tasks` — aggiungere filtro `?assignedToMe=true` | GAP-UX §UX-GAP-03 |

### Acceptance Criteria

| AC | Condizione |
|---|---|
| AC-S10-01 | Dashboard Operatore mostra box "Azioni" con almeno un'azione del gruppo |
| AC-S10-02 | Dashboard Supervisore ha banner billboard con sfondo giallo #FFEC00 |
| AC-S10-03 | Checkbox "Visualizza attività a me assegnate" presente e funzionante nella lista attività |
| AC-S10-04 | Tutti i bottoni nell'app hanno shape rettangolare e label UPPERCASE |
| AC-S10-05 | Box container con stile BOA_STYLE_POSTE mostrano bordo ACCENT #0047BB |
| AC-S10-06 | Griglia riassegnazione: "Riassegna" disabled se nessuna riga selezionata |
| AC-S10-07 | Header pratica: billboardLayout SHORT con milestoneField (3 fasi) e info pratica (2 colonne) |

### Dipendenze

- Sprint 6 completato (componenti di base lavorazione già implementati)
- Sprint 8 completato (lista pratiche già funzionante)

---

## Dipendenze tra Sprint — Grafo

```
Sprint 11 (BPM Retry + Ticketing)
    │
    ├──► Sprint 12 (Lavorazione UI) ──► Sprint 16 (UX/UI Residui)
    │         │
    │         └──► Sprint 13 (Checklist Avanzata)
    │                    │
    ├──► Sprint 14 (Lista Attività + Pratiche) ──► Sprint 16
    │
    └──► Sprint 15 (Tecnici — parallelo con 12/13/14)
```

Sprint 9 è **parallelizzabile** con Sprint 6÷8 (nessuna dipendenza di feature tra loro).

---

## Tabella Completa GAP → Sprint

| GAP ID | Titolo | Sprint | Team | DB |
|---|---|---|---|---|
| GAP-US-01 | Ticketing mock + ticket_id | 11 | BE + bpm-stub | V16 |
| GAP-US-02 | BPM retry sincrono | 11 | BE + BPMN + bpm-stub | V15 |
| GAP-US-03 | Verifica + classificazione sequenziale | 12 | BE + FE | — |
| GAP-US-04 | Sidebar 3-step collassabile | 12 | BE + FE | — |
| GAP-US-05 | Filtri Lista Attività salvati | 14 | BE + FE + DBA | V20 |
| GAP-US-06 | Milestone fasi operative | 12 | BE + FE | — |
| GAP-US-07 | bpm-stub configurabile OK/KO | 11 | bpm-stub | — |
| GAP-US-08 | Checklist dipendenze padre-figlio | 13 | BE + FE + DBA | V17 |
| GAP-US-09 | Griglia pratiche 11 colonne | 14 | BE + FE | — |
| GAP-US-10 | Tab "Stati" dettaglio pratica | 14 | BE + FE | — |
| GAP-US-11 | Causali KO + nota libera | 13 | BE + FE + DBA | V18 |
| TECNICO-GAP-A | link_download expand 2500 | 15 | DBA | V21 |
| TECNICO-GAP-B | SLA 5gg lavorativi | 15 | BE + BPMN + DBA | V22 |
| TECNICO-GAP-C | Case note intermediate | 13 | BE + DBA | V19 |
| TECNICO-GAP-D | Validazione CANALE {APP,WEB} | 15 | BE | — |
| TECNICO-GAP-E | Debug flag codice-doc-id | 15 | Config | — |
| TECNICO-GAP-F | Cleanup Flowable history | 15 | Ops + Config | Script |
| UX-GAP-01 | Dashboard Azioni gruppo | 16 | FE | — |
| UX-GAP-02 | Link Favoriti CRUD inline | ~~10~~ GIÀ FATTO (Sprint 10 C8.3) | FE + BE | — |
| UX-GAP-03 | Checkbox "attività a me assegnate" | 16 | FE + BE | — |
| UX-GAP-04 | Billboard Supervisore #FFEC00 | 16 | FE | — |
| UX-GAP-05 | SezioneIndirizzoResidenza | 16 | FE | — |
| UX-GAP-06 | Button Modifica reset esitoSD | 12 | FE | — |
| UX-GAP-07 | Col. DataScadenza riassegnazione hidden | 12 | FE | — |
| UX-GAP-08 | Tipizzazione route separata | 12 | FE | — |
| UI-GAP-01 | MilestoneBar 3 fasi | 12 | FE | — |
| UI-GAP-02 | Avatar app header pratica | 16 | FE | — |
| UI-GAP-03 | Card shape SEMI_ROUNDED | 16 | FE | — |
| UI-GAP-04 | BOA_ANC_Contenuti_Section struttura | 12 | FE | — |
| UI-GAP-05 | Colonna Segnalazioni = richTextIcon | 14 | FE | — |
| UI-GAP-06 | Sidebar collapse angle-double | 12 | FE | — |
| UI-GAP-07 | ROW_HIGHLIGHT griglia filtri salvati | 14 | FE | — |
| UI-GAP-08 | Griglia riassegnazione multi-select | 16 | FE | — |

---

## Migration Flyway — Piano Sequenziale

| Versione | File | Sprint | Dipende da |
|---|---|---|---|
| V15 | `V15__gap_us02_bpm_outbound_retry.sql` | 11 | V14 |
| V16 | `V16__gap_us01_practice_ticket.sql` | 11 | V14 |
| V17 | `V17__gap_us08_checklist_dependency.sql` | 13 | V16 |
| V18 | `V18__gap_us11_causali_checklist.sql` | 13 | V17 |
| V19 | `V19__tecnico_gap_c_case_note.sql` | 13 | V18 |
| V20 | `V20__gap_us05_user_task_filter.sql` | 14 | V19 |
| V21 | `V21__tecnico_gap_a_attachment_linkdownload.sql` | 15 | V20 |
| V22 | `V22__tecnico_gap_b_task_sla.sql` | 15 | V21 |

> Tutte le migration devono seguire il pattern idempotente (PREPARE/EXECUTE + IF NOT EXISTS) già adottato da V8÷V14.

---

## Issue Aperte da Sprint 4 — Impatto sulla Roadmap

| Issue | SEV | Impatto sui GAP sprint |
|---|---|---|
| CR-S4-01 — `sample.png/.jpg` placeholder non validi | 3 | Sprint 10 (viewer immagini — non blocca Sprint 5÷9) |
| ISS-S4P-04 — AC-VIEWER-1 BLOCKED-AUTH (viewer senza sessione) | 3 | Sprint 6 (verifica viewer in VerificaDocumentiStep) |
| ISS-S4P-03 — fixture > 25MB non testata | 3 | Sprint 9 (TECNICO-GAP-A — non blocca) |
