# Presentazione Business — Fabbrica AI per la Migrazione Open Source
### Lutech — IP Proprietario

---

## SLIDE 1 — Titolo

**La Fabbrica AI che libera le applicazioni dal vendor lock-in**

*Come abbiamo automatizzato il porting da piattaforme proprietarie a open source enterprise-grade*

Lutech — 2026

---

## SLIDE 2 — Il problema: vendor lock-in

**Molti clienti enterprise sono prigionieri di piattaforme proprietarie**

- Licenze milionarie (Appian, Pega, ServiceNow…)
- Dipendenza totale dal fornitore per evoluzioni
- Competenze specifiche difficili da reperire sul mercato
- Impossibilità di sostituire un componente senza riscrivere tutto

**La domanda del mercato:**
> *"Esiste un'alternativa enterprise-grade, open source, che non crei un nuovo lock-in?"*

---

## SLIDE 3 — La risposta Lutech: un metodo, non un prodotto

**Lutech ha costruito un metodo replicabile basato su AI multi-agente (MAS)**

Tre documenti fondativi — IP Lutech:
- **DOC1** — Architettura di Riferimento BPM Open Source
- **DOC2** — Catalogo Tecnologico (componenti valutati, scelti, scartati)
- **DOC3** — Sizing e Costi

Principio cardine: **no vendor lock-in** — ogni componente è sostituibile senza riscrivere l'applicazione.

Standard aperti: BPMN 2.0 · OpenAPI · OIDC · AsyncAPI

---

## SLIDE 4 — La Fabbrica AI: architettura del metodo

```
DOCUMENTI CLIENTE          DOCUMENTI LUTECH
(backlog, manuale,    +    (Architettura, Catalogo,
 test book, interfacce)     Sizing)
         │
         ▼
  ┌─────────────────┐
  │  MAS DISCOVERY  │  Coordinator + BA + Architect + DBA + UX
  └─────────────────┘
         │ deliverable automatici
         ▼
  ┌─────────────────┐
  │  MAS DEVELOP    │  Coordinator + Backend + Frontend + QA
  │  V1 — 10 sprint │  (Spring Boot + Flowable)
  └─────────────────┘
```

**Il MAS**: sistema di agenti AI specialistici coordinati in parallelo.
Ogni Coordinator assegna task, controlla consistenza, consolida output, passa i deliverable al MAS successivo — automaticamente.

---

## SLIDE 5 — Il caso reale: Poste Italiane — ANC

**Attivazione Nuova Carta (ANC)** — oggi su Appian (piattaforma proprietaria)

Documenti di input forniti:
- Backlog funzionale
- Manuale utente
- Test book
- Documenti di interfaccia BPM

**Risultato MAS Discovery + MAS Develop V1:**
- Architettura applicativa open source definita
- 10 sprint completati
- Applicazione funzionante: Spring Boot 3 + Flowable + React + MariaDB

---

## SLIDE 6 — Il Reverse Engineering: leggere la realtà

**Cosa succede se i documenti non bastano — o non ci sono?**

**MAS Reverse Engineering** — legge direttamente l'applicazione Appian

Input: export XML dell'applicazione `BOA_ANC`
- 130+ oggetti
- 20 process model BPM
- 18 datatype business
- 2 datastores JPA

Output: 5 documenti di analisi completi
- Funzionale · BPM · Tecnico · Schema DB · UX/UI

> *Il metodo funziona anche senza documenti: si parte dall'applicazione reale.*

---

## SLIDE 7 — Il GAP: confronto tra sviluppato e reale

**MAS GAP** — trova le differenze tra V1 e la realtà Appian

```
V1 software costruito (sprint 0-10)
         VS
Documenti MAS Reverse (analisi Appian reale)
         │
         ▼
Delta verificabile: non teorico, su codice reale
```

Coordinator + Backend + Frontend + QA

Output: documenti GAP con analisi di impatto, rischi, remediazione

→ Input automatico al **MAS Develop GAP**

---

