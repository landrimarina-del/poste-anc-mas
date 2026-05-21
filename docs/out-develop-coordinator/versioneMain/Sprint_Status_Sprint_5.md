# Sprint Status — Sprint 5

Data: 2026-05-15  
Sprint: 5 (Checklist Verbale + Calcolo Esito)

Gate corrente: 🟡 CONDITIONAL-GO

## Stato stream
- Backend: ✅ completato e deployato in locale.
- Frontend: ✅ completato e integrato sulla pagina tipizzazione.
- QA: 🟡 artefatti pronti, esecuzione runtime marcata BLOCKED-EXEC dall’agente QA.

## Evidenze tecniche
- Build backend: PASS.
- Backend restart: PASS.
- Flyway: V10 applicata con success=1.
- Tabelle Sprint5 presenti:
  - checklist_verbale
  - practice_outcome

Riferimenti codice:
- [apps/backend/src/main/java/it/poste/anc/document/application/IntakeChecklistService.java](../../apps/backend/src/main/java/it/poste/anc/document/application/IntakeChecklistService.java)
- [apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistController.java](../../apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistController.java)
- [apps/frontend/src/features/intake/TypingPage.jsx](../../apps/frontend/src/features/intake/TypingPage.jsx)
- [apps/frontend/src/core/api/intakeApi.js](../../apps/frontend/src/core/api/intakeApi.js)

## Acceptance coverage (stato attuale)
| AC Sprint 5 | Stato |
|---|---|
| AC-S5-01 documento_presente=NO => RESPINTA | 🟡 BLOCKED-EXEC |
| AC-S5-02 tutti SI => APPROVATA | 🟡 BLOCKED-EXEC |
| AC-S5-03 formal_ok=NO senza causali => errore | 🟡 BLOCKED-EXEC |
| AC-S5-04 formal_ok=NO con causali => RESPINTA | 🟡 BLOCKED-EXEC |
| AC-S5-05 SALVA E PROSEGUI persiste BOZZA | 🟡 BLOCKED-EXEC |
| AC-S5-06 MODIFICA porta RIAPERTA | 🟡 BLOCKED-EXEC |
| AC-S5-07 note solo su RESPINTA | 🟡 BLOCKED-EXEC |

Dettaglio QA:
- [docs/out-develop-qa/Sprint_Test_Checklist_Sprint_5.md](../out-develop-qa/Sprint_Test_Checklist_Sprint_5.md)
- [docs/out-develop-qa/Smoke_Test_Report_Sprint_5.md](../out-develop-qa/Smoke_Test_Report_Sprint_5.md)

## Decisione gate
🟡 CONDITIONAL-GO per sviluppo completato, con promozione finale subordinata a:
1. esecuzione runbook QA Sprint5 su ambiente locale;
2. evidenza PASS degli AC-S5-01..07;
3. controllo di conformità funzionale rispetto a [docs/requirements/source-of-truth/checklistSD.xlsx](../requirements/source-of-truth/checklistSD.xlsx) (tracciamento item-by-item).
