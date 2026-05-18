# CrossAgent Issues - Sprint 6

Data: 2026-05-15

## Issue aperte

| ID | Severità | Tema | Owner | Stato | Note |
|---|---|---|---|---|---|
| ISS-S6-01 | SEV-2 | QA runtime AC Sprint 6 non eseguiti in sessione agente | develop-qa / Coordinator | OPEN | Script e report pronti, esecuzione live pending. |
| ISS-S6-02 | SEV-3 | Polling automatico stato ack BPM non presente in UI | develop-frontend | OPEN | Refresh manuale disponibile; valutare polling in sprint successivo. |
| ISS-S6-03 | SEV-3 | bpm-stub unhealthy su healthcheck docker | infra | OPEN | Servizio comunque operativo per test correnti, da hardenizzare. |

## Issue chiuse

| ID | Severità | Tema | Owner | Stato | Evidenza |
|---|---|---|---|---|---|
| ISS-S6-04 | SEV-1 | Build backend KO su costruttore IntakeChecklistResponse ramo CARTA | Coordinator | CLOSED | Correzione in [apps/backend/src/main/java/it/poste/anc/document/application/IntakeChecklistService.java](../../apps/backend/src/main/java/it/poste/anc/document/application/IntakeChecklistService.java) e build PASS. |
| ISS-S6-05 | SEV-2 | Deploy Sprint 6 senza V11 applicata | Coordinator | CLOSED | Flyway mostra V11 success=1 e tabella checklist_carta presente. |

## Coerenza cross-agent
- Backend e frontend sono allineati sugli endpoint intake checklist e close pratica.
- Flusso stati Discovery preservato: stato finale chiuso solo da ACK BPM.
- Nessuna evidenza di anticipazione scope Sprint 7 o successivi.

## Azioni immediate
1. Eseguire suite QA Sprint 6 runtime e aggiornare esiti AC.
2. Aggiornare gate Sprint 6 da CONDITIONAL-GO a GO/NO-GO in base agli AC runtime.
3. Pianificare hardening healthcheck bpm-stub e polling stato ack UI se confermato dal backlog.