## SLIDE 8 — V2: colmare i gap

**MAS Develop GAP** — sprint 11→16

- 6 sprint aggiuntivi
- Colma le differenze tra V1 e comportamento reale Appian
- Stessa architettura, stessi agenti, nuovi task guidati dal GAP

**V2: applicazione allineata alla realtà legacy**

Stack invariato: Spring Boot 3 + Flowable + React + MariaDB + MinIO
- 7 container Docker
- Avviabile con `docker compose up`
- Demo end-to-end: apertura pratica → lavorazione → checklist → chiusura → callback BPM

---

## SLIDE 9 — V3: la prova della sostituibilità

**Migrazione Flowable → Kogito — fatta interamente da agenti**

Due filosofie BPM a confronto:

| | Flowable | Kogito |
|---|---|---|
| Paradigma | **Workflow-centric** | **Domain-centric** |
| Chi governa | Il processo governa il sistema | Il dominio governa, il processo supporta |
| Stack | Spring Boot | Quarkus / Red Hat |

Agenti istruiti specificamente per questa migrazione.

**Il risultato dimostra il principio:**
> *Il motore BPM è un componente intercambiabile. L'applicazione sopravvive alla sostituzione.*

---

## SLIDE 10 — Architettura POC vs Architettura Target

**Il DOC1 definisce 8 layer. La POC ne attiva 3 pienamente, 3 parzialmente, 2 non attivati — per scelta consapevole.**

### Layer architetturali — stato in POC

| Layer | Target enterprise | POC | Motivazione semplificazione |
|---|---|---|---|
| **L1 — Presentation** | React + Next.js (SSR) | React SPA statica + Vite | SSR non richiesto per UI interna BO |
| **L2 — BPM** | Flowable 7 servizio dedicato | Flowable 7 **embedded** nel monolite → Kogito V3 | Riduce container; contratti BPM invariati |
| **L3 — Rules** | Drools / KIE Rule Engine | ❌ Non attivato — logica embedded nel codice | Regole semplici, non esternalizzare sotto soglia |
| **L4 — AI** | LLM inference + agent orchestration | ❌ Non attivato | Nessun caso d'uso concreto in scope POC |
| **L5 — Document** | ECM enterprise (Alfresco) + OCR | MinIO single-node (S3-compatible) | Allegati semplici, no conservazione a norma |
| **L6 — Integration** | BPM Poste reale + Sinergia + Data Lake | Stub container (mock REST) | Sistemi non disponibili localmente |
| **L7 — Process Mining** | PM4Py + dashboard KPI | ❌ Non attivato | FUTURE_ENTERPRISE; eventi già emessi |
| **L8 — Infrastructure** | Kubernetes + APISIX + Keycloak + Observability | Docker Compose + NGINX + Basic Auth + log stdout | POC locale single-node |

### Componenti sostituiti (20 sostituzioni documentate)

| Slot target | POC | Impatto evolutivo |
|---|---|---|
| Kubernetes | Docker Compose | Aggiungere manifest K8s — immagini già pronte |
| Apache APISIX (API Gateway) | NGINX reverse proxy | Sostituzione routing — contratti API invariati |
| Keycloak + AD/Entra (IAM) | Basic Auth + tabella utenti DB | Cambia solo filtro sicurezza — `Principal` già astratto |
| MariaDB cluster HA | MariaDB single-node | Configurazione HA — schema applicativo invariato |
| RabbitMQ / Kafka (broker eventi) | Pattern outbox in-process | Aggiungere broker — produttori già scritti |
| Valkey/Redis (cache distribuita) | Cache in-process (Caffeine) | Interfaccia repository invariata |
| OpenTelemetry stack completo | Log JSON stdout + Actuator | Aggiungere agent OTel — instrumentazione già presente |
| Pipeline CI/CD + GitOps | Build locale docker compose | Dockerfile e immagini riutilizzabili |

### Principi DOC1 — rispetto in POC

