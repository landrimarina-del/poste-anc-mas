# 05 - API Candidate

> Stile: REST + JSON, contratti versionati `/api/v1`. Autenticazione: token (Basic in POC, OIDC nel target). Le API verso BPM esterno sono modellate sull'Interface Agreement v1.4 esistente; le API verso il FE sono interne all'applicazione.

## 1. Mappa per bounded context

| Gruppo | Owner BC | Path base |
|---|---|---|
| Auth | M-IAM | `/api/v1/auth` |
| Pratiche (repo + dettaglio) | BC1 | `/api/v1/practices` |
| Allegati & Viewer | BC3 | `/api/v1/practices/{id}/attachments`, `/api/v1/attachments/{id}` |
| Tipizzazione & Checklist | BC3 | `/api/v1/practices/{id}/intake` |
| Lista Attività & Task | BC2 | `/api/v1/tasks` |
| Riassegnazione | BC2 | `/api/v1/supervision/tasks` |
| Supervisione (KPI) | BC5 | `/api/v1/supervision/dashboard` |
| Segnalazioni | BC6 | `/api/v1/signals` |
| Audit / Storia | M-Audit (esposto da BC1) | `/api/v1/practices/{id}/history`, `/states` |
| BPM Integration (in) | BC4 | `/api/v1/bpm/practices` |
| BPM Integration (out / ack) | BC4 | `/api/v1/bpm/outcome-ack` |
| Sinergia stub | BC6 | `/api/v1/sinergia/...` (esterno) |

## 2. Endpoint candidati

### 2.1 Auth (M-IAM)
| Metodo | Path | Scopo | Capability |
|---|---|---|---|
| POST | `/auth/login` | login utente, restituisce token + ruoli | C8.2 |
| POST | `/auth/logout` | invalida sessione | C8.2 |
| GET  | `/auth/me` | profilo + ruoli | C8.1 |

### 2.2 Pratiche (BC1)
| Metodo | Path | Scopo | Capability |
|---|---|---|---|
| GET  | `/practices` | lista paginata + filtri (n°, stato, date, esito) | C2.2, C2.3, C2.4 |
| GET  | `/practices/export` | export Excel della lista filtrata | C2.5 |
| GET  | `/practices/{id}` | dettaglio Riepilogo (testata + cliente + carta) | C2.6 |
| GET  | `/practices/{id}/history` | tab Cronologia | C2.7 |
| GET  | `/practices/{id}/states` | tab Stati | C2.8 |
| GET  | `/practices/{id}/related-actions` | tab Azioni Correlate | C2.9 |

### 2.3 Allegati & Viewer (BC3)
| Metodo | Path | Scopo | Capability |
|---|---|---|---|
| GET  | `/practices/{id}/attachments` | elenco allegati pratica | C4.1 |
| GET  | `/attachments/{id}/preview` | stream per viewer integrato (range request) | C4.1 |
| GET  | `/attachments/{id}/download` | download manuale fallback | C4.2 |

### 2.4 Tipizzazione & Checklist (BC3)
| Metodo | Path | Scopo | Capability |
|---|---|---|---|
| POST | `/practices/{id}/intake/typing` | conferma tipo documento (Verbale|Carta) — irreversibile | C4.3 |
| GET  | `/practices/{id}/intake/checklist` | recupera checklist abilitata in base alla tipizzazione | C4.4, C4.5 |
| PUT  | `/practices/{id}/intake/checklist` | salva bozza ("Salva e Prosegui") | C4.8 |
| POST | `/practices/{id}/intake/checklist/edit` | sblocca per modifica | C4.9 |
| GET  | `/practices/{id}/intake/checklist/help/{itemId}` | help in linea ("Mostra Descrizione") | C4.10 |
| POST | `/practices/{id}/intake/close` | CHIUDI PRATICA: rimuove task, transita a IN_ATTESA_CONFERMA_BPM | C5.3 |

### 2.5 Task (BC2)
| Metodo | Path | Scopo | Capability |
|---|---|---|---|
| GET  | `/tasks` | Lista Attività dell'utente (filtri n°, stato; tipo pratica fisso = ANC) | C3.2, C3.5 |
| POST | `/tasks/{id}/accept` | ACCETTA → diventa owner; pratica → IN_LAVORAZIONE | C3.3 |
| POST | `/tasks/{id}/back` | tasto Indietro (annulla schermata accettazione) | C3.4 |

