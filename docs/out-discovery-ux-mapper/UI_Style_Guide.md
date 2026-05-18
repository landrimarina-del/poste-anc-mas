# UI Style Guide — Scrivania Digitale ANC

Style guide ricostruita dagli screenshot di riferimento (`docs/requirements/ui-reference/`) e dalle descrizioni dei manuali utente Operatore e Supervisore. Scopo: fornire al MAS di sviluppo frontend il sistema visuale percepito della soluzione legacy, da preservare senza alterazioni creative.

Lingua: italiano. Naming applicativo preservato.

---

## 1. Identità visiva complessiva

L'interfaccia si presenta come **case management enterprise** di taglio sobrio, orientato alla densità informativa e alla guida del workflow. La percezione complessiva è:

- Schema **chiaro su sfondo bianco**, con accenti **gialli** (header del processo) e **blu** (azioni, linee di avanzamento, link).
- Tipografia di sistema sans-serif a buona leggibilità, dimensioni contenute per massimizzare la densità tabellare.
- Layout a colonne con larghezza variabile, intestazioni di sezione marcate, tabelle a piena larghezza.
- Forte presenza di **card** rettangolari con bordi sottili, separazione netta tra aree informative.

---

## 2. Colori

### 2.1 Palette funzionale (derivata dagli screenshot)

| Ruolo | Colore percepito | Uso |
|---|---|---|
| Brand / Header processo | Giallo Poste (giallo saturo) | Striscia "griglia gialla" che ospita la linea di avanzamento e i dati identificativi pratica |
| Azione primaria / Link | Blu intenso | Pulsanti primari, link in tabella, frecce di ordinamento e paginazione, barra orizzontale della linea di avanzamento |
| Azione secondaria / Outline | Blu su sfondo bianco con bordo | Pulsanti secondari (es. MODIFICA, INDIETRO) |
| Esito positivo (APPROVATA) | Verde | Card APPROVATA in milestone Riepilogo, badge "Chiusa OK", segmenti grafici "Chiusa OK" |
| Esito negativo (RESPINTA) | Rosso | Card RESPINTA in milestone Riepilogo, badge "Chiusa KO", segmenti grafici "Chiusa KO" |
| Sfondo applicazione | Bianco | Sfondo principale |
| Sfondo sezione / Card | Bianco con bordo grigio chiaro | Sezioni informative, tabelle, form |
| Header tabella | Grigio chiaro | Righe di testata delle tabelle |
| Testo primario | Grigio scuro / nero | Testo standard |
| Testo secondario / etichette | Grigio medio | Label di form, descrizioni |
| Stato neutro / Disabled | Grigio chiaro | Pulsanti disabilitati, colonne non attive (es. Conformità prima della selezione presenza documento) |

### 2.2 Note di applicazione

- Il **giallo brand** è riservato alla fascia di intestazione del processo (linea di avanzamento). Non usato come fondo di altre aree.
- Il **blu** è il colore primario di interazione: pulsanti CTA, link cliccabili in tabella, frecce di ordinamento (su/giù), simboli di paginazione `<`, `>`, `<<`, `>>`.
- Verde e rosso sono usati **solo** per veicolare esito (semantica), mai per UI generica.
- Le barre espandibili (es. "Indirizzo di Residenza") riprendono il blu come banda di intestazione della sottosezione.

---

## 3. Tipografia

- Font sans-serif di sistema (impressione: web-safe, simile a Roboto/Open Sans/Segoe UI).
- Gerarchia tipografica:
  - **Titolo schermata / Tab attivo**: peso semibold, dimensione maggiore.
  - **Etichette di sezione (es. "Dati Cliente", "Controllo Verbale di denuncia")**: bold, dimensione media.
  - **Etichette di campo (label)**: peso regular, dimensione contenuta.
  - **Valori**: peso regular, leggermente più scuri delle label per contrasto.
  - **Testo tabellare**: dimensione compatta, sufficiente densità.
  - **Pulsanti**: testo in **MAIUSCOLO** (ACCETTA, CONFERMA, SALVA E PROSEGUI, MODIFICA, CHIUDI PRATICA, ESCI, AGGIORNA, CANCELLA FILTRI, APPLICA FILTRI, NASCONDI SEZIONE, NASCONDI ALLEGATI).