| Principio | Stato |
|---|---|
| **P1 — No vendor lock-in** | ✅ Tutte le tecnologie OSI-approved, nessuna API cloud proprietaria |
| **P2 — Cloud-native** | ✅ parziale — container come unità di deploy; K8s è solo configurazione |
| **P3 — Security Zero Trust** | ⚠️ Semplificato — Basic Auth con fallback dichiarato; Keycloak in target |
| **P4 — API-first** | ✅ parziale — contratti REST OpenAPI; Interface Agreement BPM↔SD invariato |
| **P5 — Observability** | ⚠️ Semplificato — log strutturati; stack centralizzato in target |
| **P6 — AI-ready** | ⏭️ Predisposto, non attivato — slot architetturale riservato |
| **P8 — Scalabilità progressiva** | ✅ Applicato — monolite modulare → few-services → microservizi (roadmap V4) |
| **P9 — DevOps** | ⚠️ Semplificato — struttura repository pronta per pipeline CI/CD |

> **La POC copre lo Step 2 del percorso di adozione DOC1** (piattaforma base + BPM + storage documentale base).
> Ogni semplificazione è reversibile senza riscrittura applicativa — i contratti, i bounded context e il workflow BPMN sono invarianti.

---

## SLIDE 10 — La roadmap: il metodo si affina

**Ogni iterazione produce agenti più precisi**

| Versione | Trasformazione | Metodo |
|---|---|---|
| V1 | Da documenti a software | MAS Discovery + MAS Develop |
| V2 | Gap vs applicazione reale | MAS Reverse + MAS GAP + MAS Develop |
| V3 | Flowable → Kogito | Agenti specializzati migrazione BPM |
| **V4** | **Monolite → Microservizi Quarkus** | Agenti chirurgici per decomposizione |

**V4 — prossimo passo:**
Stessa applicazione ANC, stessa logica di business, architettura a microservizi Quarkus.
Agenti addestrati per quella migrazione specifica: più precisi, con controlli embedded che prevengono gli errori tipici della decomposizione.

---

## SLIDE 11 — Il vero IP Lutech

**Non il software — il metodo**

```
Problema cliente
      │
      ▼
Fabbrica AI Lutech
  ├── Analisi (Discovery o Reverse)
  ├── Gap Analysis
  ├── Sviluppo controllato
  └── Migrazione chirurgica
      │
      ▼
Piattaforma open source
enterprise-grade
vendor-agnostic
evolutiva
```

- Replicabile su qualsiasi applicazione proprietaria
- Applicabile a qualsiasi stack open source target
- La fabbrica impara: ogni migrazione affina gli agenti per la successiva

---

## SLIDE 12 — Messaggio finale

> *"Abbiamo costruito una fabbrica AI che porta applicazioni da piattaforme proprietarie a open source in modo sistematico.*
>
> *Abbiamo dimostrato che la piattaforma open source è davvero vendor-agnostic cambiando il motore BPM — da Flowable a Kogito, da workflow-centric a domain-centric — con agenti AI.*
>
> *Il passo successivo è la migrazione da monolite a microservizi Quarkus: stesso metodo, agenti addestrati specificamente, più precisi e chirurgici.*
>
> *Qualsiasi componente è sostituibile. La sostituzione è automatizzabile. E ogni iterazione rende il metodo più preciso."*

---

## NOTE SPEAKER

### Dati quantitativi disponibili
- **16 sprint** completati (V1 + V2)
- **7 container Docker** in produzione
- **4 MAS** orchestrati (Discovery, Reverse, GAP, Develop)
- **5 documenti** prodotti dal MAS Reverse (funzionale, BPM, tecnico, DB, UX/UI)
- **60+ capability** mappate dal Business Analyst
- **130+ oggetti** Appian analizzati dal Reverse
- **20 process model** BPM analizzati
- **3 versioni** del software prodotte

### Demo disponibile
Stack completo avviabile localmente con `docker compose up`.
Ciclo end-to-end ANC dimostrabile: apertura pratica → lavorazione operatore → checklist → chiusura → callback BPM.
