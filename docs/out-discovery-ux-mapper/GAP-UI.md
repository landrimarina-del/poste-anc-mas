# GAP-UI — Specifica Componenti, Layout e Stile

> **Scopo**: Documento di riferimento UI per il gruppo di sviluppo frontend.  
> Definisce componenti, layout, stile visivo, regole di visibilità condizionale e specifiche di campo  
> ricavati dal reverse engineering dell'applicazione Appian originale (`05_ux-ui.md`).  
>
> **Fonte di verità**: `docs/reverse/attivazione-nuova-carta/v20260506/05_ux-ui.md`  
> **Data analisi reverse**: 2026-05-20 | **Data documento**: 2026-05-21  
> **Target tecnologico**: React SPA + Tailwind CSS / MUI (o equivalente)

---

## 1. Style Guide

### 1.1 Colori

| Token | Valore HEX | Utilizzo |
|---|---|---|
| `--color-accent` / `--color-primary` | `#0047BB` | Link, icone attive, badge, bottoni primari, bordi evidenza |
| `--color-dashboard-supervisore-bg` | `#FFEC00` | Billboard background Dashboard Supervisore (distingue il ruolo visivamente) |
| `--color-pratiche-chiuse` | `#008000` | Contatore "Pratiche Chiuse" nella Dashboard Operatore |
| `--color-billboard-operatore-bg` | default (bianco/grigio chiaro) | Billboard background Dashboard Operatore — nessun backgroundColor esplicito |

### 1.2 Forme (Button / Input / Dialog)

- `buttonShape: SQUARED` → tutti i bottoni con bordo rettangolare (border-radius: 0 o minimo)
- Label bottoni in **MAIUSCOLO** (text-transform: uppercase)
- Dialog modali: forma SQUARED (stile conforme alle finestre Appian SQUARED shape)

### 1.3 Stile Box / Card

- Box container con stile `BOA_STYLE_POSTE`:
  - Bordo evidenziato con colore `--color-accent` (`#0047BB`)
  - Header box in grassetto, sfondi neutri
  - Bordo sottile visibile
- Card style `INFO`: sfondo azzurro chiaro, icona `info-circle`, testo descrittivo

### 1.4 Typography

- Bottoni: UPPERCASE
- Label sezione (boxLayout header): `STRONG` weight (font-weight: 600/700)
- Link navigazione (tab, sidebar): `ACCENT` color, `MEDIUM` size, `STRONG` weight
- Icone: FontAwesome 4.x (classi `fa-*`)

### 1.5 Spacing e Layout

- Pagine: width `WIDE` → full-width layout container
- Billboard: altezza `SHORT` per l'header pratica (`BOA_ANC_Header`)
- Sidebar task: due stati — `NARROW` (con label) / `EXTRA_NARROW` (solo icone, collassata)
- Colonne layout principale task: `columnsLayout spacing: SPARSE, showDividers: true`

---

## 2. Layout Pattern per Schermata

### 2.1 Dashboard Operatore

**Componente React**: `DashboardOperatore`

```
Layout radice: billboardLayout (Billboard hero → 2 colonne body)

[BILLBOARD BANNER]
  background: default (no color specifico)

[BODY — columnsLayout]
  col-sx (MEDIUM):
    boxLayout "Pratiche Attive"      → contatore (text ACCENT LARGE)
    boxLayout "Pratiche In Carico"   → contatore (text ACCENT LARGE)
    boxLayout "Pratiche Chiuse"      → contatore (text #008000 LARGE)
  col-dx:
    boxLayout "Azioni"
      richTextIcon + richTextItem "{nomeAzione}"  (forEach azioni gruppo — dinamico)
    boxLayout "Link Favoriti"
      showWhen: utenteLinkFavoriti != "" AND gestisciLinks = false
        richTextIcon "edit" | richTextIcon "remove" | richTextItem "{titoloLink}" (forEach)
      showWhen: utenteLinkFavoriti = "" AND gestisciLinks = false
        richTextDisplayField "Nessun link presente"
      richTextIcon "plus-square-o" "Aggiungi link"
      showWhen: gestisciLinks = true  → card form inline:
        a!textField "Link" (validated: isValidURL)
        a!dropdownField "Categoria"
        Button "Salva" [PRIMARY] | Button "Esci" [SECONDARY]
```

### 2.2 Dashboard Supervisore

**Componente React**: `DashboardSupervisore`

