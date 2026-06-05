**Vincoli Architetturali Emersi**

**1. Vincoli su Data Layer**

- **Database target vincolato a MariaDB**

  - Motivazione: già presente nel contesto Poste

  - Deve essere mantenuto per evitare complessità di porting

  - Deve comunque:

    - usare SQL standard

    - essere compatibile con altri DB

**2. Gestione Documentale / Storage**

- Necessità di supporto per **object storage flessibile e
  intercambiabile**

- Possibili opzioni:

  - S3-compatible

  - file system

  - storage proprietari/enterprise

- Requisito chiave:

  - componente **pluggable / sostituibile**

**3. Linguaggi e Stack Tecnologico**

- **Backend: Java / OpenJDK come riferimento principale**

  - evitando dipendenze Oracle

- Possibilità di:

  - microservizi eterogenei (es. anche .NET)

  - ma evitare eccessiva eterogeneità

**4. Architettura Applicativa**

- Architettura orientata a:

  - **microservizi**

  - containerizzazione

  - deployment su Docker / Kubernetes

- Necessità di:

  - ambiente locale "light" per sviluppo

  - ambiente centralizzato per demo runtime

**5. Runtime & Ambienti**

- Separazione chiara tra:

  - ambiente di sviluppo locale

  - runtime condiviso (solo per integrazione/demo)

- Limite:

  - runtime condiviso non può essere usato in parallelo da più
    sviluppatori

- Necessità di:

  - repository centralizzato per artefatti e configurazioni

**6. Identity & Access Management**

- Integrazione obbligatoria con:

  - **Active Directory / sistemi IAM enterprise (es. Entra ID, Oracle
    IAM)**

- Necessità di:

  - fallback base auth per test

- Modello di autorizzazione:

  - **RBAC basato su gruppi gerarchici (stile Appian)**

**7. Sicurezza e API**

- API devono supportare:

  - autenticazione:

    - OAuth 2.0

    - Basic (fallback)

  - mutual authentication

- Possibile utilizzo API Gateway:

  - Kong

  - Apache APISIX (provato ma con criticità)

- Tutte le integrazioni devono essere:

  - esposte secondo standard enterprise

**8. Sicurezza Trasporto**

- Tutti i canali di integrazione devono supportare:

  - **TLS (versione indicata: TLS 2.x)**

**9. Integrazione con Sistemi Esterni**

- Obbligo di supporto a:

  - REST API

  - SFTP

  - code/messaging (MQ)

- Supporto per:

  - autenticazione mutual TLS

**10. Gestione Eventi / Processi**

- Process orchestration:

  - possibilità di trigger:

    - timer-based

    - eventi

- Processi modellati come:

  - grafi (nodi/archi)

**11. Front-end**

- Preferenza:

  - **React**

**12. Componenti Minimi "Punti Fermi"**

Durante la riunione vengono identificati esplicitamente alcuni "punti
fermi":

- Database → MariaDB

- Storage → object storage pluggable

- Backend → Java

- Frontend → React

- IAM → integrazione AD / gruppi

- Security → OAuth2 / TLS

**13. Requisiti Enterprise**

- Compatibilità con:

  - contesti **large enterprise (Poste Italiane)**

- Necessità di:

  - scalabilità

  - standard enterprise

  - conformità a regolamenti (es. contesto bancario)

**14. Vincoli su Framework e Scelte Tecnologiche**

- Necessità di:

  - evitare framework "giovani" o instabili

  - privilegiare:

    - open source maturi

    - soluzioni già utilizzate internamente

- Richiesta esplicita:

  - documentare motivazioni di scelta/scarto

**15. AI & Agent Architecture (Vincolo di metodo architetturale)**

- Architettura basata su:

  - agenti specializzati:

    - analisi

    - architettura

    - sviluppo

    - testing

- Input:

  - documentazione funzionale + export tecnico

- Output:

  - codice + documentazione

**📌 Sintesi Executive (per slide)**

  -------------------------------------------
  **Categoria**    **Vincolo principale**
  ---------------- --------------------------
  Data             MariaDB (compatibilità
                   cross-DB)

  Storage          Object storage pluggable

  Backend          Java / OpenJDK

  Frontend         React

  Architettura     Microservizi + container

  Runtime          Dev locale + runtime
                   centralizzato

  IAM              AD / RBAC a gruppi

  API              OAuth2 + API Gateway

  Security         TLS obbligatorio

  Integrazione     REST, SFTP, MQ

  Orchestrazione   Event/timer based

  Enterprise       Scalabilità + compliance

  Framework        Open source maturi
  -------------------------------------------

**👉 Osservazione utile (operativa)**

La discussione evidenzia chiaramente che:

- i "vincoli" non sono ancora formalizzati in un documento unico

- ma devono diventare rapidamente: 👉 **una baseline architetturale
  condivisa su cui addestrare gli agenti**

Se vuoi, posso fare il passo successivo per te: ✅ trasformare questi
vincoli in

- un **documento architetturale formale (Word/Slide)**

- oppure una **checklist di validazione target (per selezione
  framework)**
