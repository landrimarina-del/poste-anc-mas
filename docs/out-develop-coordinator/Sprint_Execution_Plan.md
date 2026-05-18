# Sprint Execution Plan - Sprint 0 Rebaseline

## Missione Sprint
Rieseguire Sprint 0 dopo rilancio dei discovery, verificando la coerenza cross-agent e includendo la baseline tecnica di integrazione Flowable richiesta dal coordinamento, senza anticipare feature business degli sprint successivi.

## Perimetro attivo
- Solo Sprint 0.
- Nessuna modifica roadmap.
- Nessuna implementazione dei task operativi ANC (presa in carico, checklist, chiusura).
- Coerenza obbligatoria tra output BA, Architect, UX, Backend, Frontend, QA.

## Input documentali consolidati
- docs/out-discovery-business-analist/03_Roadmap_Porting.md
- docs/out-discovery-business-analist/04_Epic_UserStories.md
- docs/out-discovery-business-analist/06_Dipendenze_Funzionali.md
- docs/out-discovery-architect/03_Package_Structure.md
- docs/out-discovery-architect/04_Workflow_Architecture.md
- docs/out-discovery-architect/05_API_Candidate.md
- docs/out-discovery-architect/06_State_Management.md
- docs/out-discovery-architect/10_POC_Runtime_Simplification_Matrix.md
- docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md

## Piano esecutivo applicato
1. Stream backend (develop-backend)
- Riallineamento foundation auth/ruoli Sprint 0.
- Introduzione baseline Flowable tecnica (dipendenze/config/readiness) con placeholder BPMN tecnico.
- Fix successivo del blocker compilazione Flowable emerso da QA.

2. Stream frontend (develop-frontend)
- Riallineamento delle schermate al perimetro Sprint 0 foundation.
- Conferma route protette e gestione sessione.
- Introduzione indicatore tecnico readiness workflow non invasivo.

3. Stream QA (develop-qa)
- Validazione pre-fix e post-fix del rebaseline Sprint 0.
- Chiusura blocker critico Flowable di compilazione.
- Verdetto finale aggiornato con issue residue.

## Definition of Done Sprint 0 (attesa)
1. Backend foundation compilabile e avviabile.
2. Baseline Flowable presente in modalita tecnica non bloccante.
3. Frontend foundation funzionante (login, route protette, tab principali).
4. Nessuna feature business Sprint 1+ anticipata.
5. Smoke Sprint 0 con evidenza esecuzione.
6. Coerenza documentale minima tra percorsi BA e istruzioni operative.

## Esito corrente
Stato sprint: NO-GO condizionato.

Motivi residui:
- assenza di evidenza runtime smoke E2E eseguita in sessione;
- incoerenza documentale sui path BA (analyst vs analist).

## Exit criteria per GO
1. Eseguire smoke runtime locale backend/frontend/readiness con evidenze PASS.
2. Normalizzare i riferimenti BA ufficiali (oppure definire alias documentato).