```
Layout radice: billboardLayout (backgroundColor: #FFEC00)

[BILLBOARD BANNER — sfondo GIALLO #FFEC00]

[BODY — columnsLayout]
  col-sx:
    boxLayout grafici:
      GraficiPraticheGiornaliere        → grafico
      GraficiPraticheGiornaliereLavorate → grafico
  col-dx:
    GraficiPraticheByStato              → grafico (distribuzione stati)
    boxLayout "Azioni"
      richTextIcon + richTextItem "{nomeAzione}" (forEach azioni Supervisore)
```

### 2.3 Lista Attività

**Componente React**: `ListaAttivita`

```
Layout radice: gridField + boxLayout

[FILTRI]
  Form principale filtri (BOA_ANC_FiltriUtente_ListaAttivita):
    sideBySideLayout:
      a!textField              "Pratica N."         → text input
      a!dropdownField_v1       "Stato"              → dropdown
      a!multipleDropdownField  "Tipo Pratica"       → SEMPRE DISABLED (disabled: true hardcoded)
      a!pickerFieldUsersAndGroups "Assegnatari"     → disabled se tipoProcesso IS NULL
    a!ButtonLayout:
      Button "Applica Filtri"         [PRIMARY]   → filtra (no save)
      Button "Applica e Salva Filtri" [SECONDARY] → POST /tasks/filters/saved + filtra

  a!checkboxField "Visualizza le attività a me assegnate"
    disabled: myTasksTipoProcesso IS NULL

[FILTRI SALVATI]
  boxLayout "Ultimi N Filtri Salvati"
    gridField selectionStyle: ROW_HIGHLIGHT
      Colonne: Stato | Tipo Pratica | Pratica N. | Nome Attività |
               Data Scadenza Da/A | Assegnatario | Utente in carico

[GRIGLIA ATTIVITÀ PRINCIPALI]
  gridField pageSize: 8
    Colonne visibili:
      1. Nome Attività
      2. Stato
      3. Tipo Pratica
      4. Pratica N.
      5. Assegnatario
      6. Utente in carico
      7. Data Scadenza
    → click riga: naviga a /task/:taskId
```

> **Nota**: I filtri "Data Scadenza Da" e "Data Scadenza A" sono presenti nel codice sorgente ma **commentati** → non renderizzare questi filtri.

### 2.4 Task Lavorazione

**Componente React**: `TaskLavorazione`

```
Layout radice: columnsLayout (spacing: SPARSE, showDividers: true)

[SIDEBAR — col-sx, NARROW/EXTRA_NARROW toggle]
  BOA_ANC_Task_MenuLaterale
    richTextIcon angle-double-left (collassa) / angle-double-right (espandi)
    forEach cardLayout:
      voce 1: icon briefcase      "Dati Pratica"       → link (sempre attivo) → activeSection=1
      voce 2: icon check-square-o "Verifica Documento" → link (sempre attivo) → activeSection=2
      voce 3: icon address-card-o "Riepilogo"          → link SOLO SE esitoSD IS NOT NULL

[CONTENT AREA — col-dx]
  choose(activeSection):
    case 1: SezioniDatiPratica (readOnly: true)
    case 2: VerificaDocumenti (readOnly: esitoSD IS NOT NULL)
    case 3: TaskRiepilogo

[FOOTER — ButtonLayout]
  primaryButtons:
    Button "Salva e prosegui"  [PRIMARY]   showWhen: activeSection=2
                                           disabled: esitoSD IS NOT NULL
    Button "chiudi pratica"    [PRIMARY]   showWhen: activeSection=3
  secondaryButtons:
    Button "Modifica"          [SECONDARY] sempre visibile
```

### 2.5 Verifica Documenti (embedded in Task Lavorazione)

**Componente React**: `VerificaDocumenti`

```
[BUTTON BAR — in cima]
  Button "nascondi allegati"/"mostra allegati"  [PRIMARY]   icon: eye-slash/eye
  Button "nascondi sezione"/"mostra sezione"    [SECONDARY] icon: eye-slash/eye

[columnsLayout 2-col]
  col-sx  showWhen: showSection=true
    SezioneDataCliente   (isCollapsible: true, isInitiallyCollapsed: false)
    SezioneDatiCarta     (isCollapsible: true, isInitiallyCollapsed: false)
    ContenutiBriefcase   (BOA_ANC_Contenuti_Section — columnsLayout 2-col nested)
    CheckList            (readOnly: false in task, readOnly: true in summary)

  col-dx  showWhen: showAllegati=true
    SezioneDocumenti
```

