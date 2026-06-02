---
title: "La Fabbrica AI Lutech"
subtitle: "Liberare le applicazioni dal vendor lock-in"
author: "Lutech"
date: "2026"
---

# La Fabbrica AI Lutech

**Liberare le applicazioni dal vendor lock-in**

Come abbiamo automatizzato il porting da piattaforme proprietarie a stack open source enterprise-grade

*Lutech — 2026*

---

# Il problema: vendor lock-in

Molti clienti enterprise sono prigionieri di piattaforme proprietarie:

- **Licenze milionarie** (Appian, Pega, ServiceNow…)
- **Dipendenza totale** dal fornitore per ogni evoluzione
- **Competenze rare** e costose sul mercato
- **Impossibilità di sostituire** un componente senza riscrivere tutto

> *"Esiste un'alternativa enterprise-grade, open source, che non crei un nuovo lock-in?"*

---

# La risposta Lutech: un metodo, non un prodotto

Tre documenti fondativi — **IP Lutech**:

- **DOC1** — Architettura di Riferimento BPM Open Source
- **DOC2** — Catalogo Tecnologico (componenti valutati e selezionati)
- **DOC3** — Sizing e Costi

**Principio cardine: no vendor lock-in**
Ogni componente è sostituibile senza riscrivere l'applicazione.

Standard aperti: BPMN 2.0 · OpenAPI · OIDC · AsyncAPI

---

# Perché AI multi-agente (MAS)

Non un singolo LLM — un team di agenti specialistici coordinati.

| Caratteristica | Beneficio |
|---|---|
| **Specializzazione** | Ogni agente è esperto del suo dominio (BPM, DB, UX, codice) |
| **Parallelismo controllato** | Più agenti lavorano insieme, in parallelo |
| **Cross-validation** | Il Coordinator verifica la consistenza tra deliverable |
| **Output strutturati** | Documenti machine-readable passati automaticamente alla fase successiva |

**Risultato**: precisione, velocità e qualità verificabile.

---

# La Fabbrica AI: catena completa

```
DOC LUTECH + DOC CLIENTE          EXPORT APPIAN
        │                                │
        ▼                                ▼
┌─────────────────┐            ┌─────────────────┐
│  MAS DISCOVERY  │            │  MAS REVERSE    │
│  Coord+BA+Arch  │            │  Coord+5 spec.  │
│  +DBA+UX        │            │                 │
└─────────────────┘            └─────────────────┘
        │                                │
        ▼                                ▼
┌─────────────────┐            ┌─────────────────┐
│ MAS DEVELOP V1  │  ─────►    │  MAS GAP        │
│ 10 sprint       │            │  Confronta V1   │
│ Spring+Flowable │            │  vs Appian      │
└─────────────────┘            └─────────────────┘
                                        │
                                        ▼
                               ┌─────────────────┐
                               │ MAS DEVELOP V2  │
                               │ +6 sprint       │
                               └─────────────────┘
                                        │
                                        ▼
                               ┌─────────────────┐
                               │  MAS MIGRAZIONE │
                               │  V3: Kogito     │
                               └─────────────────┘
```

Ogni MAS ha un Coordinator che assegna, controlla e passa i deliverable al MAS successivo automaticamente.

---

# I numeri della fabbrica

| Metrica | Valore |
|---|---|
| MAS orchestrati | **4** (Discovery, Reverse, GAP, Develop) |
| Versioni del software prodotte | **3** (V1, V2, V3 Kogito) |
| Sprint completati | **16** (10 V1 + 6 V2) |
| Documenti analisi prodotti dal Reverse | **5** (funzionale, BPM, tecnico, DB, UX) |
| Capability mappate dal Business Analyst | **60+** |
| Oggetti Appian analizzati | **130+** |
| Process model BPM analizzati | **20** |
| Container Docker in produzione | **7** |

**Tutto generato da agenti AI, validato e tracciato.**

---

# Il caso reale: Poste Italiane — ANC

**Attivazione Nuova Carta** — oggi su Appian (proprietario)

**Input forniti dal cliente:**

- Backlog funzionale
- Manuale utente
- Test book
- Documenti di interfaccia BPM

**Output MAS Discovery + MAS Develop V1:**

- Architettura applicativa open source completa
- 10 sprint completati end-to-end
- Applicazione funzionante: Spring Boot 3 + Flowable + React + MariaDB

---

# Il Reverse Engineering: anche senza documenti

> **Cosa succede se i documenti del cliente non bastano — o non ci sono?**

Il **MAS Reverse Engineering** legge direttamente l'applicazione legacy.

**Input**: export XML applicazione Appian `BOA_ANC`
- 130+ oggetti analizzati
- 20 process model BPM
- 18 datatype business

**Output**: 5 documenti di analisi completi, generati automaticamente.

> *Questo è uno dei messaggi commerciali più forti: nei progetti enterprise reali, la documentazione aggiornata spesso non esiste. Il metodo funziona lo stesso.*

---

# Il GAP: confronto tra sviluppato e reale

Il **MAS GAP** trova le differenze tra la V1 costruita e l'applicazione Appian reale.

```
V1 software costruito (codice reale)
            VS
Documenti MAS Reverse (analisi Appian reale)
            │
            ▼
   Delta verificabile
   Non teorico, su codice reale
```

Coordinator + Backend + Frontend + QA

