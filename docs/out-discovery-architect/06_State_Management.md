# 06 - State Management

## 1. Lifecycle pratica (autoritativo)

```
                       (richiesta BPM valida + idempotente)
   [non esistente] ────────────────────────────────────────► APERTA
                                                                │
                                                                │  ACCETTA su task.acceptPractice
                                                                ▼
                                                           IN_LAVORAZIONE
                                                                │
                                                                │  CHIUDI PRATICA (esito calcolato)
                                                                ▼
                                                    IN_ATTESA_CONFERMA_BPM
                                                       │                   │
                                       ack BPM (OK)    │                   │   ack BPM (KO)
                                                       ▼                   ▼
                                                  CHIUSA_OK          CHIUSA_KO
                                                  (sysdate)          (sysdate)
```

## 2. Ownership stati

| Stato | Owner del cambio in INGRESSO | Servizio responsabile |
|---|---|---|
| APERTA | BC4 – BPM Integration | `svc.openPractice` (validazione + idempotenza + persistenza) |
| IN_LAVORAZIONE | BC2 – Workflow | listener azione ACCETTA |
| IN_ATTESA_CONFERMA_BPM | BC2 → BC4 | `svc.sendOutcomeToBpm` |
| CHIUSA_OK / CHIUSA_KO | BC4 | `svc.finalizeOnAck` su `POST /api/v1/bpm/outcome-ack` |

> Vincolo trasversale: nessun altro modulo può mutare lo stato pratica. BC1 conserva i dati ma **non** decide le transizioni.

## 3. Gestione transizioni

- Ogni transizione è atomica e produce in **stessa transazione**:
  1. update di `practice.stato`,
  2. insert in `practice_state_history` (Cronologia stati),
  3. insert in `event_outbox` (notifica eventi: cap. 04 §6).
- Le transizioni sono **idempotenti**: rieseguire la stessa azione su uno stato già finale è no-op (utile in caso di retry da stub BPM).

## 4. Stato dei task (BC2)

```
[creato]  →  IN_CODA  →  IN_CARICO  →  COMPLETATO
                  │            │
                  │            └─► RIASSEGNATO (rimane in IN_CARICO o torna IN_CODA)
                  └─► RIASSEGNATO (a utente / gruppo)
```

| Stato task | Innesco | Effetto su pratica |
|---|---|---|
| IN_CODA | creazione automatica all'apertura pratica | nessuno |
| IN_CARICO | ACCETTA | pratica → IN_LAVORAZIONE |
| COMPLETATO | CHIUDI PRATICA | pratica → IN_ATTESA_CONFERMA_BPM |
| RIASSEGNATO | azione Supervisore | nessun cambio stato pratica |

## 5. Stato della checklist (BC3)

| Stato | Innesco | Note |
|---|---|---|
| NON_INIZIATA | creazione pratica | nessun record `checklist_response` |
| BOZZA | "SALVA E PROSEGUI" | abilita Riepilogo |
| RIAPERTA | "MODIFICA" | torna editabile, esito ricalcolabile |
| CONSOLIDATA | "CHIUDI PRATICA" | snapshot finale legato all'esito inviato a BPM |

Vincoli:
- La **tipizzazione** è uno step pre-checklist con stato `TIPIZZATO` irreversibile.
- L'esito è funzione pura della checklist consolidata (no override manuale).

## 6. Stato segnalazione (BC6)

```
IN_CODA → IN_LAVORAZIONE → CHIUSO
```

Innesco: creazione, presa in carico operatore, chiusura post-ack Sinergia (stub).

## 7. Sincronizzazione cross-context

| Scenario | Pattern |
|---|---|
| Apertura pratica (BC4 → BC1 → BC2) | call sincrona interna nel monolith POC; saga via outbox/broker nel target. |
| Esito calcolato (BC3 → BC2 → BC4) | evento `OutcomeComputed` consumato dal processo BPMN per proseguire. |
| Ack BPM (BC4 → BC1) | aggiornamento atomico stato + timestamp; idempotente sul `id_workitem`. |
| Riassegnazione (BC2) | aggiorna assegnazione task; emette `TaskReassigned`; **non** tocca BC1. |

## 8. Vincoli di consistenza

- **VC-S1**: ogni transizione di `practice.stato` deve avere un record correlato in `practice_state_history` con `actor`, `timestamp`, `from_state`, `to_state`, `correlation_id`.
- **VC-S2**: lo stato `IN_ATTESA_CONFERMA_BPM` ammette ack ritardato → predisporre **timer di sicurezza** configurabile (segnalato come Risk R7 dal BA, gestito a livello BPMN con boundary timer event).
- **VC-S3**: `CHIUSA_OK`/`CHIUSA_KO` sono terminali. Qualsiasi nuova richiesta su pratica chiusa è respinta.
- **VC-S4**: la tipizzazione una volta confermata non è modificabile (vincolo applicativo + check DB).
- **VC-S5**: `requestId` è generato all'apertura ed è immutabile per tutto il lifecycle.

## 9. Mapping workflow → entità (riepilogo)

| Step workflow | Entità persistite |
|---|---|
| svc.openPractice | `practice`, `client_data`, `card_data`, `attachment`, `practice_state_history`, `event_outbox` |
| task.acceptPractice (ACCETTA) | `task` (owner, stato), `practice` (stato), `practice_state_history` |
| task.typeAndChecklist | `practice` (tipo doc), `checklist_response`, `audit_event` |
| svc.computeOutcome | `practice_outcome` (snapshot) |
| svc.sendOutcomeToBpm | `bpm_outbound_message`, `practice_state_history` |
| svc.finalizeOnAck | `practice` (stato finale, dataChiusura), `practice_state_history` |
| signal lifecycle | `signal`, `signal_state_history` |

> Lo schema dati di dettaglio è di competenza del Discovery-DBA.