### 2.6 Sezione Dati Pratica

**Componente React**: `SezioneDatiPratica`

```
boxLayout "Dati Pratica"  style: BOA_STYLE_POSTE  (sempre readOnly in Summary)

Campi (tutti readOnly):
  sideBySideLayout:
    a!dateField  "Data Presa in Carico"  → sempre visibile
    a!dateField  "Data Richiesta"        → sempre visibile
  sideBySideLayout:
    a!textField  "Stato Pratica"         → sempre visibile
    a!textField  "Codice Cliente"        → sempre visibile
  sideBySideLayout:
    a!textField  "Codice Fiscale"        → sempre visibile
    a!textField  "Canale"               → sempre visibile
  sideBySideLayout:
    a!dateField  "Data Chiusura"         → showWhen: dataChiusura IS NOT NULL
  sideBySideLayout:
    a!textField  "Esito SD"             → showWhen: esitoSD IS NOT NULL AND readOnly=true
```

### 2.7 Sezione Dati Cliente

**Componente React**: `SezioneDatiCliente`

```
boxLayout "Dati Cliente"  isCollapsible: true  isInitiallyCollapsed: false

4 righe sideBySideLayout, ~11 campi anagrafici readOnly:
  riga 1: Cognome | Nome
  riga 2: Data di Nascita | Luogo di Nascita | Codice Fiscale
  riga 3: Tipo Documento | Numero Documento | Scadenza Documento
  riga 4: [BOA_ANC_Sezione_IndirizzoResidenza_DatiCliente — nested]
           Via | Civico | CAP | Comune | Provincia | Nazione
```

### 2.8 Sezione Dati Carta

**Componente React**: `SezioneDatiCarta`

```
boxLayout "Dati Carta Bloccata"  isCollapsible: true  isInitiallyCollapsed: false

1 riga sideBySideLayout, 3 campi readOnly:
  Tipo Carta | Numero Carta | Intestazione Carta
```

### 2.9 Sezione Documenti (Allegati)

**Componente React**: `SezioneDocumenti`

```
showWhen: contenuti IS NOT NULL
  → forEach(allegati):
      boxLayout [nome file]
        IF idDocAppian IS VALID:
          file viewer inline (preview embed)
        ELSE:
          richTextIcon link-o + richTextItem "documento disponibile solo tramite link"
          + dynamicLink "QUI" → startProcess download singolo documento

showWhen: contenuti IS NULL
  cardLayout style: INFO
    richTextIcon "info-circle" + richTextItem "Nessun contenuto associato alla pratica"
```

### 2.10 Task Riepilogo

**Componente React**: `TaskRiepilogo`

```
richTextDisplayField  esito calcolato (richTextIcon + testo esito)

cardLayout  motivazioni
  showWhen: motivazioni IS NOT NULL
  → lista motivazioni

forEach(esiti):
  cardLayout → dettaglio esito singolo

paragraphField "Note Interne"
  showWhen: esitoControlli IS NOT NULL AND esitoControlli != BOA_ANC_ESITI_SD[1]
  (= mostrata SOLO se esito NEGATIVO — non mostrata se esito POSITIVO)
  readOnly: false
```

### 2.11 Task Tipizzazione Documenti

**Componente React**: `TaskTipizzazione`

```
showWhen: contenuti IS NULL
  cardLayout style: INFO
    richTextIcon "info-circle" + richTextItem "Nessun contenuto associato alla pratica"

showWhen: contenuti IS NOT NULL
  columnsLayout 2-col:
    col-sx:
      boxLayout "Tipizzazione Documento"  style: BOA_STYLE_POSTE
        cardLayout style: INFO  shape: SEMI_ROUNDED  → istruzioni
        dropdownField "Tipo Documento"  required
        ButtonLayout:
          Button "Conferma"  [PRIMARY]
            disabled: tipoDocSelected IS NULL
            → confirmHeader: "ATTENZIONE"
            → confirmMessage: "E' stato selezionato {tipo} come tipologia.
               Attenzione: non sarà possibile modificare il tipo documento in futuro.
               Confermare la selezione?"
    col-dx:
      boxLayout [documento.descrizione]  isCollapsible: true
        forEach(contenuti): file preview
```