- Allineamento: testo prevalentemente allineato a sinistra; numeri e date a sinistra in tabella (non a destra).

---

## 4. Spacing e griglia

- **Layout a due colonne** nelle schermate di lavorazione/tipizzazione: colonna sinistra per dati/checklist, colonna destra per preview documento. Proporzione tipica: ~60/40 o ~55/45.
- **Padding interno card**: medio (~16–20px percepiti), sufficiente per separare contenuto da bordo.
- **Gap tra sezioni**: marcato, con separatori orizzontali o spazio bianco.
- **Tabelle a piena larghezza** del contenitore, con padding cella moderato per ottimizzare densità.
- **Barra dei pulsanti azione**: posizionata in fondo alla schermata o in fondo alla sezione corrente, allineata a destra o centrata in base al contesto.

---

## 5. Header applicativo

Layout consistente in tutte le schermate (Operatore e Supervisore):

- **Sinistra**: logo / brand + nome applicazione.
- **Centro-sinistra**: tab di navigazione orizzontali (Home, Attività/Pratiche/Riassegna Attività in base al profilo). Il tab attivo è evidenziato (sottolineatura blu o cambio colore).
- **Destra**: blocco contatori real-time (tre numeri con etichetta: Attività, Pratiche Attive, Pratiche Chiuse). Stile a "pillole" o "badge" rettangolari con numero grande e label sottostante.

Non è prevista una sidebar di navigazione verticale persistente: la navigazione primaria avviene via tab.

---

## 6. Card e contenitori

### 6.1 Card informazioni processo

Card centrale sotto la linea di avanzamento, sfondo bianco, bordo sottile, contiene:
- Nome processo (es. "Attivazione Nuova Carta")
- Codice Fiscale Cliente
- Pratica N.

Stile sobrio, testo allineato a sinistra, etichetta e valore su due righe o coppia inline.

### 6.2 Card esito (Riepilogo)

Due card grandi, rettangolari, affiancate:
- **APPROVATA**: dominante verde quando attiva; grigia/spenta quando inattiva.
- **RESPINTA**: dominante rossa quando attiva; grigia/spenta quando inattiva.

Comportamento "spotlight": solo una delle due risulta visivamente "illuminata" in base all'esito calcolato. Sola lettura.

### 6.3 Card grafici (Supervisore)

Tre contenitori card sulla Home Supervisore, uno per istogramma:
- Pratiche Giornaliere
- Pratiche Giornaliere Lavorate (segmentato per esito)
- Pratiche per Stato

Ogni card ha titolo in alto, selettore periodo (calendario + frecce `←` / `→`), area grafico, tooltip su hover.

### 6.4 Sezioni dati (Dati Cliente, Dati Carta Bloccata, Contenuti Documento)

Card a piena larghezza con titolo bold, contenuto in due colonne di campi label/valore. Tasti "NASCONDI SEZIONE" e "NASCONDI ALLEGATI" per collassare. Barre blu espandibili per sottosezioni (es. "Indirizzo di Residenza").

---

## 7. Tabelle

### 7.1 Stile tabellare

- Header riga: sfondo grigio chiaro, testo bold, testo allineato a sinistra.
- Righe alternate: opzionale (zebra striping leggera) — gli screenshot suggeriscono righe a sfondo bianco con bordi orizzontali sottili.
- Righe selezionabili / hover: leggero highlight su passaggio mouse.
- **Link in tabella**: testo blu sottolineato (o blu standard) sulle colonne cliccabili (es. "Pratica N.", "Attività", "Attività Segnalazione").
- **Indicatori di ordinamento**: frecce blu su/giù affianco alla testata della colonna ordinabile attualmente attiva.

### 7.2 Componenti integrati nelle tabelle

- **Checkbox di selezione riga** (Riassegna Attività).
- **Icona occhio** per anteprima documento allegato.
- **Icona obbligatorio / non obbligatorio** nella colonna "Richiesto" della checklist (a due stati).
- **Icona "Mostra Descrizione"** per ogni riga di checklist.
- **Badge stato** nella colonna Stato (es. "Aperta", "In Lavorazione", "Chiusa OK", "Chiusa KO"). Colorazione coerente con la semantica esito quando applicabile.

