# Cross-Agent Issues - Sprint 2

## Sintesi
Issue raccolte in orchestrazione Sprint 2 tra stream backend, frontend e QA.

## Issue aperte

### CAI-S2-001
- Tipo: Scope governance
- Severita': High
- Origine: QA
- Descrizione: feature `/riassegna-attivita` presente nel perimetro frontend, ma roadmap la colloca in Sprint 7.
- Evidenza: docs/out-develop-qa/Defect_List_Sprint_2.md (DQ-S2-001)
- Impatto: violazione regola "non anticipare sprint successivi".
- Owner: develop-frontend
- Azione: isolare/disattivare feature fuori scope nel ramo Sprint 2.
- Stato: CHIUSO (post-fix frontend e revalidazione QA).

### CAI-S2-002
- Tipo: Regressione funzionale non certificata
- Severita': High
- Origine: QA
- Descrizione: manca evidenza runtime completa che pratiche create via inbound Sprint 1 siano visibili nella lista Sprint 2.
- Evidenza: docs/out-develop-qa/Smoke_Test_Report_Sprint_2.md (FAIL)
- Impatto: impossibile certificare vertical slice E2E.
- Owner: develop-backend + develop-frontend + develop-qa
- Azione: eseguire test tracciato end-to-end con evidenze log/API/UI e allegare report.
- Stato: CHIUSO (evidenza runtime manuale operatore confermata e recepita da QA).

### CAI-S2-003
- Tipo: Tracciabilita' documentale
- Severita': Medium
- Origine: QA
- Descrizione: alcuni riferimenti BA non reperiti ai path attesi dal mandato QA.
- Evidenza: docs/out-develop-qa/Defect_List_Sprint_2.md (DQ-S2-003)
- Impatto: riduzione tracciabilita' AC <-> implementazione.
- Owner: Coordinator
- Azione: normalizzare path BA e aggiornare checklist QA con riferimenti consolidati.
- Stato: CHIUSO (path BA riallineati su out-discovery-business-analist).

## Stato complessivo issue
- Aperte: 0
- Bloccanti GO Sprint 2: 0

## Decisione coordinatore
- Sprint 2 in stato GO: tutte le issue cross-agent risultano chiuse.