### 2.12 Dettaglio Pratica — Summary

**Componente React**: `DettaglioPratica`

```
Layout radice: sideBySideLayout tab links + cardLayout per tab

[HEADER — BOA_ANC_Header]
  billboardLayout height: SHORT, marginBelow: NONE, overlay: fullOverlay style: NONE
    columnsLayout: milestoneField (stati pratica in sequenza)
    columnsLayout 2-col:
      col-sx [MEDIUM]: avatar app + "Attivazione Nuova Carta" + "Pratica N. {n}"
      col-dx: stato pratica + canale + CF

[SPACER] richTextDisplayField

[TAB LINKS — sideBySideLayout]
  icon: file-o        → ACCENT LARGE CENTER
  icon: file-image-o  → ACCENT LARGE CENTER
  icon: legal         → ACCENT LARGE CENTER
  
  dynamicLink "Dati Pratica"     → selezione="Pratica"   [ACCENT MEDIUM STRONG]
  dynamicLink "Dati Lavorazione" → selezione="Video"     [ACCENT MEDIUM STRONG]
  dynamicLink "Esito"            → selezione="Esito"     [ACCENT MEDIUM STRONG]

[CARD TAB — Dati Pratica]    showWhen: selezione="Pratica"
  SezioneDatiPratica (readOnly: true)

[CARD TAB — Dati Lavorazione] showWhen: selezione="Video"
  VerificaDocumenti (readOnly: true)

[CARD TAB — Esito]           showWhen: selezione="Esito"
  SummaryEsito
```

### 2.13 Summary Esito

**Componente React**: `SummaryEsito`

```
boxLayout "Esito Verifiche Back Office"  style: BOA_STYLE_POSTE

  richTextDisplayField   esito (richTextIcon + testo esito)
  
  a!dateField "Data Esito Scrivania Digitale"
    showWhen: pratica.dataEsitoSD IS NOT NULL

  boxLayout "Note"
    showWhen: nota IS NOT NULL AND nota IS NOT EMPTY
    paragraphField (readonly, valore: nota)
```

### 2.14 Lista Pratiche

**Componente React**: `ListaPratiche`

```
Layout radice: recordGridField (griglia record)

Colonne con visibilità esplicita:
  N. | Campo             | Visibile | Note
  1  | Id                | NO       | hidden (showWhen: false)
  2  | Pratica N.        | SÌ       | recordLink → DettaglioPratica
  3  | Stato             | SÌ       |
  4  | Canale            | NO       | hidden (showWhen: false)
  5  | Tipo Pratica      | SÌ       |
  6  | Codice Fiscale    | SÌ       |
  7  | Assegnatario      | SÌ       |
  8  | Utente in Carico  | SÌ       |
  9  | Data Apertura     | SÌ       |
  10 | Data Presa Carico | SÌ       |
  11 | Data Scadenza     | NO       | hidden (showWhen: false)
  12 | Esito SD          | SÌ       |
  13 | Data Esito SD     | SÌ       |
  14 | Segnalazioni      | SÌ       | richTextIcon (icona warning se segnalazioni attive)

→ click riga o link "Pratica N." → DettaglioPratica
```

### 2.15 Riassegnazione Task

**Componente React**: `RiassegnazioneTask`

```
boxLayout "Dettagli riassegnazione"  style: BOA_STYLE_POSTE
  richTextItem "Processo: Attivazione Nuova Carta"
  
  showWhen: processo IS NOT NULL
    columnsLayout 2-col:
      col-sx: radioButtonField "Scegli Tipologia di Riassegnazione"  required
                opzione 1: "Riassegna al Gruppo Operatore"
                opzione 2: "Riassegna a Utenti"
      col-dx: pickerFieldUsers "Inserisci utenti..."
                showWhen: tipoRiassegnazione = "Utenti"
                required: true

boxLayout "Le attività dei miei processi"
  showWhen: processo IS NOT NULL
  BOA_ANC_Filtri_TaskReportSupervisoreGruppi (filtri embedded)
  gridField  selectable (multi-select)  selectionRequired: true  pageSize: 8
    Colonne visibili:
      1. Processo
      2. Pratica
      3. Nome Attività
      4. Assegnatario
      5. Owner
      6. Data Assegnazione
      7. Data Presa in Carico
      8. [Data Scadenza — hidden: showWhen: false]
      9. Stato (icona)

ButtonLayout:
  Button "Riassegna"  [PRIMARY]
```

