# GAP-BLOCKER-001 — Modalità Trasporto Documento BPM

SEVERITÀ
BLOCKER

DESCRIZIONE

Dall’analisi del Product Backlog ANC, del manuale utente e del testbook emerge che:

- il sistema SD deve ricevere e rendere disponibile all’operatore BO l’immagine/documento associato alla pratica
- l’operatore deve poter:
  - visualizzare il documento
  - effettuare download
  - effettuare verifiche visuali

L’attuale Sprint 4 implementa come vincolo architetturale certo:
- document transport esclusivamente tramite metadata + URL

Tale comportamento NON risulta esplicitamente confermato nei documenti funzionali disponibili.

RISCHIO

Lo Sprint 4 potrebbe implementare:
- modello dati errato
- API BPM errate
- retrieval documentale errato
- gestione allegati incompatibile con il runtime reale

IMPATTO

- Architect
- DBA
- Backend
- Flowable integration
- UX document viewer

AZIONE RICHIESTA

Eseguire impact analysis immediata limitatamente a:
- workflow documentale
- BPM intake
- modello dati allegati
- retrieval strategy

OUTPUT RICHIESTI

- delta architetturale
- delta modello dati
- delta BPM integration
- eventuale redesign Sprint 4

REGOLE

NON modificare:
- baseline roadmap
- sprint precedenti
- requisiti funzionali consolidati