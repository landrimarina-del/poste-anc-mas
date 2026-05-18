# 06 - Dipendenze Funzionali

## 1. Dipendenze tra Capability

| Capability | Dipende da | Note |
|---|---|---|
| C2.1 Creazione pratica | C1.1, C1.2, C1.3, C1.4 | nessuna creazione senza validazione e idempotenza |
| C3.1 Generazione task | C2.1 | il task nasce contestualmente alla pratica |
| C3.3 Presa in carico | C3.1, C3.2 | il task deve esistere ed essere visibile |
| C4.3 Tipizzazione | C3.3, C4.1 | richiede presa in carico + viewer disponibile |
| C4.4 Checklist Verbale | C4.3 (con tipo=Verbale) | abilitata solo se tipizzazione = Verbale |
| C4.5 Checklist Carta | C4.3 (con tipo=Carta) | abilitata solo se tipizzazione = Carta |
| C4.6 Causali KO | C4.4 | obbligatorie se idoneità formale = NO |
| C4.8 Salva e Prosegui | C4.4 / C4.5 | prerequisito per accedere al Riepilogo |
| C5.1 Calcolo esito | C4.8 | calcolato sui controlli salvati |
| C5.3 Chiusura task | C5.1 | possibile solo dopo esito calcolato |
| C5.4 / C5.5 Invio esito BPM | C5.3 | scattano dopo CHIUDI PRATICA |
| C5.6 Sincronizzazione finale | C5.4 / C5.5 + C5.7 | richiede ack stub BPM |
| C6.1–C6.4 Riassegnazione | C3.1, C9.3 | richiede task esistenti + ruolo SUPERVISORE |
| C6.5–C6.8 Dashboard Supervisore | C2.1, C5.6 | dati aggregati su pratiche/stati |
| C7.x Segnalazioni | C2.1 | segnalazione legata a pratica esistente |

## 2. Workflow correlati

### Workflow principale (happy path Operatore)
```
BPM → Apertura Pratica (C1) → Pratica Aperta (C2.1) → Task generato (C3.1)
    → Operatore vede coda (C3.2) → ACCETTA (C3.3) → In Lavorazione
    → Visualizza allegato (C4.1) → Tipizza (C4.3)
    → Compila checklist (C4.4 / C4.5) → Salva e Prosegui (C4.8)
    → Esito calcolato (C5.1) → CHIUDI PRATICA (C5.3) → In Attesa Conferma BPM
    → Invio esito a BPM (C5.4 / C5.5) → ACK BPM → Chiusa OK / KO (C5.6)
```

### Workflow alternativo (Supervisore – ribilanciamento)
```
Supervisore monitora Home (C6.5–C6.8)
    → Identifica colli di bottiglia (es. Pratiche per Stato)
    → Apre Riassegna Attività (C6.1)
    → Filtra (C6.4) → Riassegna a Gruppo (C6.2) o Utente (C6.3)
    → Operatore destinatario vede il task (C3.2)
```

### Workflow eccezioni (Segnalazioni)
```
Operatore/Supervisore identifica anomalia su pratica
    → Invia segnalazione (C7.1) → Sinergia stub
    → Supervisore monitora vista globale (C7.3)
    → Eventuale riassegnazione (C7.4)
```

### Workflow di errore tecnico (viewer KO)
```
Operatore apre allegato → viewer fallisce
    → Box informativo (C4.12)
    → Download manuale (C4.2)
    → Operatore decide: tipizza e prosegue OPPURE chiude KO con causale appropriata
```

## 3. Prerequisiti tecnici per gli sprint

| Sprint | Prerequisiti |
|---|---|
| S0 | nessuno |
| S1 | infra DB + BE pronti, schema utenti (S0) |
| S2 | dati pratica disponibili (S1) |
| S3 | pratiche persistite (S1, S2) |
| S4 | task accettabile + storage allegati (S1, S3) |
| S5 | tipizzazione confermata (S4) |
| S6 | checklist + esito (S5) + endpoint stub BPM bidirezionale |
| S7 | task assegnati e ruolo SUPERVISORE attivo (S0, S3) |
| S8 | popolazione di pratiche in vari stati (S6) |
| S9 | pratiche persistite (S1) |
| S10 | tutti gli sprint funzionali |

## 4. Dipendenze esterne (stub in POC)

> **Nota**: Appian non figura in questa tabella. Appian è la **piattaforma legacy** che ospita oggi la Scrivania Digitale ANC: è la sorgente del porting, non un sistema esterno con cui integrarsi. La generazione di "Pratica N°" (oggi a carico della runtime Appian) viene **internalizzata** nell'applicazione open source.

| Sistema esterno | Capability impattate | Modalità POC |
|---|---|---|
| BPM | C1.1, C5.4, C5.5, C5.6 | Stub funzionale (driver di test) conforme al contratto Interface Agreement BPM↔SD (`InterfaceAgreement.md` + `BPM_SubmitWorkItem_v1.1.yaml`) |
| Sinergia (PIX) | C7.x | Stub locale (apertura ticket simulata) |
| Data Lake | C9.4 | Non implementato (FUTURE_ENTERPRISE) |
| Front End cliente | upstream BPM | Non in scope (BPM riceve allegati) |

## 5. Dipendenze sui dati di riferimento

- Catalogo **causali KO formali**: Intestazione, Firme, Timbro, Dichiarazione, Carta Poste Italiane.
- Catalogo **stati pratica**: Aperta, In Lavorazione, In Attesa Conferma BPM, Chiusa OK, Chiusa KO.
- Catalogo **CODICE_DOC_ID**: 1, 2, 3 (mappati a combinazioni Verbale/Carta come da test ST01-ST03).
- Catalogo **resultCode**: 0 (OK), -4 (messaggio in ingresso non valido), -5 (idempotenza).
- Mapping **causali KO → codici motivazione** verso BPM (da Interface Agreement BPM↔SD).

## 6. Vincoli cross-funzionali

- **VC-1**: la riassegnazione (C6.x) NON deve alterare lo stato della pratica, solo l'owner del task.
- **VC-2**: il calcolo esito (C5.1) è funzione pura della checklist salvata; nessun override manuale.
- **VC-3**: ogni cambiamento di stato deve produrre voce in tab Stati (C2.8) e Cronologia (C2.7).
- **VC-4**: lo stato "Chiusa OK/KO" è raggiungibile **solo** via callback BPM (C5.6); CHIUDI PRATICA da solo lascia "In Attesa Conferma BPM".
- **VC-5**: la presa in carico è prerequisito per qualsiasi azione di lavorazione sulla pratica.
- **VC-6**: la tipizzazione è atomica e irreversibile per la durata della pratica.
