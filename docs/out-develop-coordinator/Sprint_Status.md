# Sprint Status - Sprint 0 Rebaseline

## Stato sintetico
| Voce | Valore |
|---|---|
| Sprint attivo | Sprint 0 |
| Data aggiornamento | 2026-05-13 |
| Obiettivo | Verifica coerenza completa post-rilancio discovery con baseline Flowable |
| Stato complessivo | NO-GO condizionato |

## Stato stream
| Stream | Stato | Evidenza sintetica |
|---|---|---|
| Backend | Completato con fix | baseline Flowable foundation + fix compile blocker |
| Frontend | Completato | perimetro Sprint 0 mantenuto, placeholder espliciti |
| QA | Completato | validazione post-fix: 14 PASS, 1 FAIL |

## Evidenze principali
- Backend:
  - apps/backend/src/main/java/it/poste/anc/workflow/api/WorkflowReadinessController.java
  - apps/backend/src/main/java/it/poste/anc/workflow/engine/FlowableEngineConfig.java
  - apps/backend/src/main/resources/processes/sprint0_foundation_placeholder.bpmn20.xml
- Frontend:
  - apps/frontend/src/app/shell/AppShell.jsx
  - apps/frontend/src/core/api/workflowTechnicalApi.js
  - apps/frontend/src/features/practices/PracticesPage.jsx
  - apps/frontend/src/features/practices/PracticeDetailPlaceholderPage.jsx
- QA reports:
  - Smoke_Test_Report.md
  - Sprint_Test_Checklist.md
  - Defect_List.md
  - BPM_Workflow_Validation.md

## Matrice QA ultimo run
| Esito | Conteggio |
|---|---|
| PASS | 14 |
| FAIL | 1 |

## Issue residue
1. High: nessuna evidenza runtime smoke E2E nella sessione corrente.
2. Medium: incoerenza path BA attesi (analyst vs analist).

## Decisione coordinatore
NO-GO finche non vengono completate le evidenze runtime e la normalizzazione documentale minima.

## Next checkpoint
Riesecuzione smoke runtime locale e aggiornamento stato a GO/NO-GO definitivo.
