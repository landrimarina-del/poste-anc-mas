# 03 - Roadmap di Porting Tecnico

> Obiettivo: ricostruzione incrementale per **vertical slice** della soluzione ANC esistente, con sviluppo controllabile dall'utente umano e validazione end-to-end ad ogni sprint.

## Principi

- Ogni sprint produce una **funzionalità end-to-end verificabile** (UI + workflow + BE + persistenza + test).
- **Nessuna nuova capability** rispetto alla baseline.
- Appian è la **piattaforma legacy sorgente** del porting (la Scrivania Digitale ANC è oggi implementata su Appian) e **non viene integrata** dal target open source.
- BPM, Sinergia, Data Lake sono **stub/mock** in tutti gli sprint.
- Ogni sprint termina con uno **smoke-test** automatizzabile.

## Sintesi Sprint

| Sprint | Tema vertical slice | Capability incluse | Esito demo |
|---|---|---|---|
| Sprint 0 | Foundation tecnica | infra, auth, ruoli, CI/CD locale | login operatore/supervisore funzionante |
| Sprint 1 | Apertura Pratica E2E (BPM stub → SD) | C1.1–C1.5, C2.1, C9.1 | pratica creata in stato "Aperta" via stub BPM e visibile in repository |
| Sprint 2 | Lista Pratiche + Dettaglio (read) | C2.2–C2.4, C2.6–C2.8, C8.1 | navigazione completa repository pratiche |
| Sprint 3 | Lista Attività + Presa in carico | C3.1–C3.3, C3.5, C9.3 | operatore prende in carico task (Aperta → In Lavorazione) |
| Sprint 4 | Tipizzazione + Viewer documenti | C4.1–C4.3, C4.12 | tipizzazione confermata, viewer + download fallback |
| Sprint 5 | Checklist Verbale + Calcolo Esito | C4.4, C4.6, C4.7, C4.8, C4.9, C5.1, C5.2 | esito automatico Approvata/Respinta visibile in Riepilogo |
| Sprint 6 | Checklist Carta + Chiusura task → BPM | C4.5, C5.3, C5.4, C5.5, C5.6, C5.7 | ciclo completo fino a "Chiusa OK/KO" via stub BPM |
| Sprint 7 | Supervisore: Riassegna Attività | C6.1–C6.4, C9.3 | supervisore sposta task tra gruppo/utente |
| Sprint 8 | Home Supervisore: contatori + grafici | C6.5–C6.8 | dashboard con contatori real-time + 3 istogrammi |
| Sprint 9 | Dashboard Segnalazioni (Sinergia stub) | C7.1–C7.5 | invio/visualizzazione/riassegnazione segnalazioni |
| Sprint 10 | Hardening & Polish | C2.5, C4.10, C4.11, C8.3, audit completo, export Excel | release candidate POC |

---

## Sprint 0 – Foundation

**Obiettivo**: piattaforma tecnica pronta per ricevere le slice funzionali.

**Vertical slice**: login → home (vuota) → logout, su entrambi i ruoli.

**Contenuti**:
- Repository, branching, CI minimale.
- Infrastruttura di sviluppo locale (DB + BE + FE) — scelte tecnologiche demandate all'Architect.
- Migration baseline (utenti, ruoli OPERATORE/SUPERVISORE).
- Autenticazione locale e guard di rotta FE.
- Skeleton FE (tab Home/Attività/Pratiche con placeholder).
- Smoke test login.

**Dipendenze**: nessuna.

---

## Sprint 1 – Apertura Pratica E2E

**Obiettivo**: pratica creata da richiesta BPM (mock), persistita, visibile come record minimale.

**Vertical slice**: stub BPM invia richiesta → BE crea pratica (Aperta) → record visibile in repository.

**Contenuti**:
- Servizio di ricezione apertura pratica (contratto funzionale Interface Agreement BPM↔SD, campi essenziali).
- Validazione `DOCUMENTI`, `CODICE_DOC_ID ∈ {1,2,3}` → errore `-4`.
- Idempotenza su `ID_WORKITEM` → errore `-5`.
- Persistenza pratica + dati cliente + dati carta + allegato.
- Audit base (apertura pratica).
- Stub BPM (driver di test).
- Lista pratiche grezza (tabella semplice).

**Dipendenze**: Sprint 0.

---

## Sprint 2 – Repository Pratiche (Lista + Dettaglio read-only)

**Obiettivo**: il repository pratiche è navigabile come in baseline.

**Vertical slice**: utente apre tab Pratiche → filtra/ordina → apre dettaglio → vede Riepilogo, Cronologia, Stati.

**Contenuti**:
- Lista Pratiche con filtri (Pratica N°, Stato, Date, Esito SD).
- Ordinamento, paginazione.
- Dettaglio Pratica: linea avanzamento (Raccolta input / Lavorazione / Chiusura).
- Tab Riepilogo, Cronologia, Stati.
- Endpoint read paginato.

**Dipendenze**: Sprint 1.

---

## Sprint 3 – Lista Attività e Presa in Carico

**Obiettivo**: workflow operativo iniziale: l'operatore vede la coda e accetta un task.

**Vertical slice**: pratica creata → task generato → operatore vede in Lista Attività → ACCETTA → stato "In Lavorazione".

**Contenuti**:
- Generazione automatica task all'apertura pratica.
- Lista Attività (filtri Pratica N°, Stato; tipo pratica = ANC fisso).
- Azione ACCETTA + tasto Indietro.
- Transizione di stato Aperta → In Lavorazione.
- Audit "presa in carico" + utente.
- Autorizzazione: solo OPERATORE può accettare.