---

## 3. Mappa Visibilità Condizionale — Tutti i Campi

Questa tabella è la fonte ufficiale per le condizioni React (`{condition && <Component/>}` oppure `style={{display: condition ? 'block' : 'none'}}`).

| Schermata | Componente / Campo | Condizione visibilità | Tipo |
|---|---|---|---|
| TaskLavorazione | VerificaDocumenti intero (readOnly toggle) | `readOnly = pratica.esitoSD != null` | state-driven |
| TaskLavorazione | Button "Salva e prosegui" | `activeSection === 2` | step-driven |
| TaskLavorazione | Button "Salva e prosegui" disabled | `pratica.esitoSD != null` | state-driven |
| TaskLavorazione | Button "chiudi pratica" | `activeSection === 3` | step-driven |
| TaskLavorazione | Voce sidebar "Riepilogo" enabled | `pratica.esitoSD != null` | state-driven |
| DashboardOperatore | Lista link favoriti | `utenteLinkFavoriti != "" && !gestisciLinks` | data-driven |
| DashboardOperatore | "Nessun link presente" | `utenteLinkFavoriti === "" && !gestisciLinks` | data-driven |
| DashboardOperatore | Card form gestione link | `gestisciLinks === true` | toggle |
| SezioneDatiPratica | Campo "Data Chiusura" | `pratica.dataChiusura != null` | data-driven |
| SezioneDatiPratica | Campo "Esito SD" | `pratica.esitoSD != null && readOnly === true` | state+mode |
| SummaryEsito | "Data Esito Scrivania Digitale" | `pratica.dataEsitoSD != null` | data-driven |
| SummaryEsito | Box "Note" | `nota != null && nota !== ""` | data-driven |
| TaskRiepilogo | Card motivazioni | `motivazioni != null && motivazioni.length > 0` | data-driven |
| TaskRiepilogo | Field "Note Interne" | `esitoControlli != null && esitoControlli !== ESITO_POSITIVO` | state-driven |
| TaskTipizzazione | Card info "Nessun contenuto" | `contenuti === null` | data-driven |
| TaskTipizzazione | Form tipizzazione 2-col | `contenuti != null` | data-driven |
| TaskTipizzazione | Button "Conferma" disabled | `tipoDocSelected === null` | input-driven |
| RiassegnazioneTask | Picker utenti | `tipoRiassegnazione === "Utenti"` | radio-driven |
| RiassegnazioneTask | BOX radio+picker | `processo != null` | state-driven |
| RiassegnazioneTask | BOX griglia attività | `processo != null` | state-driven |
| ListaAttivita | Checkbox "Attività a me assegnate" disabled | `myTasksTipoProcesso === null` | state-driven |
| ListaAttivita | Filtro "Assegnatari" disabled | `tipoProcesso === null` | input-driven |
| ListaAttivita | Filtro "Tipo Pratica" | sempre disabled (hardcoded) | — |
| VerificaDocumenti | Col-sinistra dati | `showSection === true` | toggle |
| VerificaDocumenti | Col-destra allegati | `showAllegati === true` | toggle |
| SezioneDocumenti | Box lista allegati | `contenuti != null && contenuti.length > 0` | data-driven |
| SezioneDocumenti | Card info "Nessun contenuto" | `contenuti == null \|\| contenuti.length === 0` | data-driven |
| ListaPratiche | Colonna "Id" | `showWhen: false` → sempre nascosta | never |
| ListaPratiche | Colonna "Canale" | `showWhen: false` → sempre nascosta | never |
| ListaPratiche | Colonna "Data Scadenza" | `showWhen: false` → sempre nascosta | never |
| RiassegnazioneTask | Colonna "Data Scadenza" griglia | `showWhen: false` → sempre nascosta | never |
| ListaAttivita | Filtro "Data Scadenza Da" | commentato → non renderizzare | never |
| ListaAttivita | Filtro "Data Scadenza A" | commentato → non renderizzare | never |

---

## 4. Specifiche Form e Validazione

### 4.1 Regole di Validazione

