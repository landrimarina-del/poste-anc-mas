# Decision Log - Sprint 9

Data compilazione: 2026-05-16  
Decision owner: BA Discovery / Architect Discovery  
Riferimento issue: ISS-S9-01  
Riferimento richiesta: [docs/out-develop-coordinator/Clarification_Request_Sprint_9_Signal_Work.md](Clarification_Request_Sprint_9_Signal_Work.md)

## Oggetto decisione

Conferma ufficiale sul perimetro dello step signal.work relativo a endpoint `POST /api/v1/signals/{id}/take`.

## Esito

- [ ] Opzione A - Endpoint take in scope Sprint 9
- [x] Opzione B - De-scope formale endpoint take da Sprint 9

## Motivazione BA/Architect

Per la vertical slice Sprint 9 la presa in carico operativa della segnalazione e gia coperta dal flusso implementato di riassegnazione (`targetType=ME|USER`) con transizione `IN_CODA -> IN_LAVORAZIONE` validata runtime. L'endpoint `take` non e richiesto come prerequisito tecnico per il completamento della capability C7 in Sprint 9 e viene rinviato a eventuale enhancement successivo.

Riferimenti:
- [docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md](../out-discovery-ux-mapper/BPM_Task_UI_Mapping.md)
- [docs/out-discovery-architect/05_API_Candidate.md](../out-discovery-architect/05_API_Candidate.md)
- [apps/backend/src/main/java/it/poste/anc/signals/application/SignalService.java](../../apps/backend/src/main/java/it/poste/anc/signals/application/SignalService.java)

## Impatti approvati

- Backend: nessuna modifica codice richiesta per Sprint 9.
- Frontend: nessuna modifica codice richiesta per Sprint 9.
- QA: chiusura rilievo residuo BPM su endpoint `take` come de-scope formale.
- Gate Sprint 9: promozione a GO pieno dopo allineamento documentale coordinator.

## Azioni coordinator eseguite

1. Chiusura ISS-S9-01 in [docs/out-develop-coordinator/CrossAgent_Issues_Sprint_9.md](CrossAgent_Issues_Sprint_9.md).
2. Aggiornamento gate a GO in [docs/out-develop-coordinator/Sprint_Status_Sprint_9.md](Sprint_Status_Sprint_9.md).
3. Chiusura richiesta di chiarimento in [docs/out-develop-coordinator/Clarification_Request_Sprint_9_Signal_Work.md](Clarification_Request_Sprint_9_Signal_Work.md).

## Firma decisione

- BA Discovery: APPROVATO
- Architect Discovery: APPROVATO
- Data: 2026-05-16