### 7.3 Filtri e azioni tabella

Posizionati **sopra la tabella**, in un blocco a sé:
- Riga di campi filtro (input testuali, dropdown, date picker da/a).
- Riga di pulsanti azione: APPLICA FILTRI, APPLICA FILTRI E SALVA, CANCELLA FILTRI, AGGIORNA, ESCI, ESPORTA EXCEL (icona).

### 7.4 Paginazione

Posizionata in basso a destra:
- Indicatore "pagina X di Y".
- Controlli: `<<` (prima pagina), `<` (precedente), `>` (successiva), `>>` (ultima pagina).

---

## 8. Pulsanti

### 8.1 Tipologie

| Tipo | Aspetto | Esempi |
|---|---|---|
| Primario | Sfondo blu pieno, testo bianco MAIUSCOLO | CONFERMA, ACCETTA, SALVA E PROSEGUI, CHIUDI PRATICA, APPLICA FILTRI, AGGIORNA |
| Secondario / Outline | Bordo blu, sfondo bianco, testo blu MAIUSCOLO | INDIETRO, MODIFICA, CANCELLA FILTRI, NASCONDI SEZIONE, NASCONDI ALLEGATI, ESCI |
| Link inline | Testo blu sottolineato | "+ Aggiungi nuovo link", link colonna "Pratica N.", "Attività" |
| Icon button | Icona blu, opzionale tooltip | Occhio (preview), edit/delete (link favoriti), Esporta Excel, Aggiorna |
| Stato disabled | Grigio chiaro, non cliccabile | CONFERMA prima della selezione, Conformità prima del flag presenza |

### 8.2 Posizionamento

- Pulsanti di **azione primaria di milestone**: in fondo alla schermata, allineati a destra o centrati.
- Pulsanti di **azione su sezione** (NASCONDI SEZIONE / NASCONDI ALLEGATI): vicino al titolo di sezione o accanto al blocco.
- Pulsanti di **filtro**: subito sotto la riga dei filtri, allineati a sinistra.
- Pulsanti **ACCETTA / INDIETRO** nella schermata di Presa in Carico: affiancati, primario a destra, secondario a sinistra.

### 8.3 Convenzioni di etichetta

- Testo sempre in **MAIUSCOLO** per pulsanti CTA.
- Etichette concise, orientate al verbo ("CONFERMA", "MODIFICA", "CHIUDI PRATICA").
- Etichette identiche tra Operatore e Supervisore per stesse azioni.

---

## 9. Form

### 9.1 Pattern di campo

- Label sopra il campo o alla sinistra, testo grigio scuro.
- Input rettangolari con bordo grigio sottile, padding interno comodo.
- Dropdown con freccia in basso a destra, lista a discesa stile sistema.
- Date picker da/a su due input affiancati.
- Radio button e checkbox di stile classico, blu quando selezionati.
- Indicatore obbligatorio: marcatura implicita (non esplicitata negli screenshot — preservare comportamento di disabilitazione del CTA finché i campi obbligatori non sono compilati).

### 9.2 Validazione

- Validazione operata al click del CTA primario (es. CONFERMA, SALVA E PROSEGUI).
- Vincoli condizionali realizzati con **disabilitazione** di campi/colonne (es. colonna Conformità disabilitata se "Presenza documento" = No).
- Messaggi di errore: non descritti nei manuali — preservare un'area di feedback inline o a livello di form senza inventare nuovi pattern.

### 9.3 Form Checklist (pattern specifico)

Tabella editabile con righe-controllo, ogni riga contiene:
- Indicatore "Richiesto" (icona a due stati).
- "Descrizione" + pulsante "Mostra Descrizione" per espandere il testo guida.
- "Conforme" (radio Si/No, gestito a livello colonna o riga).
- "Note" (campo testo).
- "Esito Controllo" (OK/KO, calcolato).
- "Causale" (dropdown popolato dinamicamente solo se richiesto).

---

## 10. Linea di avanzamento (process bar)

Componente caratteristico del processo:

- Striscia orizzontale **gialla** a tutta larghezza, ospitata in card.
- Sopra: tre step etichettati — **Raccolta input** → **Lavorazione** → **Chiusura Pratica**.
- Linea **blu** orizzontale che attraversa gli step, evidenziando lo stato corrente con un marker o riempimento progressivo.
- Sotto: card con informazioni identificative della pratica (processo, CF, Pratica N.).