| Schermata | Campo | Regola | Messaggio Errore |
|---|---|---|---|
| DashboardOperatore | `textField "Link"` | `isValidURL(value) === true` | `"Inserire un link valido."` |
| RiassegnazioneTask | `radioButtonField "Tipologia Riassegnazione"` | required | (messaggio standard) |
| RiassegnazioneTask | `pickerFieldUsers "Inserisci utenti"` | required se tipoRiassegnazione="Utenti" | (messaggio standard) |
| TaskTipizzazione | `dropdownField "Tipo Documento"` | required (implicito — Conferma disabled se null) | Button Conferma disabled |

### 4.2 Pattern Form Inline (Edit Toggle)

Usato nella Dashboard Operatore per la gestione dei link favoriti.

```
PATTERN: Show/Hide inline form via stato locale
  Default: lista visualizzazione (read)
  Trigger: click icona edit/plus → gestisciLinks = true → form inline appare
  Submit: POST → gestisciLinks = false → lista aggiornata
  Cancel: Button "Esci" → gestisciLinks = false → lista invariata
```

### 4.3 Pattern Grid con Filtri Salvati

Usato nella Lista Attività.

```
PATTERN: Grid + Filtri Salvati
  Grid filtri salvati (ROW_HIGHLIGHT selection): click riga → carica filtri nel form
  Form filtri: editabile → Button "Applica" (filtra) | Button "Applica e Salva" (salva + filtra)
  Nota: la selezione riga filtri salvati NON applica automaticamente i filtri
         → l'utente deve cliccare "Applica Filtri" dopo aver selezionato
```

---

## 5. Componenti UI Riutilizzabili

| Componente | Usato in | Props chiave |
|---|---|---|
| `BoxPoste` (style: BOA_STYLE_POSTE) | DatiPratica, DatiCliente, DatiCarta, RiassegnazioneTask, Tipizzazione | `label`, `isCollapsible`, `isInitiallyCollapsed` |
| `CardInfo` (style: INFO) | SezioneDocumenti (empty state), TaskTipizzazione (empty state), TaskTipizzazione istruzioni | `icon`, `message` |
| `MilestoneBar` (milestoneField) | BOA_ANC_Header → DettaglioPratica | `stati`, `statoCorrente` |
| `BillboardHeader` (billboardLayout SHORT) | DettaglioPratica header | `height: SHORT`, `overlay content` |
| `BillboardBanner` (billboardLayout TALL) | Dashboard Operatore, Dashboard Supervisore | `backgroundColor` |
| `SidebarNav` | TaskLavorazione | `steps[]`, `activeStep`, `collapsed`, `onStepClick` |
| `TabNav` | DettaglioPratica | `tabs[]`, `activetab`, `onTabChange` |
| `ToggleVisibilityButtons` | VerificaDocumenti | `showSection`, `showAllegati`, `onToggle` |
| `ConfirmDialog` | TaskTipizzazione "Conferma" | `header`, `message`, `onConfirm`, `onCancel` |
| `FileViewer` | SezioneDocumenti | `idDocAppian`, `fallbackDownloadUrl` |
| `RichTextIcon` | Varie | `icon`, `color`, `size` (fa-* classe) |

---

## 6. Milestone Field — Stati Pratica

Il `milestoneField` nell'header del dettaglio pratica visualizza le fasi in sequenza con indicazione della fase corrente.

```
Stati in sequenza (milestone):
  RACCOLTA_INPUT  →  LAVORAZIONE  →  CHIUSURA_PRATICA

Mapping al campo `fase` derivato (da GET /practices/{id}):
  RACCOLTA_INPUT     = pratica ha documenti da tipizzare o task non iniziato
  LAVORAZIONE        = task in corso, checklist non ancora completata
  CHIUSURA_PRATICA   = pratica esitata o chiusa

Visualizzazione React:
  Fase precedente alla corrente → completed (check mark)
  Fase corrente                 → active (highlight ACCENT #0047BB)
  Fase successiva               → pending (grigio)
```

---

## 7. Colonne Griglia — Specifiche Complete

### 7.1 Lista Pratiche (RecordType)

| N. | Label | Visibile | Tipo | Note |
|---|---|---|---|---|
| 1 | Id | **NO** | text | hidden |
| 2 | Pratica N. | SÌ | recordLink | naviga a Summary |
| 3 | Stato | SÌ | badge/text | |
| 4 | Canale | **NO** | text | hidden |
| 5 | Tipo Pratica | SÌ | text | |
| 6 | Codice Fiscale | SÌ | text | |
| 7 | Assegnatario | SÌ | text | |
| 8 | Utente in Carico | SÌ | text | |
| 9 | Data Apertura | SÌ | date | |
| 10 | Data Presa in Carico | SÌ | date | |
| 11 | Data Scadenza | **NO** | date | hidden |
| 12 | Esito SD | SÌ | text/badge | |
| 13 | Data Esito SD | SÌ | date | |
| 14 | Segnalazioni | SÌ | richTextIcon | icona warning se segnalazioni attive |

