# Clarification Request - Sprint 9 Signal Work

Data: 2026-05-16  
Owner: develop-coordinator  
Ambito: EPIC E10 (Sprint 9), solo punto residuale su step `signal.work`.

## Contesto

Durante la validazione Sprint 9, la vertical slice Segnalazioni e stata completata e validata runtime su:
- create segnalazione,
- viste my/global,
- reassign,
- forward sinergia,
- enforcement ruoli,
- vincolo VC-1 (stato pratica invariato).

Rimane una sola incoerenza documentale cross-discovery:
- [docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md](../out-discovery-ux-mapper/BPM_Task_UI_Mapping.md) include lo step T12 signal.work con endpoint `POST /api/v1/signals/{id}/take`.
- [docs/out-discovery-architect/05_API_Candidate.md](../out-discovery-architect/05_API_Candidate.md) non espone esplicitamente endpoint take tra le API C7.x.

Nel codice attuale Sprint 9 non e presente endpoint take dedicato in [apps/backend/src/main/java/it/poste/anc/signals/api/SignalController.java](../../apps/backend/src/main/java/it/poste/anc/signals/api/SignalController.java).

## Decisione richiesta a BA/Architect

Confermare una delle due opzioni:

1. Opzione A - In scope Sprint 9
   - confermare che `POST /api/v1/signals/{id}/take` e requisito obbligatorio per US-E10.
   - in questo caso il coordinator richiamera backend/frontend/qa per implementazione e smoke dedicato.

2. Opzione B - De-scope formale
   - confermare che lo step signal.work e coperto dal modello corrente senza endpoint take esplicito.
   - in questo caso il rilievo QA residuo viene chiuso e Sprint 9 passa a GO pieno.

## Evidenze gia disponibili

- Stato sprint: [docs/out-develop-coordinator/Sprint_Status_Sprint_9.md](Sprint_Status_Sprint_9.md)
- Issue cross-agent: [docs/out-develop-coordinator/CrossAgent_Issues_Sprint_9.md](CrossAgent_Issues_Sprint_9.md)
- Smoke QA runtime: [docs/out-develop-qa/Smoke_Test_Report_Sprint_9.md](../out-develop-qa/Smoke_Test_Report_Sprint_9.md)
- BPM validation QA: [docs/out-develop-qa/BPM_Workflow_Validation_Sprint_9.md](../out-develop-qa/BPM_Workflow_Validation_Sprint_9.md)

## Stato richiesta

Escalation chiusa il 2026-05-16.

Esito approvato:
- Opzione B (de-scope formale endpoint `POST /api/v1/signals/{id}/take` in Sprint 9).

Riferimento decisione:
- [docs/out-develop-coordinator/Decision_Log_Sprint_9.md](Decision_Log_Sprint_9.md)