**Output**: documenti GAP con analisi di impatto, rischi, remediazione → input automatico al MAS Develop V2.

---

# V2: colmare i gap

**MAS Develop GAP** — sprint 11 → 16

- **6 sprint** aggiuntivi
- Colma le differenze tra V1 e comportamento reale Appian
- Stesso stack, nuovi task guidati dal GAP

**V2: applicazione allineata alla realtà legacy**

Stack: Spring Boot 3 + Flowable + React + MariaDB + MinIO

- **7 container** Docker
- Avviabile con `docker compose up`
- Demo end-to-end: apertura → lavorazione → checklist → chiusura → callback BPM

---

# V3: la prova della sostituibilità

**Migrazione Flowable → Kogito, fatta interamente da agenti.**

Due filosofie BPM a confronto:

| Aspetto | V1/V2 — Flowable | V3 — Kogito |
|---|---|---|
| Paradigma | **Workflow-centric** | **Domain-centric** |
| In parole semplici | Il processo *è* il software | Il software *è* il dominio (pratica, cliente). Il processo orchestra. |
| Cambio processo | = cambi il software | = aggiorni una regola |
| Stack | Spring Boot | Quarkus / Red Hat |

> *Il motore BPM è un componente intercambiabile. L'applicazione sopravvive alla sostituzione.*

---

# Architettura POC vs Target enterprise

Il DOC1 definisce **8 layer**. La POC ne attiva alcuni in modo **consapevolmente semplificato**.

| Layer | Target | POC |
|---|---|---|
| L1 — Presentation | React + Next.js | React SPA + Vite |
| L2 — BPM | Servizio dedicato | Embedded → Kogito V3 |
| L3 — Rules | Drools / KIE | Logica embedded |
| L4 — AI | LLM + agent | Non attivato |
| L5 — Document | ECM + OCR | MinIO S3-compatible |
| L6 — Integration | Sistemi reali | Stub container |
| L7 — Process Mining | PM4Py + dashboard | Non attivato |
| L8 — Infrastructure | K8s + APISIX + Keycloak | Docker Compose + NGINX + Basic Auth |

**Ogni semplificazione è reversibile senza riscrivere l'applicazione.**
Contratti, bounded context e workflow BPMN sono **invarianti**.

---

# Roadmap: il metodo si affina

Ogni iterazione produce agenti più precisi.

| Versione | Trasformazione | Metodo |
|---|---|---|
| V1 | Da documenti a software | MAS Discovery + Develop |
| V2 | Gap vs applicazione reale | MAS Reverse + GAP + Develop |
| V3 | Flowable → Kogito | Agenti specializzati BPM migration |
| **V4** | **Monolite → Microservizi Quarkus** | **Agenti chirurgici per decomposizione** |

**V4 — prossimo passo**

Stessa applicazione ANC, architettura a microservizi Quarkus.
Agenti addestrati per quella migrazione: **più precisi**, **più chirurgici**, con controlli embedded che prevengono gli errori tipici.

---

# Cosa risparmia il cliente

**Costi diretti:**
- Niente licenze BPM proprietarie (Appian Enterprise: centinaia di k€/anno)
- Niente vincoli di rinnovo o aumento listino

**Costi indiretti:**
- Indipendenza dal fornitore originale
- Competenze reperibili sul mercato (Java, React, BPMN — non skill proprietarie)
- Riduzione drastica del time-to-market per il porting

**Costi nascosti evitati:**
- Niente "re-platform" futuro: l'architettura cresce per evoluzione
- Componenti sostituibili senza riscrivere l'applicazione

---

# Demo live disponibile

**La POC è funzionante e dimostrabile.**

Stack completo avviabile localmente:

```
docker compose up -d
```

5 minuti per vedere il **ciclo end-to-end**:

1. Apertura pratica via stub BPM
2. Presa in carico operatore
3. Tipizzazione documento + viewer
4. Compilazione checklist
5. Calcolo esito (Approvata / Respinta)
6. Chiusura pratica + callback BPM

7 container, 1 comando, 0 dipendenze sul PC del cliente (solo Docker).

---

# Il vero IP Lutech

**Non il software — il metodo.**

```
   Problema cliente
         │
         ▼
   ┌─────────────────┐
   │ FABBRICA AI     │
   │   Lutech        │
   ├─────────────────┤
   │ • Analisi       │
   │ • Gap Analysis  │
   │ • Sviluppo      │
   │ • Migrazione    │
   └─────────────────┘
         │
         ▼
   Piattaforma open source
   • enterprise-grade
   • vendor-agnostic
   • evolutiva
```

- Replicabile su qualsiasi applicazione proprietaria
- Applicabile a qualsiasi stack open source target
- **La fabbrica impara**: ogni migrazione affina gli agenti per la successiva

---

# Liberiamo la tua applicazione

> *Abbiamo costruito una fabbrica AI che porta applicazioni da piattaforme proprietarie a open source in modo sistematico.*
>
> *Abbiamo dimostrato che la piattaforma è davvero vendor-agnostic — cambiando il motore BPM con agenti AI.*
>
> *Il prossimo passo è la migrazione a microservizi Quarkus, con agenti specializzati e chirurgici.*

**Qualsiasi componente è sostituibile.**
**La sostituzione è automatizzabile.**
**Ogni iterazione rende il metodo più preciso.**

---

**Vuoi liberare la tua applicazione dal lock-in?**
**Parliamone.**
