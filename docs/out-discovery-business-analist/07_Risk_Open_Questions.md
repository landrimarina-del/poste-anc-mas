# 07 - Risk & Open Questions

> Solo elementi realmente bloccanti o ambigui ai fini del porting (max 10).

## Risk

### R1 – Comportamento "Passed with defect" su chiusura task (ST46/ST47)
**Descrizione**: i Test Book riportano la chiusura pratica (Approvata e Respinta) come "Passed with defect". Non è chiaro se i difetti siano stati risolti o se rappresentino la baseline.
**Impatto**: replicare bug indesiderati o ignorare comportamenti reali del legacy.
**Mitigazione**: in Sprint 6, definire esplicitamente l'esito atteso "ideale" e segnalare al Coordinator se si individuano discrepanze rispetto al legacy.

### R2 – Mapping completo CODICE_DOC_ID → combinazioni allegati
**Descrizione**: il Discovery dichiara dominio {1,2,3} ma non descrive in modo esplicito a quale combinazione (solo Verbale / solo Carta / entrambi) corrisponda ciascun codice.
**Impatto**: errori in C1.2 e nella generazione del task corretto.
**Mitigazione**: assumere mapping inferito (1=Verbale, 2=Carta, 3=Entrambi) e validare con stakeholder.

### R3 – Mapping causali KO → codici motivazione BPM
**Descrizione**: il Discovery cita le causali (Intestazione/Firme/Timbro/Dichiarazione/Carta PI) ma non esplicita i codici inviati a BPM.
**Impatto**: invio esiti incompleto o errato verso BPM (C5.4/C5.5).
**Mitigazione**: chiedere accesso esplicito al mapping codici motivazione concordato con BPM nel pacchetto Interface Agreement BPM↔SD, oppure stipulare codici fittizi documentati.

### R4 – Comportamento checklist quando pratica contiene entrambi i documenti (CODICE_DOC_ID=3)
**Descrizione**: il vincolo V4 dice "ogni pratica un solo tipo da controllare", ma il cliente può aver caricato entrambi. Non è chiaro se l'operatore debba scegliere uno o se il sistema generi due task.
**Impatto**: rischio di processo non allineato al legacy.
**Mitigazione**: assumere "operatore sceglie uno solo" salvo conferma diversa.

### R5 – Logica calcolo esito su controlli facoltativi
**Descrizione**: il controllo "corrispondenza numero carta" è facoltativo. Se l'operatore lo lascia vuoto, conta come SI, NO o neutro nel calcolo?
**Impatto**: errori sull'esito (C5.1) per scenari borderline.
**Mitigazione**: assumere "neutro / non concorre", come scritto in AC-E6.03.

### R6 – Affidabilità del visualizzatore integrato (criticità tecnica nota)
**Descrizione**: il Discovery segnala criticità ricorrenti del viewer; il download manuale è il fallback ufficiale.
**Impatto**: la robustezza del visualizzatore degli allegati condiziona l'usabilità dell'istruttoria.
**Mitigazione**: in S4 selezionare una soluzione di visualizzazione robusta (scelta tecnologica a cura dell'Architect); test su file reali del cliente.

### R7 – Orchestrazione transitorio "In Attesa Conferma BPM" senza ack
**Descrizione**: se BPM non conferma mai (timeout), la pratica resta bloccata in stato transitorio.
**Impatto**: pratiche fantasma; nessun meccanismo di retry/escalation descritto nel Discovery.
**Mitigazione**: in POC implementare ack immediato dello stub; segnalare al Coordinator come gap potenziale.

## Open Questions

### Q1 – Ruolo Cliente in SD
È confermato che il Cliente NON interagisce con SD (solo Front End)? Documenti coerenti, ma vale formalizzare per scope.

### Q2 – Conservazione documentale a norma
La conservazione degli allegati (verbali, carte) è demandata a sistemi esterni (Data Lake?) o deve risiedere in SD? Discovery non chiarisce.

### Q3 – Multi-tenant / Più gruppi operativi
Esiste un solo "Gruppo Operatore ANC" o sono previsti più gruppi (per filiale/regione)? Impatto su modello dati e riassegnazione (C6.x).
