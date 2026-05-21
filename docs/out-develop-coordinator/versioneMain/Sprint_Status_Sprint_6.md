# Sprint Status - Sprint 6

Data: 2026-05-15
Sprint: 6
Gate corrente: CONDITIONAL-GO

## Stato stream
- Backend: completato e deployato.
- Frontend: completato e deployato.
- QA: artefatti pronti, esecuzione runtime AC marcata BLOCKED-EXEC.

## Evidenze tecniche
- Build backend PASS dopo correzione compilazione response checklist carta.
- Docker deploy PASS con immagini backend e frontend aggiornate.
- Flyway V11 applicata con successo.
- Schema checklist carta presente.

Riferimenti:
- [infra/db/migrations/V11__sprint6_carta_close_bpm_ack.sql](../../infra/db/migrations/V11__sprint6_carta_close_bpm_ack.sql)
- [apps/backend/src/main/java/it/poste/anc/document/application/IntakePracticeCloseService.java](../../apps/backend/src/main/java/it/poste/anc/document/application/IntakePracticeCloseService.java)
- [apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmOutcomeAckService.java](../../apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmOutcomeAckService.java)
- [apps/frontend/src/features/intake/TypingPage.jsx](../../apps/frontend/src/features/intake/TypingPage.jsx)

## Copertura Acceptance Sprint 6
| AC | Stato |
|---|---|
| AC-S6-01 tipizzazione CARTA abilita checklist carta | BLOCKED-EXEC |
| AC-S6-02 card_present=NO -> RESPINTA | BLOCKED-EXEC |
| AC-S6-03 card_present=SI conformita=SI -> APPROVATA | BLOCKED-EXEC |
| AC-S6-04 close -> IN_ATTESA_CONFERMA_BPM + task rimosso | BLOCKED-EXEC |
| AC-S6-05 outbound payload verso bpm-stub | BLOCKED-EXEC |
| AC-S6-06 ack OK -> CHIUSA_OK + data chiusura | BLOCKED-EXEC |
| AC-S6-07 ack KO -> CHIUSA_KO + data chiusura | BLOCKED-EXEC |
| AC-S6-08 idempotenza ack | BLOCKED-EXEC |
| AC-S6-09 storico stati e audit | BLOCKED-EXEC |

Dettaglio report QA:
- [docs/out-develop-qa/Sprint_Test_Checklist_Sprint_6.md](../out-develop-qa/Sprint_Test_Checklist_Sprint_6.md)
- [docs/out-develop-qa/Smoke_Test_Report_Sprint_6.md](../out-develop-qa/Smoke_Test_Report_Sprint_6.md)
- [docs/out-develop-qa/Defect_List_Sprint_6.md](../out-develop-qa/Defect_List_Sprint_6.md)

## Decisione gate
CONDITIONAL-GO.

Condizioni per GO finale:
1. esecuzione runbook QA Sprint 6 con evidenze runtime.
2. pass di AC-S6-01..09.
3. conferma completa vertical slice BPM eseguibile end-to-end con close e ack.
