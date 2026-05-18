# CrossAgent Issues - Sprint 10

Data: 2026-05-16

## Issue aperte

| ID | Severita | Tema | Owner | Stato | Note |
|---|---|---|---|---|---|
| ISS-S10-05 | Medium | Review Discovery BA su narrativa demo supervisore | Coordinator/BA | Open | Richiesta inviata in `Richiesta_Review_Discovery_BA_Demo_Supervisore_Sprint_10.md`; in attesa feedback gap. |

## Issue chiuse

| ID | Severita | Tema | Owner | Stato | Evidenza |
|---|---|---|---|---|---|
| ISS-S10-01 | High | Report QA inizialmente BLOCKED per assenza runtime | QA/Coordinator | Closed | Smoke runtime eseguiti, report QA riallineati a PASS. |
| ISS-S10-02 | Medium | Mismatch FE/BE su payload favoriti (`label` vs `titolo` + `tipo`) | Frontend/Backend | Closed | CRUD favoriti validato runtime (GET/CREATE/DELETE). |
| ISS-S10-03 | High | Duplicazione migration Flyway su favoriti | Backend | Closed | Rimosse migration duplicate, backend avviato stabilmente. |
| ISS-S10-04 | Medium | Errore UX su URL favoriti senza schema | Frontend | Closed | Normalizzazione URL lato FE (`https://` automatico) confermata. |

## Coerenza cross-agent
- Sprint 10 completato su scope EPIC E11 (hardening & polish).
- Nessuna anticipazione di sprint successivi.
- Coerenza BPM/UI/API preservata su capability C2.5, C2.9, C4.10, C4.11, C8.3, C9.1.
- Dipendenze soddisfatte da sprint precedenti (S0-S9 completati).

## Azioni immediate
1. Archiviare Sprint 10 come chiuso (GO) nella governance MAS.
2. Mantenere monitoraggio non bloccante su salute `bpm-stub` in ambiente locale.
