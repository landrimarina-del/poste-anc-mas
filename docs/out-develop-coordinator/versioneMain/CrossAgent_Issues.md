# CrossAgent Issues - Sprint 0 Rebaseline

## CAI-001 - Path documenti BA incoerenti
| Campo | Valore |
|---|---|
| Severita | Medium |
| Stato | OPEN |
| Owner | Coordinator |

Descrizione:
- Alcune istruzioni operative fanno riferimento a `docs/out-discovery-business-analyst/*`.
- Nel repository i file effettivi sono in `docs/out-discovery-business-analist/*`.

Impatto:
- fallimenti nei check automatici di input documentali;
- rischio errori di orchestrazione nei run successivi.

Azione richiesta:
- definire naming ufficiale unico o alias documentato.

## CAI-002 - Evidenza runtime smoke non disponibile in sessione
| Campo | Valore |
|---|---|
| Severita | High |
| Stato | OPEN |
| Owner | QA |

Descrizione:
- Validazione Sprint 0 chiusa con evidenza statica.
- Manca run live smoke backend/frontend/readiness nello stesso ciclo QA.

Impatto:
- impedisce certificazione GO definitiva di Sprint 0.

Azione richiesta:
- eseguire smoke runtime locale e allegare esiti tracciabili.

## CAI-003 - Blocker compilazione Flowable (storico)
| Campo | Valore |
|---|---|
| Severita | Critical |
| Stato | CLOSED |
| Owner | Backend |

Descrizione:
- Nel primo ciclo rebaseline, classpath Flowable non risolto su file engine/readiness/test.

Risoluzione:
- richiamato agente backend responsabile;
- applicato fix dedicato e chiusura blocker confermata da QA post-fix.

## CAI-004 - Warning type-safety test auth
| Campo | Valore |
|---|---|
| Severita | Medium |
| Stato | CLOSED |
| Owner | Backend |

Descrizione:
- warning residuo su test auth foundation.

Impatto:
- issue tecnica chiusa; nessun impatto residuo su gate Sprint 0.

Azione richiesta:
- nessuna ulteriore azione.

## Decisione di coordinamento
- Nessuna correzione autonoma cross-stream fuori ownership.
- Le issue aperte restano in carico agli owner indicati e guidano il prossimo gate GO/NO-GO.
