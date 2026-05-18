# CrossAgent Issues - Sprint 8

Data: 2026-05-15

## Issue aperte

| ID | Severita | Tema | Owner | Stato | Note |
|---|---|---|---|---|---|
| ISS-S8-01 | SEV-2 | Coerenza requisito filtro mese (US-E9.05) vs contratto API `/supervision/dashboard/by-state` senza `month` | develop-backend / develop-frontend / develop-qa | OPEN | Conflitto tra user story BA (filtro statistiche per mese) e API Candidate architect (`by-state` senza month). Non applicata correzione autonoma; richiesto chiarimento formale BA/Architect. |
| ISS-S8-02 | SEV-3 | Test backend `SupervisionDashboardServiceTest` falliscono in contesto containerizzato (H2/Flyway) | develop-backend | OPEN | Runtime funzionale PASS, ma quality gate tecnico incompleto su test unit/integration del servizio dashboard. |

## Issue chiuse

| ID | Severita | Tema | Owner | Stato | Evidenza |
|---|---|---|---|---|---|
| ISS-S8-03 | SEV-2 | Report QA iniziali tutti BLOCKED per assenza runtime | develop-qa | CLOSED | Report Sprint 8 aggiornati a PASS/FAIL con evidenze runtime in [docs/out-develop-qa/Smoke_Test_Report_Sprint_8.md](../out-develop-qa/Smoke_Test_Report_Sprint_8.md). |
| ISS-S8-04 | SEV-3 | Fallimento test frontend Sprint 8 per query ambigua su titolo grafico | develop-frontend | CLOSED | Fix test in [apps/frontend/src/features/home/HomePage.test.jsx](../../apps/frontend/src/features/home/HomePage.test.jsx), esito PASS 2/2. |

## Coerenza cross-agent
- Backend e frontend allineati sugli endpoint dashboard Sprint 8 (`counters`, `daily-opened`, `daily-worked`, `by-state`).
- Enforcement ruolo SUPERVISORE verificato a runtime (403 su operatore).
- Vertical slice BPM/UI/API eseguibile localmente, senza alterare lifecycle pratica definito dal Discovery.
- Presente un conflitto semantico residuo sul perimetro del filtro mese per il grafico by-state.

## Azioni immediate
1. Richiamare BA e Architect per decisione vincolante su `US-E9.05` applicata a `by-state`.
2. Richiamare develop-backend per fix della strategia test `SupervisionDashboardServiceTest` (H2/Flyway).
3. Dopo risoluzione ISS-S8-01 e ISS-S8-02, rieseguire smoke/regression Sprint 8 e aggiornare gate a GO pieno.
