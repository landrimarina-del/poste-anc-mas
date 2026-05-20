# CrossAgent Issues - Sprint 10

Data: 2026-05-16

## Issue aperte

| ID | Severita | Tema | Owner | Stato | Note |
|---|---|---|---|---|---|
| ISS-S10-05 | Medium | Review Discovery BA su narrativa demo supervisore | Coordinator/BA | Open | Richiesta inviata in `Richiesta_Review_Discovery_BA_Demo_Supervisore_Sprint_10.md`; in attesa feedback gap. |
| ISS-S10-06 | High | Flyway: V100 in classpath anziché infra/db/migrations | Backend/Architect | Open | Vedere analisi DIFF-BE-1 di seguito. Richiede autorizzazione prima di qualsiasi azione. |

### Analisi DIFF-BE-1 — Flyway migration naming (2026-05-19)

**Stato attuale:**
- `V100__favorite_link_crud.sql` esiste in `apps/backend/src/main/resources/db/migration/` (classpath)
- NON duplicato in `infra/db/migrations/`
- Java migration V2 confermata in `apps/backend/src/main/java/db/migration/V2__seed_demo_users.java`
- Prossimo numero libero in `infra/db/migrations/`: **V15**
- Nessuna traccia di `flyway_schema_history` verificabile nel workspace (nessun dump SQL)

**Conflitti rilevati:**
- V100 vs V1-V14: **nessun conflitto numerico** (100 > 14, ordine di applicazione corretto)
- Coesistenza due directory: **intenzionale** (documentata in application.yml), ma V100 è in posizione anomala — il classpath è riservato esclusivamente alle Java migrations per architettura
- Naming: **discrepanza** tra README Sprint 10 (`V20260516_01__favorite_link_crud.sql`) e file fisico (`V100__favorite_link_crud.sql`)

**Raccomandazione agente backend:**
Spostare in `infra/db/migrations/V15__sprint10_favorite_link_crud.sql`. Rischio operativo **ALTO** se il DB locale ha già applicato V100 (rename causa mismatch checksum o re-applicazione). Richede verifica preventiva `flyway_schema_history` e autorizzazione esplicita del Coordinator/Architect.

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
