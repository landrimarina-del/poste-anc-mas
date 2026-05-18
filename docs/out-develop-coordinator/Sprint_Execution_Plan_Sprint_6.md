# Sprint Execution Plan - Sprint 6

Data: 2026-05-15
Owner orchestrazione: develop-coordinator

## Obiettivo Sprint
Implementare la vertical slice Checklist Carta e Chiusura task con sincronizzazione BPM:
- C4.5
- C5.3
- C5.4
- C5.5
- C5.6
- C5.7
- US-E7.01..US-E7.05

Riferimenti Discovery:
- [docs/out-discovery-business-analist/03_Roadmap_Porting.md](../out-discovery-business-analist/03_Roadmap_Porting.md)
- [docs/out-discovery-business-analist/04_Epic_UserStories.md](../out-discovery-business-analist/04_Epic_UserStories.md)
- [docs/out-discovery-business-analist/06_Dipendenze_Funzionali.md](../out-discovery-business-analist/06_Dipendenze_Funzionali.md)
- [docs/out-discovery-architect/04_Workflow_Architecture.md](../out-discovery-architect/04_Workflow_Architecture.md)
- [docs/out-discovery-architect/06_State_Management.md](../out-discovery-architect/06_State_Management.md)
- [docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md](../out-discovery-ux-mapper/BPM_Task_UI_Mapping.md)

## Scope Sprint 6
1. Checklist Carta attiva solo con tipizzazione CARTA.
2. Regole outcome carta:
- card_present = NO -> RESPINTA automatica.
- card_present = SI richiede card_conformity_ok.
3. CHIUDI PRATICA:
- task rimosso dalla lista attività.
- stato pratica in IN_ATTESA_CONFERMA_BPM.
4. Outbound esito verso bpm-stub.
5. Endpoint ACK inbound da BPM con finalizzazione:
- CHIUSA_OK oppure CHIUSA_KO.
- data_chiusura valorizzata.
6. Storico stati e audit coerenti.

Out of scope:
- dashboard supervisore.
- segnalazioni.
- hardening sprint 10.

## Orchestrazione agenti

### Stream backend (develop-backend)
Consegne principali:
- Migrazione:
  - [infra/db/migrations/V11__sprint6_carta_close_bpm_ack.sql](../../infra/db/migrations/V11__sprint6_carta_close_bpm_ack.sql)
- Checklist BC3 e close:
  - [apps/backend/src/main/java/it/poste/anc/document/application/IntakeChecklistService.java](../../apps/backend/src/main/java/it/poste/anc/document/application/IntakeChecklistService.java)
  - [apps/backend/src/main/java/it/poste/anc/document/application/IntakePracticeCloseService.java](../../apps/backend/src/main/java/it/poste/anc/document/application/IntakePracticeCloseService.java)
  - [apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistController.java](../../apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistController.java)
  - [apps/backend/src/main/java/it/poste/anc/document/api/IntakeCloseResponse.java](../../apps/backend/src/main/java/it/poste/anc/document/api/IntakeCloseResponse.java)
- BPM gateway outbound/inbound ack:
  - [apps/backend/src/main/java/it/poste/anc/bpmgw/outbound/BpmOutcomeOutboundGateway.java](../../apps/backend/src/main/java/it/poste/anc/bpmgw/outbound/BpmOutcomeOutboundGateway.java)
  - [apps/backend/src/main/java/it/poste/anc/bpmgw/outbound/HttpBpmOutcomeOutboundGateway.java](../../apps/backend/src/main/java/it/poste/anc/bpmgw/outbound/HttpBpmOutcomeOutboundGateway.java)
  - [apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmOutcomeAckController.java](../../apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmOutcomeAckController.java)
  - [apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmOutcomeAckService.java](../../apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmOutcomeAckService.java)

### Stream frontend (develop-frontend)
Consegne principali:
- Branch checklist carta in Typing:
  - [apps/frontend/src/features/intake/TypingPage.jsx](../../apps/frontend/src/features/intake/TypingPage.jsx)
- API close pratica:
  - [apps/frontend/src/core/api/intakeApi.js](../../apps/frontend/src/core/api/intakeApi.js)
- Supporto UI stato transitorio/finale:
  - [apps/frontend/src/styles.css](../../apps/frontend/src/styles.css)

### Stream QA (develop-qa)
Consegne principali:
- Runbook e suite AC Sprint 6:
  - [tools/qa/sprint6/README.md](../../tools/qa/sprint6/README.md)
  - [tools/qa/sprint6/run-sprint6-qa.ps1](../../tools/qa/sprint6/run-sprint6-qa.ps1)
  - [tools/qa/sprint6/run-sprint6-qa.sh](../../tools/qa/sprint6/run-sprint6-qa.sh)
- Report:
  - [docs/out-develop-qa/Smoke_Test_Report_Sprint_6.md](../out-develop-qa/Smoke_Test_Report_Sprint_6.md)
  - [docs/out-develop-qa/Sprint_Test_Checklist_Sprint_6.md](../out-develop-qa/Sprint_Test_Checklist_Sprint_6.md)
  - [docs/out-develop-qa/Defect_List_Sprint_6.md](../out-develop-qa/Defect_List_Sprint_6.md)
  - [docs/out-develop-qa/BPM_Workflow_Validation_Sprint_6.md](../out-develop-qa/BPM_Workflow_Validation_Sprint_6.md)

## Validazione tecnica coordinator
- Build backend: PASS.
- Deploy docker compose completo: PASS.
- Flyway: V11 success=1.
- Tabella checklist_carta presente.
- Stack healthy: backend, frontend, db, minio, reverse proxy.

Nota runtime nota:
- bpm-stub risulta unhealthy nel healthcheck ma operativo per i path file; issue già tracciata in sprint precedenti.