È un elemento sempre presente nelle schermate di Tipizzazione, Lavorazione, e Dettaglio Pratica Riepilogo. Va preservato come marcatore visivo dell'intero processo.

---

## 11. Milestone navigation

Componente di navigazione verticale (o orizzontale a seconda del breakpoint) presente in:
- Dettaglio Pratica Riepilogo (consultazione): tasti **Dati Pratica**, **Dati Lavorazione**, **Esito**.
- Schermata di Lavorazione: tasti **Dati Pratica**, **Verifica Documento**, **Riepilogo**.

Stile pulsanti milestone:
- Card-pulsante con label e indicatore di stato (attivo / completato / inattivo).
- Click sul pulsante → apre la milestone corrispondente.
- Milestone non ancora attivabili (es. Riepilogo prima di SALVA E PROSEGUI): stile disabled.

---

## 12. Visual hierarchy

Ordine gerarchico tipico in una schermata di lavorazione:

1. **Header applicativo** (tab + contatori).
2. **Linea di avanzamento** (striscia gialla con step e marker blu).
3. **Card identificativa pratica** (processo + dati chiave).
4. **Pannello milestone** (navigazione tra Dati Pratica / Verifica Documento / Riepilogo).
5. **Area centrale**:
   - In Tipizzazione: due colonne (sinistra Tipo Documento, destra Preview).
   - In Verifica Documento: due colonne (sinistra Dati Cliente / Dati Carta Bloccata / Contenuti Verbale / Controllo Verbale, destra Preview).
   - In Riepilogo: card APPROVATA / RESPINTA + eventuale box Note interne.
6. **Barra azioni** in fondo (SALVA E PROSEGUI / MODIFICA / CHIUDI PRATICA / CONFERMA).

---

## 13. Pattern di interazione comuni

- **Espansione/Compressione**: barre blu cliccabili per sottosezioni (es. Indirizzo di Residenza), tasti NASCONDI SEZIONE / NASCONDI ALLEGATI per sezioni intere.
- **Preview documento integrata**: icona occhio apre l'anteprima nella colonna destra; selezione dimensione (Piccolo/Medio/Grande); fallback con download manuale.
- **Help contestuale**: pulsante "Mostra Descrizione" per ogni controllo della checklist; box informativo con icona "i" in Tipizzazione.
- **Filtri salvabili**: pulsante "APPLICA FILTRI E SALVA" nella Lista Attività.
- **Reset filtri**: pulsante "CANCELLA FILTRI" / "Pulisci Filtri" ovunque.

---

## 14. Stati visivi di sistema

| Stato | Trattamento visivo |
|---|---|
| Disabled | Componente in grigio chiaro, cursore default, no hover |
| Loading | Non documentato negli screenshot — preservare un pattern non invasivo (es. spinner inline) senza reinventare l'UX |
| Empty state | Non documentato — preservare placeholder testuali sobri |
| Error / Validation | Non documentato negli screenshot — adottare pattern inline coerente con tipografia e palette |
| Hover su riga tabella | Highlight tenue |
| Selezione riga (checkbox) | Riga evidenziata + checkbox spuntato in blu |
| Tab attivo | Sottolineatura o riempimento blu sotto la label del tab |

---

## 15. Conflitti e ambiguità di stile

Per trasparenza verso il MAS:

1. **Tonalità esatte di giallo e blu**: gli screenshot suggeriscono il giallo brand Poste e un blu istituzionale, ma le esatte coordinate cromatiche non sono dichiarate nei manuali. Il MAS deve allinearsi al brand book Poste Italiane se disponibile.
2. **Layout responsivo**: i manuali e gli screenshot mostrano viewport desktop ampio. Il comportamento su schermi più piccoli (tablet/mobile) non è documentato.
3. **Iconografia**: i manuali citano icone (occhio, freccia su/giù, frecce paginazione, icona "i", icone edit/delete) ma non forniscono nomenclatura di una libreria specifica. Il MAS può adottare un set icone coerente (es. Material o equivalente neutro), preservando i significati.
4. **Dark mode**: non prevista nelle fonti.

---

Fine documento.
