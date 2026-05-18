# GAP-BLOCKER-001 — Orchestration Log (Coordinator)

Data: 2026-05-15
Modalità: discovery-coordinator (analisi GAP mirata)
Scope: SOLO GAP-BLOCKER-001 — Document Transport Mode

## Agenti coinvolti

| Agente | Coinvolto | Motivo inclusione/esclusione |
|---|---|---|
| Discovery-Architect | SI | Impatto su BC3/BC4, API allegati, workflow `svc.openPractice`, deployment locale, runtime simplification matrix |
| Discovery-DBA | SI | Impatto su schema `attachment` (lifecycle ingestione, nullability, vincoli) |
| Discovery-Business-Analyst | NO | Su richiesta utente; nessun delta funzionale ai backlog/user story richiesto |
| Discovery-UX-Mapper | NO | Su richiesta utente; viewer e download già definiti in Sprint 4 |

## Sequenza di esecuzione

1. Lettura GAP ([docs/discovery-delta/GAP-BLOCKER-001_Document_Transport_Mode.md](docs/discovery-delta/GAP-BLOCKER-001_Document_Transport_Mode.md))
2. Discovery-Architect e Discovery-DBA invocati in PARALLELO (read-only, no dipendenza reciproca; perimetro mirato al GAP)
3. Consolidamento e Cross-Agent Validation a cura del Coordinator

## Vincoli enforced

- NON rigenerata la discovery completa
- NON modificata la baseline roadmap ([docs/out-discovery-business-analist/03_Roadmap_Porting.md](docs/out-discovery-business-analist/03_Roadmap_Porting.md))
- NON modificate user story / acceptance criteria consolidati
- NON modificati gli sprint precedenti
- Output Architect/DBA in modalità SOLO PROPOSTA: le riscritture documentali non sono applicate; sono elencate come delta puntuali

## Dipendenze risolte

- Nessuna dipendenza Architect→DBA stretta per la sola IMPACT ANALYSIS: entrambi leggono il GAP + i propri output discovery + lo schema `attachment` reale (V3, V6).
- L'allineamento DDL canonico vs migrazioni reali è cross-agent: gestito nel Validation Report.

## Conflitti rilevati

Vedi [GAP-BLOCKER-001_CrossAgent_Validation.md](docs/discovery-delta/GAP-BLOCKER-001_CrossAgent_Validation.md). Nessun conflitto bloccante; due punti di allineamento richiesti (naming `storage_uri` e semantica `ingestion_status` in modalità sync).

## Stato deliverable

| Deliverable | Stato |
|---|---|
| Impact Analysis consolidata | EMESSO |
| Delta Documentali consolidati | EMESSO |
| Remediation Sprint 4 | EMESSO |
| Cross-Agent Validation Report | EMESSO |
