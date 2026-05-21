# Sprint Status — Sprint 12

> **Sprint**: 12  
> **Titolo**: Lavorazione Task — Step e Sidebar  
> **Data**: 2026-05-21  
> **Owner**: develop-coordinator  
> **Esito**: **GO**

---

## Riepilogo Esecuzione

| Stream | Stato | Note |
|---|---|---|
| STREAM-1 Backend | ✅ COMPLETATO | `intakeStep`, `sidebarState`, `fase` implementati e verificati |
| STREAM-2 Frontend | ✅ COMPLETATO | 5 componenti creati, build Docker OK, container healthy |
| STREAM-3 QA | ✅ COMPLETATO | 9/9 AC PASS |

---

## Acceptance Criteria

| AC | Fonte GAP | Esito | Verificato da |
|---|---|---|---|
| AC-S6-01 | GAP-US-03, GAP-US-04 | ✅ PASS | Test live API GET /tasks/4 |
| AC-S6-02 | GAP-US-06 | ✅ PASS | Test live API GET /practices/6 |
| AC-S6-03 | GAP-US-04, UX-GAP §N-01 | ✅ PASS | Ispezione codice WorkflowSidebar.jsx |
| AC-S6-04 | GAP-US-04, UX-GAP §N-02 | ✅ PASS | Ispezione codice WorkflowSidebar.jsx |
| AC-S6-05 | UX-GAP §N-03, N-05 | ✅ PASS | Ispezione codice TaskLavorazionePage.jsx |
| AC-S6-06 | UX-GAP §N-04 | ✅ PASS | Ispezione codice TaskLavorazionePage.jsx |
| AC-S6-07 | GAP-UX §7.1 | ✅ PASS | Ispezione codice ClassificazioneStep.jsx |
| AC-S6-08 | GAP-US-06, GAP-UI §6 | ✅ PASS | Ispezione codice PhaseProgressBar.jsx |
| AC-S6-09 | GAP-US-04, GAP-UX §4.1 | ✅ PASS | Ispezione codice WorkflowSidebar.jsx |

---

## File Prodotti

### Backend (STREAM-1)
- `PracticeQueryService.java` — aggiunto metodo `computeFase()` (logica RACCOLTA_INPUT / LAVORAZIONE / CHIUSURA_PRATICA)
- `TaskDetailResponse.java` — record già con `intakeStep` e `SidebarState` (presenti da architettura)
- `TaskManagementService.java` — metodi `getTaskDetail()`, `computeIntakeStep()`, `buildSidebarState()` (presenti da architettura)

### Frontend (STREAM-2)
- `apps/frontend/src/features/shared/PhaseProgressBar.jsx` — barra milestone 3 fasi
- `apps/frontend/src/features/intake/WorkflowSidebar.jsx` — sidebar 3 voci, toggle NARROW↔EXTRA_NARROW
- `apps/frontend/src/features/intake/ClassificazioneStep.jsx` — dropdown + confirm dialog verbatim
- `apps/frontend/src/features/intake/VerificaDocumentiStep.jsx` — layout 2 col read-only
- `apps/frontend/src/features/intake/TaskLavorazionePage.jsx` — pagina principale con logica N-01÷N-05

---

## Stack Finale

```
anc-backend             anc/backend:0.1.0       healthy  (8080)
anc-bpm-outbound-stub   bpm-outbound-stub        healthy  (8090 interno)
anc-bpm-stub            nginx:1.27-alpine        healthy  (80 interno)
anc-db                  mariadb:10.11            healthy  (3307)
anc-frontend            anc/frontend:0.1.0       healthy
anc-minio               minio/minio:latest       healthy  (9000-9001)
anc-reverse-proxy       nginx:1.27-alpine        running  (80)
```

7 container healthy/running al termine dello sprint.

---

## Note

- Mock BPM stub in modalità **KO** (impostato in Sprint 11, non modificato in Sprint 12)
- **Nessuna migration DB** in questo sprint (conforme a roadmap)
- Flyway V1÷V14, V101, V102 invariati

---

## Prossimo Sprint

**Sprint 13** — Checklist Avanzata: Dipendenze e Causali KO  
Migration: V103 (checklist_item_catalog dipendenze), V104 (ref_causali_checklist + codice_causale_id), V105 (case_note)  
GAP coperti: GAP-US-08, GAP-US-11, TECNICO-GAP-C
