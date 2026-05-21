# Sprint Execution Plan - Sprint 10

Data: 2026-05-16  
Owner orchestrazione: develop-coordinator

## Obiettivo Sprint
Completare la vertical slice di hardening e polish POC su EPIC E11:
- C2.5 (Export Excel lista pratiche)
- C2.9 (Azioni Correlate)
- C4.10 (Help in linea checklist)
- C4.11 (Espandi/Comprimi sezioni UI)
- C8.3 (Link Favoriti CRUD)
- C9.1 (audit trail completo)

User Stories target:
- US-E11.01
- US-E11.02
- US-E11.03
- US-E11.04
- US-E11.05
- US-E11.06

Riferimenti Discovery:
- [docs/out-discovery-business-analist/02_Capability_Map.md](../out-discovery-business-analist/02_Capability_Map.md)
- [docs/out-discovery-business-analist/03_Roadmap_Porting.md](../out-discovery-business-analist/03_Roadmap_Porting.md)
- [docs/out-discovery-business-analist/04_Epic_UserStories.md](../out-discovery-business-analist/04_Epic_UserStories.md)
- [docs/out-discovery-business-analist/06_Dipendenze_Funzionali.md](../out-discovery-business-analist/06_Dipendenze_Funzionali.md)
- [docs/out-discovery-architect/05_API_Candidate.md](../out-discovery-architect/05_API_Candidate.md)
- [docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md](../out-discovery-ux-mapper/BPM_Task_UI_Mapping.md)

## Scope Sprint 10
1. Export Excel lista pratiche da repository.
2. Help contestuale "Mostra Descrizione" per controlli checklist.
3. Espandi/Comprimi sezioni UI in pagine pratica/intake.
4. CRUD Link Favoriti in Home.
5. Tab Azioni Correlate nel dettaglio pratica.
6. Completamento audit trail pratiche con evidenze consultabili.

Out of scope:
- nuove capability oltre EPIC E11.
- redesign architetturale e anticipazioni sprint successivi.

## Orchestrazione agenti

### Stream backend (develop-backend)
Consegne richieste:
1. Endpoint export Excel lista pratiche (C2.5).
2. Endpoint help checklist item (C4.10) se gap residuo.
3. Endpoint/servizio Azioni Correlate (C2.9) e allineamento audit trail (C9.1).
4. Test backend e/o smoke API a supporto.

### Stream frontend (develop-frontend)
Consegne richieste:
1. UI export Excel da Lista Pratiche.
2. UX "Mostra Descrizione" sui controlli checklist.
3. Toggle espandi/comprimi sezioni pratiche/intake.
4. UI Link Favoriti CRUD in Home.
5. Tab Azioni Correlate dettaglio pratica.
6. Test frontend su nuove feature Sprint 10.

### Stream QA (develop-qa)
Consegne richieste:
1. Smoke E2E Sprint 10 aggiornato.
2. Checklist regressione con focus E11.
3. Defect list e validazione coerenza BPM/UI/API post-hardening.

## Gate target
- Obiettivo gate Sprint 10: GO.
- Condizioni minime:
  1. backend/frontend funzionanti localmente,
  2. smoke QA PASS,
  3. vertical slice E11 eseguibile end-to-end,
  4. nessuna issue cross-agent bloccante aperta.
