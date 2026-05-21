# CrossAgent Issues - Sprint 7

Data: 2026-05-15

## Issue aperte

| ID | Severità | Tema | Owner | Stato | Note |
|---|---|---|---|---|---|
| ISS-S7-01 | SEV-3 | Evidenza UI visuale T13 non raccolta (tab e azioni browser) | develop-qa / develop-frontend | OPEN | Copertura attuale valida su API/DB; pianificare prova manuale visuale. |
| ISS-S7-02 | SEV-3 | Filtro Data Assegnazione non incluso nelle evidenze runtime correnti | develop-qa | OPEN | Filtri practiceNumber/owner/assignee validati; completare test dedicato data. |

## Issue chiuse

| ID | Severità | Tema | Owner | Stato | Evidenza |
|---|---|---|---|---|---|
| ISS-S7-03 | SEV-2 | Divergenza report QA iniziale (BLOCKED) vs stato runtime reale | develop-qa | CLOSED | Report Sprint 7 aggiornati a PASS in [docs/out-develop-qa/Smoke_Test_Report_Sprint_7.md](../out-develop-qa/Smoke_Test_Report_Sprint_7.md). |
| ISS-S7-04 | SEV-2 | Test frontend Sprint 7 instabili (3 fail su 4) | develop-frontend | CLOSED | Harden test isolation in [apps/frontend/src/features/supervisor/ReassignActivitiesPage.test.jsx](../../apps/frontend/src/features/supervisor/ReassignActivitiesPage.test.jsx), esito PASS 4/4. |
| ISS-S7-05 | SEV-2 | Validazione backend non eseguibile su host per JDK mismatch | develop-coordinator | CLOSED | Build backend Docker PASS con toolchain Java 21 containerizzata. |
| ISS-S7-06 | SEV-2 | Rischio violazione VC-1 in riassegnazione | develop-backend / develop-qa | CLOSED | Verifica DB su task 3/practice 6: stato pratica invariato IN_LAVORAZIONE dopo reassign-group/reassign-user. |

## Coerenza cross-agent
- Backend e frontend allineati sugli endpoint Sprint 7 di supervisione.
- Enforcement ruolo SUPERVISORE verificato end-to-end (403 su operatore).
- Workflow/state management Discovery preservato: la riassegnazione aggiorna ownership task senza alterare lifecycle pratica.
- Nessuna anticipazione scope Sprint 8/9/10 rilevata.

## Azioni immediate
1. Eseguire una sessione QA visuale browser sul tab Riassegna Attivita e allegare screenshot.
2. Estendere runbook QA Sprint 7 con caso specifico filtro Data Assegnazione.
3. Mantenere monitoraggio health bpm-stub (unhealthy noto) in hardening sprint successivi.