### 7.2 Lista Attività

| N. | Label | Visibile | Note |
|---|---|---|---|
| 1 | Nome Attività | SÌ | |
| 2 | Stato | SÌ | |
| 3 | Tipo Pratica | SÌ | |
| 4 | Pratica N. | SÌ | |
| 5 | Assegnatario | SÌ | |
| 6 | Utente in Carico | SÌ | |
| 7 | Data Scadenza | SÌ | |

### 7.3 Griglia Riassegnazione

| N. | Label | Visibile | Note |
|---|---|---|---|
| 1 | Processo | SÌ | |
| 2 | Pratica | SÌ | |
| 3 | Nome Attività | SÌ | |
| 4 | Assegnatario | SÌ | |
| 5 | Owner | SÌ | |
| 6 | Data Assegnazione | SÌ | |
| 7 | Data Presa in Carico | SÌ | |
| 8 | Data Scadenza | **NO** | hidden (showWhen: false) |
| 9 | Stato | SÌ | icona |

### 7.4 Griglia Filtri Salvati (Lista Attività)

| Label |
|---|
| Stato |
| Tipo Pratica |
| Pratica N. |
| Nome Attività |
| Data Scadenza Da/A |
| Assegnatario |
| Utente in carico |

---

## 8. Elementi UI Non Coperti nei Documenti Architetturali Precedenti

| ID | Elemento | Fonte Reverse | Azione richiesta |
|---|---|---|---|
| UI-GAP-01 | `milestoneField` header pratica → 3 fasi (RACCOLTA_INPUT / LAVORAZIONE / CHIUSURA_PRATICA) | `05_ux-ui.md §BOA_ANC_Header` | Implementare componente `MilestoneBar` con fasi derivate da `GET /practices/:id` campo `fase`. Dettagli in `GAP_Architettura.md §GAP-US-06`. |
| UI-GAP-02 | Avatar app + nome "Attivazione Nuova Carta" nel header pratica | `05_ux-ui.md §BOA_ANC_Summary` | Asset immagine avatar da definire. In POC: testo con icona fa-credit-card. |
| UI-GAP-03 | Card shape `SEMI_ROUNDED` nelle istruzioni tipizzazione | `05_ux-ui.md §BOA_ANC_Task_TipizzazioneDocumenti` | Se la lib CSS non supporta SEMI_ROUNDED usare border-radius: 4px. |
| UI-GAP-04 | `BOA_ANC_Contenuti_Section` (columnsLayout 2-col) — struttura interna non dettagliata nel reverse | `05_ux-ui.md §STEP 2` | Verificare nel codice Appian la struttura. In POC: columnsLayout 2-col con campo testo/label. |
| UI-GAP-05 | Colonna "Segnalazioni" in Lista Pratiche = `richTextIcon` (warning) non boolean semplice | `05_ux-ui.md §Pratica_ANC List View` | `segnalazioni` nel DTO è calcolata (vedere `GAP_Architettura.md §GAP-US-09`). Renderizzare come icona `fa-exclamation-triangle` (accent/warning) se valore presente, altrimenti vuoto. |
| UI-GAP-06 | Sidebar task: icona collapse/expand = `angle-double-left` (espansa) / `angle-double-right` (collassata) | `05_ux-ui.md §BOA_ANC_Task_Lavorazione` | Implementare toggle stato sidebar. In stato EXTRA_NARROW: nascondere label, mostrare solo icona voce navigazione. |
| UI-GAP-07 | `selectionStyle: ROW_HIGHLIGHT` nella griglia filtri salvati | `05_ux-ui.md §BOA_ANC_ListaAttivita` | La riga selezionata nella griglia filtri salvati deve essere evidenziata con sfondo ACCENT. |
| UI-GAP-08 | `selectable + selectionRequired: true` nella griglia riassegnazione | `05_ux-ui.md §BOA_ANC_Intertfaccia_RiassegnazioneTask` | Griglia con checkbox multi-selezione. "Riassegna" abilitato solo se almeno una riga selezionata. |
