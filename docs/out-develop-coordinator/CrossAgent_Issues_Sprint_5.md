# CrossAgent Issues — Sprint 5

Data: 2026-05-15

## Issue aperte

| ID | Severità | Tema | Owner | Stato | Note |
|---|---|---|---|---|---|
| ISS-S5-01 | SEV-2 | QA runtime non eseguito dall’agente (BLOCKED-EXEC) | develop-qa / Coordinator | OPEN | Script e report generati, manca esecuzione live AC-S5-01..07. |
| ISS-S5-02 | SEV-3 | Conformità completa a checklistSD.xlsx non ancora evidenziata con matrice item-by-item | Coordinator + BA support | OPEN | Serve tracciamento puntuale su [docs/requirements/source-of-truth/checklistSD.xlsx](../requirements/source-of-truth/checklistSD.xlsx). |
| ISS-S5-03 | SEV-3 | Dipendenza UX legacy su milestone riepilogo da validare end-to-end operatore | develop-frontend + develop-qa | OPEN | Necessaria prova guidata da UI reale dopo salvataggio checklist. |

## Issue chiuse nello sprint

| ID | Severità | Tema | Owner | Stato | Evidenza |
|---|---|---|---|---|---|
| ISS-S5-00 | SEV-1 | Errore SQL tipizzazione (`Unknown column 'version' in 'SET'`) | Coordinator | CLOSED | Query corretta in [apps/backend/src/main/java/it/poste/anc/document/application/IntakeTypingService.java](../../apps/backend/src/main/java/it/poste/anc/document/application/IntakeTypingService.java). |
| ISS-S5-04 | SEV-2 | Migration V10 creata in path non standard | Coordinator | CLOSED | Migration spostata in [infra/db/migrations/V10__intake_checklist_verbale.sql](../../infra/db/migrations/V10__intake_checklist_verbale.sql), Flyway success=1. |

## Coerenza cross-agent
- Backend e frontend allineati sugli endpoint intake/checklist Sprint5.
- Nessuna anticipazione Sprint6 (no close pratica, no send outcome BPM, no ack BPM) rilevata nelle patch.
- Workflow e state management preservati rispetto ai vincoli Discovery.

## Azioni immediate
1. Eseguire runbook QA Sprint5 e consolidare PASS/FAIL in report finale.
2. Produrre matrice di copertura `checklistSD.xlsx` ↔ campi UI/API Sprint5.
3. Aggiornare gate Sprint_Status_Sprint_5.md da CONDITIONAL-GO a GO/NO-GO in base agli AC runtime.