**Dipendenze**: Sprint 2.

---

## Sprint 4 – Tipizzazione Documento e Viewer

**Obiettivo**: l'operatore visualizza l'allegato e tipizza (Verbale/Carta) in modo irreversibile.

**Vertical slice**: task accettato → schermata tipizzazione → viewer documento → seleziona tipo → CONFERMA → checklist abilitata (placeholder).

**Contenuti**:
- Visualizzatore integrato per allegati, con regolazione dimensioni (piccolo/medio/grande).
- Download manuale fallback.
- Box informativo errore tecnico.
- Selezione Tipo Documento (Verbale / Carta) + CONFERMA.
- Vincolo irreversibilità (DB-level e UI).
- Audit tipizzazione.

**Dipendenze**: Sprint 3.

---

## Sprint 5 – Checklist Verbale + Calcolo Esito

**Obiettivo**: l'operatore compila checklist Verbale, salva bozza, vede esito calcolato.

**Vertical slice**: tipizzazione = Verbale → checklist verbale → Salva e Prosegui → Riepilogo con esito automatico (Approvata/Respinta) + note.

**Contenuti**:
- Checklist Verbale: presenza documento (Si/No con cascata KO), leggibilità, idoneità formale, coerenza dati cliente, corrispondenza numero carta (facoltativo).
- Causali KO formali (Intestazione/Firme/Timbro/Dichiarazione/Carta PI) — selezione obbligatoria se idoneità = NO.
- Salva e Prosegui (bozza) + Modifica.
- Calcolo esito automatico (regola: tutti SI = OK; almeno un NO = KO).
- Card esito (verde/rossa) sola lettura.
- Note interne facoltative (visibili solo se Respinta).

**Dipendenze**: Sprint 4.

---

## Sprint 6 – Checklist Carta + Chiusura E2E con BPM

**Obiettivo**: completare il ciclo fino agli stati finali "Chiusa OK/KO".

**Vertical slice**: tipizzazione = Carta → checklist Carta → CHIUDI PRATICA → invio esito a stub BPM → conferma BPM → Chiusa OK/KO con sysdate.

**Contenuti**:
- Checklist Carta tagliata (presenza + conformità).
- Azione CHIUDI PRATICA: rimozione task, transizione → In Attesa Conferma BPM.
- Endpoint funzionale di invio esiti verso stub BPM (single OK / single KO / KO multipli).
- Endpoint funzionale di ricezione conferma da stub BPM → transizione finale + sysdate.
- Mapping causali KO → codici motivazione.
- Audit chiusura task + sincronizzazione.

**Dipendenze**: Sprint 5.

---

## Sprint 7 – Supervisione: Riassegna Attività

**Obiettivo**: il supervisore sposta task tra gruppo e utenti.

**Vertical slice**: login Supervisore → tab Riassegna Attività → seleziona task → riassegna a Gruppo o Utente → operatore destinatario vede il task.

**Contenuti**:
- Tab "Riassegna Attività" (lista task processi competenza).
- Filtri: Pratica N°, Data Assegnazione, Owner, Assegnatario.
- Azione riassegna a Gruppo Operatore ANC / Utente specifico.
- Autorizzazione: solo SUPERVISORE.
- Audit riassegnazione.

**Dipendenze**: Sprint 3.

---

## Sprint 8 – Home Supervisore: Contatori e Istogrammi

**Obiettivo**: dashboard di monitoraggio in baseline.

**Vertical slice**: login Supervisore → Home → contatori real-time + 3 istogrammi mese selezionabile.

**Contenuti**:
- Contatori: Attività, Pratiche Attive, Pratiche Chiuse.
- Istogrammi: Pratiche Giornaliere, Pratiche Giornaliere Lavorate (OK/KO), Pratiche per Stato.
- Selettore mese (calendario).
- Endpoint aggregati.

**Dipendenze**: Sprint 6.

---

## Sprint 9 – Dashboard Segnalazioni (Sinergia stub)

**Obiettivo**: modulo segnalazioni operativo con stub Sinergia.

**Vertical slice**: pratica → invio segnalazione → vista "Le Mie Segnalazioni" / globale → riassegna → stub Sinergia "apre ticket".

**Contenuti**:
- Entità Segnalazione + stati (In Coda, In Lavorazione, Chiuso).
- Invio segnalazione da contesto pratica.
- Viste con filtri (ID, stato, operatore, range temporale).
- Riassegna segnalazione (operatore/gruppo/sé).
- Stub Sinergia: apertura ticket, recupero pratiche/dettaglio.

**Dipendenze**: Sprint 7.

---

## Sprint 10 – Hardening & Polish

**Obiettivo**: rifinire UX accessoria, completare audit, irrobustire la POC.

**Vertical slice**: utente esperienza completa con help, link favoriti, export Excel.

**Contenuti**:
- Help in linea checklist ("Mostra Descrizione") con testi da manuale.
- Espandi/Comprimi sezioni UI.
- Link Favoriti (CRUD).
- Export Excel lista pratiche.
- Audit trail consolidato + Tab Azioni Correlate.
- Smoke-test E2E completo + checklist regressione.

**Dipendenze**: tutti.

---

## Vincoli sequenza

```
S0 → S1 → S2 → S3 → S4 → S5 → S6 ─┬─ S8 ─┐
                              └─ S7 ─┴─ S9 ─→ S10
```
