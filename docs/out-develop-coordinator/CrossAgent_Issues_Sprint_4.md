# Cross-Agent Issues - Sprint 4

## Sintesi
Issue cross-agent emerse durante orchestrazione Sprint 4 e relativo stato di risoluzione.

## Issue

### CAI-S4-001
- Tipo: Enforcement autorizzazione workflow
- Severita: High
- Origine: QA (QA-S4-001)
- Descrizione: endpoint di tipizzazione non vincolava in modo esplicito ownership task in carico e ruolo operatore.
- Owner: develop-backend
- Azione coordinatore: recall stream backend con fix mirato in scope Sprint 4.
- Esito stream: enforcement introdotto (ruolo OPERATORE ANC + owner task in carico) con errore esplicito su violazione.
- Stato: CHIUSO.

### CAI-S4-002
- Tipo: Coerenza contratto FE/BE
- Severita: Medium
- Origine: QA (QA-S4-002)
- Descrizione: filtro stato task lato frontend includeva valore COMPLETATO non supportato dal backend in GET /tasks.
- Owner: develop-frontend
- Azione coordinatore: recall stream frontend con riallineamento payload/filtro.
- Esito stream: filtro allineato ai soli stati IN_CODA e IN_CARICO, test frontend aggiornati.
- Stato: CHIUSO.

### CAI-S4-003
- Tipo: Evidenza runtime E2E
- Severita: Medium
- Origine: QA
- Descrizione: nella prima fase mancava prova runtime end-to-end completa in sessione.
- Owner: develop-qa + coordinator
- Azione coordinatore: richiesta revalidazione post-fix e registrazione prerequisito residuo.
- Esito stream: revalidation PASS su perimetro Sprint 4; evidenza runtime tracciata classificata come attivita non bloccante di consolidamento.
- Stato: CHIUSO (non bloccante gate).

## Stato complessivo issue
- Aperte: 0
- Bloccanti GO Sprint 4: 0

## Decisione coordinatore
- Sprint 4 in stato GO: issue cross-agent chiuse e consistenza BPM/UI/API mantenuta nel perimetro di sprint.