### 2.6 Supervisione (BC5 + BC2)
| Metodo | Path | Scopo | Capability |
|---|---|---|---|
| GET  | `/supervision/tasks` | lista task processi di competenza (filtri owner/assegnatario/data) | C6.1, C6.4 |
| POST | `/supervision/tasks/{id}/reassign-group` | riassegna a Gruppo Operatore ANC | C6.2 |
| POST | `/supervision/tasks/{id}/reassign-user` | riassegna a utente specifico | C6.3 |
| GET  | `/supervision/dashboard/counters` | contatori real-time (Attività, Pratiche Attive, Pratiche Chiuse) | C6.5 |
| GET  | `/supervision/dashboard/daily-opened?month=YYYY-MM` | istogramma Pratiche Giornaliere | C6.6 |
| GET  | `/supervision/dashboard/daily-worked?month=YYYY-MM` | istogramma Pratiche Giornaliere Lavorate (OK/KO) | C6.7 |
| GET  | `/supervision/dashboard/by-state` | istogramma Pratiche per Stato | C6.8 |

### 2.7 Segnalazioni (BC6)
| Metodo | Path | Scopo | Capability |
|---|---|---|---|
| POST | `/signals` | invio segnalazione legata a pratica | C7.1 |
| GET  | `/signals/me` | "Le Mie Segnalazioni" | C7.2 |
| GET  | `/signals` | vista globale (filtri ID, stato, operatore, range date) | C7.3 |
| POST | `/signals/{id}/reassign` | riassegna segnalazione | C7.4 |
| POST | `/signals/{id}/forward-sinergia` | inoltra a Sinergia (stub) | C7.5 |

### 2.8 BPM Integration (BC4) — verso/da BPM esterno
| Metodo | Path | Scopo | Capability |
|---|---|---|---|
| POST | `/bpm/practices` | **inbound**: BPM richiede apertura pratica (payload Interface Agreement BPM↔SD); restituisce `resultCode` (0/-4/-5) | C1.1, C1.2, C1.3, C1.4 |
| POST | `/bpm/outcome-ack` | **inbound**: BPM conferma ricezione esito → finalizza stato | C5.6 |
| (out)| → `bpm-stub /receive-outcome` | **outbound**: SD invia esito (single/KO multipli) | C5.4, C5.5 |

## 3. Mapping capability → API (riepilogo)

| Capability BA | API |
|---|---|
| C1.1 Servizio apertura pratica | POST `/bpm/practices` |
| C1.2 Validazione | POST `/bpm/practices` (validatore) |
| C1.3 Idempotenza | POST `/bpm/practices` (vincolo unique su `id_workitem`) |
| C1.4 ResultCode | response POST `/bpm/practices` |
| C2.x Repository | GET `/practices*` |
| C3.x Lista/Accept | GET `/tasks`, POST `/tasks/{id}/accept` |
| C4.x Tipizzazione/Checklist/Viewer | `/intake/*`, `/attachments/*` |
| C5.x Esito/Chiusura/Sync | `/intake/close`, outbound `bpm-stub`, POST `/bpm/outcome-ack` |
| C6.x Supervisione | `/supervision/*` |
| C7.x Segnalazioni | `/signals/*` |
| C8.x Auth/Home | `/auth/*`, dashboard contatori |
| C9.1/C9.2 Audit/Stati | `/practices/{id}/history`, `/states` |

## 4. Convenzioni contrattuali

- **Versioning**: prefisso `/api/v1`. Breaking change → `/api/v2` con deprecazione documentata.
- **Errori**: response uniforme `{ resultCode, resultMessage, details? }`. Per BC4 i codici negativi `-4`/`-5` sono baseline immutabile (Discovery).
- **Idempotenza**: BC4 inbound → vincolo unique su `id_workitem`; le API di mutazione di task usano `If-Match` (versione) per evitare doppie conferme.
- **Pagination**: `?page=&size=&sort=` con metadata `{ total, page, size }`.
- **Audit**: ogni mutazione produce evento di dominio (cap. 04 §6).
