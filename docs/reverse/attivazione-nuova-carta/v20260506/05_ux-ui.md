---
app: "Attivazione Nuova Carta"
prefix: "BOA_ANC"
layer: "ux-ui"
export_version: "20260506"
analyzed: "2026-05-20"
output_dir: "docs/reverse/attivazione-nuova-carta/v20260506/"
---

# Attivazione Nuova Carta — Reverse Engineering UX/UI
**Export version**: 20260506 | **Analyzed**: 2026-05-20 | **Layer**: UX/UI (FASE 1: STEP 0 → STEP 3b)

---

## STEP 0 — Ricognizione Risorse UX/UI

### Fonti lette
- `META-INF/export.log` → mapping completo UUID → nome simbolico
- `application/_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9070895.xml` → nome app, prefisso, oggetti associati
- `site/ae0c311c-d23e-4b54-bdc1-918dcfe70834.xml` → Scrivania Digitale Operatore ANC
- `site/d72fc841-0f52-4131-8737-6d8e8cd1f319.xml` → Scrivania Digitale Supervisore ANC
- `content/*.xml` → tutti gli artefatti UX/UI (interface rules)
- `recordType/126d7d57-5922-4aed-b5ee-14b28d796f95.xml` → Pratica_ANC

### Variabili estratte

| Variabile | Valore |
|---|---|
| APP_NAME | Attivazione Nuova Carta |
| APP_PREFIX | BOA_ANC |
| EXPORT_ROOT | `appian-export/Attivazione Nuova Carta/` |
| URL_IDENTIFIER | `gGsQzg` |

---

### Inventario Risorse UX/UI

| Tipo Artefatto | Nome | UUID | File | Note |
|---|---|---|---|---|
| Site | Scrivania Digitale Operatore ANC | ae0c311c-d23e-4b54-bdc1-918dcfe70834 | site/ae0c311c...xml | urlStub: scrivania-digitale-operatore-anc |
| Page | Home (Operatore) | 78c6bde7-8382-3a78-92cd-600a93d1f4f6 | site/ae0c311c...xml | → BOA_ANC_Interfaccia_DashBoard_Operatore |
| Page | Attività | 54a0c752-2fab-33d6-9942-6b910a3734f3 | site/ae0c311c...xml | → BOA_ANC_ListaAttivita |
| Page | Pratiche (Operatore) | fb450e98-7cf3-32ca-9746-25295074551b | site/ae0c311c...xml | → RecordType Pratica_ANC |
| Site | Scrivania Digitale Supervisore ANC | d72fc841-0f52-4131-8737-6d8e8cd1f319 | site/d72fc841...xml | urlStub: scrivania-digitale-supervisore-anc |
| Page | Home (Supervisore) | 3cdbf54c-0aa9-3cc2-9de3-a3e10ab1fbd5 | site/d72fc841...xml | → BOA_ANC_Interfaccia_DashBoard_Supervisore |
| Page | Pratiche (Supervisore) | ae91a0ea-e0fb-37ac-9e51-a335c8c11d28 | site/d72fc841...xml | → RecordType Pratica_ANC |
| Page | Riassegna Attività | eacafc69-ce20-3097-8a6d-b9b97f740eef | site/d72fc841...xml | → ProcessModel BOA_ANC_ProcessoSupervisore_RiassegnazioneTask |
| Interface Rule | BOA_ANC_Interfaccia_DashBoard_Operatore | _a-..._9097037 | content/9097037.xml | Dashboard Operatore (billboard + azioni + link favoriti) |
| Interface Rule | BOA_ANC_ListaAttivita | _a-..._9098401 | content/9098401.xml | Lista attività con filtri e grid |
| Interface Rule | BOA_ANC_FiltriUtente_ListaAttivita | _a-..._9099170 | content/9099170.xml | Form filtri per lista attività |
| Interface Rule | BOA_ANC_Interfaccia_DashBoard_Supervisore | _a-..._9113190 | content/9113190.xml | Dashboard Supervisore (billboard + grafici + azioni) |
| Interface Rule | BOA_ANC_Intertfaccia_RiassegnazioneTask | _a-..._9115083 | content/9115083.xml | Riassegnazione task (form + grid attività) |
| Interface Rule | BOA_ANC_Task_Lavorazione | _a-..._9087111 | content/9087111.xml | Task lavorazione (sidebar nav + 3 sezioni) |
| Interface Rule | BOA_ANC_Task_MenuLaterale | _a-..._9090228 | content/9090228.xml | Menu laterale navigazione task |
| Interface Rule | BOA_ANC_Sezione_DatiPratica | _a-..._9090504 | content/9090504.xml | Sezione dati pratica (readonly) |
| Interface Rule | BOA_ANC_Task_VerificaDocumenti | _a-..._9092690 | content/9092690.xml | Verifica documenti (2 colonne: dati + allegati) |
| Interface Rule | BOA_ANC_Task_Riepilogo | _a-..._9107408 | content/9107408.xml | Riepilogo esito checklist + note interne |
| Interface Rule | BOA_ANC_Summary | _a-..._9099789 | content/9099789.xml | Summary pratica (3 tab: Pratica/Lavorazione/Esito) |
| Interface Rule | BOA_ANC_Summary_Esito | _a-..._9112571 | content/9112571.xml | Sezione esito SD + note (readonly) |
| Interface Rule | BOA_ANC_Sezione_DatiCliente | _a-..._9093123 | content/9093123.xml | Sezione dati cliente (boxLayout collassabile, readonly) |
| Interface Rule | BOA_ANC_Sezione_IndirizzoResidenza_DatiCliente | _a-..._9093562 | content/9093562.xml | Sezione indirizzo residenza cliente (readonly) |
| Interface Rule | BOA_ANC_Sezione_DatiCarta | _a-..._9094869 | content/9094869.xml | Sezione dati carta bloccata (boxLayout collassabile, readonly) |
| Interface Rule | BOA_ANC_Sezione_Documenti | _a-..._9095458 | content/9095458.xml | Sezione allegati (viewer documenti con download) |
| Interface Rule | BOA_ANC_Contenuti_Section | _a-..._9168934 | content/9168934.xml | Contenuti documento (2 colonne: lista + viewer) |
| Interface Rule | BOA_ANC_Task_CheckList | _a-..._9127320 | content/9127320.xml | Checklist controlli pratica |
| Interface Rule | BOA_ANC_CheckList_Section | _a-..._9120508 | content/9120508.xml | Sezione interna checklist |
| Interface Rule | BOA_ANC_Task_TipizzazioneDocumenti | _a-0000ead1..._10798994 | content/10798994.xml | Tipizzazione documento (dropdown + conferma) |
| Interface Rule | BOA_ANC_Header | _a-..._9077771 | content/9077771.xml | Header pratica/case |
| Interface Rule | BOA_ANC_GraficiPraticheGiornaliere_Section | _a-..._9113035 | content/9113035.xml | Sezione grafici pratiche giornaliere |
| Interface Rule | BOA_ANC_GraficiPraticheGiornaliereLavorate_Section | _a-..._9113078 | content/9113078.xml | Sezione grafici pratiche lavorate |
| Interface Rule | BOA_ANC_GraficiPraticheByStato_Section | _a-..._9113178 | content/9113178.xml | Sezione grafici pratiche per stato |
| Interface Rule | BOA_ANC_Filtri_TaskReportSupervisoreGruppi | _a-..._9115247 | content/9115247.xml | Filtri report supervisore |
| Interface Rule | BOA_ANC_EsitiAttivita | _a-..._9107462 | content/9107462.xml | Esiti attività (card per esito) |
| Record Type | Pratica_ANC | 126d7d57-5922-4aed-b5ee-14b28d796f95 | recordType/126d7d57...xml | List view + Summary view |
| Gruppo UI | BOA ANC All Users | _e-..._2234 | group/ | Ruolo: all users |
| Gruppo UI | BOA ANC Administrators | _e-..._2236 | group/ | Ruolo: site administrator |
| Gruppo UI | Operatore ANC | _e-..._2238 | group/ | Ruolo: site_viewer Operatore |
| Gruppo UI | Supervisore ANC | _e-..._2242 | group/ | Ruolo: site_viewer Supervisore |

---

## STEP 1 — Site & Navigation Structure

### Tabella Site

| Nome Site | URL Stub | Descrizione | Page Count | Accento colore | Nav Style |
|---|---|---|---|---|---|
| Scrivania Digitale Operatore ANC | scrivania-digitale-operatore-anc | Sito per l'operatore di questa applicazione | 3 | #0047BB | TOPBAR |
| Scrivania Digitale Supervisore ANC | scrivania-digitale-supervisore-anc | — | 3 | #0047BB | TOPBAR |

**Configurazione comune ai due site**:
- Logo: `https://www.poste.it/img/1453895043057/2X/logo-poste-italiane.png` (alt: "PosteItaliane")
- `showName: false` (il nome del sito non è mostrato in navbar)
- `buttonShape: SQUARED`, `inputShape: SQUARED`, `dialogShape: SQUARED`
- `buttonLabelCase: UPPERCASE`
- `tasksInSitesVisibility: HIDDEN`
- `loadingBarColor: #0047BB`
- `selectedTabBackgroundColor: #0047BB`

---

### Mappa di Navigazione

```
Scrivania Digitale Operatore ANC (/scrivania-digitale-operatore-anc)
  ├── Home          /scrivania-digitale-operatore-anc/home           [icon: f015 (fa-home)]   [width: WIDE]  → BOA_ANC_Interfaccia_DashBoard_Operatore
  ├── Attività      /scrivania-digitale-operatore-anc/task           [icon: f0ca (fa-list-ul)] [width: WIDE]  → BOA_ANC_ListaAttivita
  └── Pratiche      /scrivania-digitale-operatore-anc/pratiche       [icon: f016 (fa-file)]    [width: WIDE]  → Pratica_ANC (RecordType)

Scrivania Digitale Supervisore ANC (/scrivania-digitale-supervisore-anc)
  ├── Home               /scrivania-digitale-supervisore-anc/home              [icon: f015 (fa-home)]   [width: WIDE]  → BOA_ANC_Interfaccia_DashBoard_Supervisore
  ├── Pratiche           /scrivania-digitale-supervisore-anc/pratiche          [icon: f016 (fa-file)]   [width: WIDE]  → Pratica_ANC (RecordType)
  └── Riassegna Attività /scrivania-digitale-supervisore-anc/riassegna-attivita [icon: f0c0 (fa-group)]  [width: WIDE]  → ProcessModel BOA_ANC_ProcessoSupervisore_RiassegnazioneTask
```

---

### Tabella Pagine

| Nome Page | URL Completa | Icon (hex) | FontAwesome | Visibilità | Page Width | Oggetto UI | Tipo Oggetto |
|---|---|---|---|---|---|---|---|
| Home | /scrivania-digitale-operatore-anc/home | f015 | fa-home | sempre | WIDE | BOA_ANC_Interfaccia_DashBoard_Operatore | Interface Rule |
| Attività | /scrivania-digitale-operatore-anc/task | f0ca | fa-list-ul | sempre | WIDE | BOA_ANC_ListaAttivita | Interface Rule |
| Pratiche | /scrivania-digitale-operatore-anc/pratiche | f016 | fa-file | sempre | WIDE | Pratica_ANC | Record Type |
| Home | /scrivania-digitale-supervisore-anc/home | f015 | fa-home | sempre | WIDE | BOA_ANC_Interfaccia_DashBoard_Supervisore | Interface Rule |
| Pratiche | /scrivania-digitale-supervisore-anc/pratiche | f016 | fa-file | sempre | WIDE | Pratica_ANC | Record Type |
| Riassegna Attività | /scrivania-digitale-supervisore-anc/riassegna-attivita | f0c0 | fa-group | sempre | WIDE | BOA_ANC_ProcessoSupervisore_RiassegnazioneTask | Process Model |

> **Nota**: tutte le page hanno `areUrlParamsEncrypted: true` e `visibilityExpr: fn!true()`.

---

## STEP 2 — Interface Rules Inventory

### Tabella Interface Rules

| Nome Interface Rule | UUID (breve) | Tipo Schermata | Layout Radice SAIL | Associata a | Note |
|---|---|---|---|---|---|
| BOA_ANC_Interfaccia_DashBoard_Operatore | _9097037 | Dashboard (View) | `billboardLayout_v1` + `columnsLayout` | Page Home (Operatore) | Banner con avatar, contatori, box Azioni, box Link Favoriti |
| BOA_ANC_ListaAttivita | _9098401 | List | `checkboxField` + `boxLayout`(gridField) | Page Attività | Filtri + checkbox + grid filtri salvati + grid attività principali |
| BOA_ANC_FiltriUtente_ListaAttivita | _9099170 | Form (filtri) | `sideBySideLayout` + `ButtonLayout` | Embedded in BOA_ANC_ListaAttivita | Filtri: Stato, Tipo Pratica, Pratica N., Nome Attività, Assegnatari, Utente in carico |
| BOA_ANC_Interfaccia_DashBoard_Supervisore | _9113190 | Dashboard (View) | `billboardLayout_v1` + `columnsLayout` | Page Home (Supervisore) | Come Operatore + sezioni grafici pratiche |
| BOA_ANC_Intertfaccia_RiassegnazioneTask | _9115083 | Form + List | `boxLayout` + `gridField_v1` | Page Riassegna Attività (via processo) | Sezione dettagli riassegnazione + grid attività selezionabili + azioni |
| BOA_ANC_Task_Lavorazione | _9087111 | Form (Task) | `columnsLayout` (sidebar + content area) | Task process (BOA_ANC_AvvioAttivita) | 3 sezioni: Dati Pratica / Verifica Documenti / Riepilogo; sidebar con menu laterale |
| BOA_ANC_Task_MenuLaterale | _9090228 | Component (nav) | `forEach`(cardLayout) | Embedded in BOA_ANC_Task_Lavorazione | Sidebar navigazione verticale collassabile |
| BOA_ANC_Sezione_DatiPratica | _9090504 | View (sezione) | `boxLayout` | Embedded in Task_Lavorazione (step 1) e BOA_ANC_Summary | Dati pratica readonly: date, stato, codice cliente, CF, canale, esito SD |
| BOA_ANC_Task_VerificaDocumenti | _9092690 | View (sezione) | `columnsLayout` (2 col) | Embedded in Task_Lavorazione (step 2) | Col-left: dati cliente/carta/contenuti/checklist; col-right: allegati |
| BOA_ANC_Task_Riepilogo | _9107408 | View + Form | `richTextDisplayField` + `columnsLayout` | Embedded in Task_Lavorazione (step 3) | Esito controlli + motivazioni + textarea Note Interne (se esito negativo) |
| BOA_ANC_Summary | _9099789 | View (Summary) | `sideBySideLayout` (tab links) + `cardLayout` | Record Type summary view / azioni correlate | 3 tab dinamici: Dati Pratica / Dati Lavorazione / Esito |
| BOA_ANC_Summary_Esito | _9112571 | View (sezione) | `boxLayout` | Embedded in BOA_ANC_Summary (tab Esito) | Esito SD + data esito + box Note (se presenti) |
| BOA_ANC_Sezione_DatiCliente | _9093123 | View (sezione) | `boxLayout` (collassabile) | Embedded in BOA_ANC_Task_VerificaDocumenti | Dati anagrafici cliente: nome, cognome, CF, date, telefono + sezione residenza |
| BOA_ANC_Sezione_IndirizzoResidenza_DatiCliente | _9093562 | View (sezione) | `boxLayout` (collassabile) | Embedded in BOA_ANC_Sezione_DatiCliente | Indirizzo di residenza cliente (readonly) |
| BOA_ANC_Sezione_DatiCarta | _9094869 | View (sezione) | `boxLayout` (collassabile) | Embedded in BOA_ANC_Task_VerificaDocumenti | Dati carta bloccata: tipo, numero, intestazione (readonly) |
| BOA_ANC_Sezione_Documenti | _9095458 | View (sezione) | `boxLayout` + `forEach` | Embedded in BOA_ANC_Task_VerificaDocumenti (col-right) | Allegati della pratica con viewer inline e link download |
| BOA_ANC_Contenuti_Section | _9168934 | View (sezione) | `columnsLayout` (2 col) | Embedded in Task_VerificaDocumenti | Contenuti denuncia con visualizzazione documento |
| BOA_ANC_Task_CheckList | _9127320 | View/Form (sezione) | `BOA_ANC_CheckList_Section` | Embedded in Task_VerificaDocumenti | Checklist controlli: mostra "Nessun controllo" se vuota, altrimenti BOA_ANC_CheckList_Section |
| BOA_ANC_CheckList_Section | _9120508 | Form (sezione) | non disponibile (ref a IR nested) | Embedded in BOA_ANC_Task_CheckList | Sezione dettaglio voci checklist |
| BOA_ANC_Task_TipizzazioneDocumenti | _10798994 | Form (Task) | `columnsLayout` (2 col) | Task process (BOA_ANC_Processo_TipizzaDoc) | Col-left: dropdown tipo documento + bottone Conferma; col-right: preview documenti |
| BOA_ANC_Header | _9077771 | Component (header) | non analizzato in dettaglio | Embedded in BOA_ANC_Summary | Header pratica con ID case e informazioni |
| BOA_ANC_GraficiPraticheGiornaliere_Section | _9113035 | View (sezione) | non disponibile | Embedded in Dashboard Supervisore | Grafici pratiche giornaliere |
| BOA_ANC_GraficiPraticheGiornaliereLavorate_Section | _9113078 | View (sezione) | non disponibile | Embedded in Dashboard Supervisore | Grafici pratiche giornaliere lavorate |
| BOA_ANC_GraficiPraticheByStato_Section | _9113178 | View (sezione) | non disponibile | Embedded in Dashboard Supervisore | Grafici pratiche per stato |
| BOA_ANC_Filtri_TaskReportSupervisoreGruppi | _9115247 | Form (filtri) | `sideBySideLayout` | Embedded in BOA_ANC_Intertfaccia_RiassegnazioneTask | Filtri: data creazione, data scadenza, assegnatario, gruppo, pratica |
| BOA_ANC_EsitiAttivita | _9107462 | View (componente) | `forEach`(cardLayout) | Embedded in BOA_ANC_Task_Riepilogo | Card per ogni esito checklist |
| Pratica_ANC | 126d7d57 | List + Detail (Record) | `recordGridField` | Page Pratiche (entrambi i siti) | List: griglia pratiche sortable; Summary: BOA_ANC_Summary |

---

## STEP 3 — Component Inventory per Schermata

### BOA_ANC_Interfaccia_DashBoard_Operatore

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `billboardLayout_v1` | Hero Banner | — | display-only | sempre | — |
| `imageField_v1` (style: AVATAR) | Avatar utente | — | display-only | sempre | — |
| `richTextDisplayField` | Testo nome utente + ruolo | — | display-only | sempre | — |
| `richTextDisplayField` | "N° Attività" (count) | N° Attività | display-only | sempre | — |
| `richTextDisplayField` | "Pratiche Attive" (count, ACCENT) | Pratiche Attive | display-only | sempre | — |
| `richTextDisplayField` | "Pratiche Chiuse" (count, #008000) | Pratiche Chiuse | display-only | sempre | — |
| `boxLayout` | Box "Azioni" | Azioni | display-only | sempre | — |
| `richTextIcon` + `richTextItem_v1` | Link azione dinamica | (per ogni azione) | display-only | sempre | [start-process] → avvia processo azione |
| `boxLayout` | Box "Link Favoriti" | Link Favoriti | display-only | sempre | — |
| `richTextIcon` (remove) | Icona rimozione link | — | display-only | `and(local!utenteLinkFavoriti <> "", not(local!gestisciLinks))` | [dynamic-link] rimozione link |
| `richTextIcon` (edit) | Icona modifica link | — | display-only | `and(local!utenteLinkFavoriti <> "", not(local!gestisciLinks))` | [dynamic-link] apre form modifica |
| `richTextItem_v1` | Link favorito (testo cliccabile) | (titoloLink) | display-only | `and(local!utenteLinkFavoriti <> "", not(local!gestisciLinks))` | [safeLink] → url favorito |
| `richTextDisplayField` | "Nessun link presente" | — | display-only | `and(local!utenteLinkFavoriti = "", not(local!gestisciLinks))` | — |
| `richTextIcon` (plus-square-o) | Aggiungi nuovo link | — | display-only | `not(local!gestisciLinks)` | [dynamic-link] apre form aggiunta |
| `cardLayout` (showBorder: false) | Card form gestione link | — | display-only | `local!gestisciLinks = true` | — |
| `textField` | "Titolo Link" | Titolo Link | editable | sempre (dentro card) | salvataggio in local!gestionelinkFavorito.titoloLink |
| `textField` | "Link" | Link | editable | sempre (dentro card) | validation: "Inserire un link valido." |
| `dropdownField_v1` | "Tipo Link" | Tipo Link | editable | sempre (dentro card) | options: Interno, Esterno, Legacy |
| `ButtonWidget` | "Esci" | Esci | enabled | sempre (dentro card) | [save] reset form + chiude card |
| `ButtonWidget` | "Salva" | Salva | enabled | sempre (dentro card) | [writeToDataStore] salva link favorito |

### BOA_ANC_FiltriUtente_ListaAttivita

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `dropdownField_v1` | Dropdown | Stato | editable | sempre | choiceLabels: "In Coda"/"In Lavorazione"; choiceValues: 0/1; placeholder: "Tutti" |
| `multipleDropdownField_v1` | Multi-select dropdown | Tipo Pratica | editable (disabled: sempre) | sempre | disabled: `true` (hardcoded in export) |
| `textField` | Text input | Pratica N. | editable | sempre | — |
| `textField` | Text input | Nome Attività | editable | sempre | — |
| `pickerFieldUsersAndGroups` | User/Group picker | Assegnatari | editable | sempre | maxSelections: 1; groupFilter: BOA ANC Operatori; disabled: `isnull(ri!tipoProcesso)` |
| `pickerFieldUsers` | User picker | Utente in carico | editable | sempre | maxSelections: 1; groupFilter: BOA ALL Users |
| `ButtonWidget` | Button PRIMARY (style: DESTRUCTIVE) | Applica Filtri | enabled | sempre | toggle pagingInfo e applicaFiltri |
| `ButtonWidget` | Button PRIMARY | Applica e Salva Filtri | enabled | sempre | [startProcess] BOA_ANC_SalvaFiltriUtente + applica |

### BOA_ANC_ListaAttivita

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `BOA_ANC_FiltriUtente_ListaAttivita` | Form filtri (embedded) | — | editable | sempre | — |
| `checkboxField` | Checkbox | "Visualizza le attività a me assegnate" | editable | sempre | disabled: `isnull(local!myTasksTipoProcesso)` |
| `boxLayout` (gridField_v1) | Box con griglia | "Ultimi N Filtri Salvati" | display-only | sempre | istruzioni: "Selezionare un filtro per utilizzarlo e cliccare su applica filtri" |
| `gridField_v1` (filtri salvati) | Grid/Table | — | readonly/selectable | sempre | selectionStyle: ROW_HIGHLIGHT; colonne: Stato, Tipo Pratica, Pratica N., Nome Attività, Data Scadenza Da, Data Scadenza A, Assegnatario, Utente in carico |
| `gridField_v1` (attività principali) | Grid/Table attività | — | readonly/selectable | sempre | pageSize: 8; sort: c1 desc, c10 desc, c3 desc; azione riga → apertura task |

> **Grid Filtri Salvati — Colonne** (in ordine): Stato | Tipo Pratica | Pratica N. | Nome Attività | Data Scadenza Da | Data Scadenza A | Assegnatario | Utente in carico

### BOA_ANC_Sezione_DatiPratica

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `boxLayout` (style: BOA_STYLE_POSTE) | Box container | "Dati Pratica" | display-only | sempre | — |
| `textField` | Text | Data Apertura (formato dd/mm/yyyy HH:mm:ss) | readonly | sempre | — |
| `textField` | Text | Data Ultima Modifica (formato dd/mm/yyyy HH:mm:ss) | readonly | sempre | — |
| `textField` | Text | Stato | readonly | sempre | — |
| `textField` | Text | Codice Cliente | readonly | sempre | default: " " se null |
| `textField` | Text | Codice Fiscale | readonly | sempre | default: "Codice Fiscale non indicato" se null |
| `textField` | Text | Canale | readonly | sempre | — |
| `sideBySideItem` con `textField` | Text | Data Chiusura | readonly | `showWhen: isNotNullOrEmpty(pratica.dataChiusura)` | — |
| `sideBySideItem` con `textField` | Text | Esito SD | readonly | `showWhen: and(isNotNullOrEmpty(pratica.esitoSD), ri!readOnly)` | default: "Nessun esito ancora definito" se null |

### BOA_ANC_Sezione_DatiCliente

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `boxLayout` (isCollapsible: true) | Box collassabile | "Dati Cliente" | display-only | sempre | — |
| `textField` | Text | Nome | readonly | sempre | — |
| `textField` | Text | Cognome | readonly | sempre | — |
| `textField` | Text | Sesso | readonly | sempre | default: "N/A" se null |
| `textField` | Text | Codice Fiscale | readonly | sempre | — |
| `textField` | Text | Data di Nascita (dd/mm/yyyy) | readonly | sempre | — |
| `textField` | Text | Comune di Nascita | readonly | sempre | — |
| `textField` | Text | Provincia di Nascita | readonly | sempre | — |
| `textField` | Text | Nazione di Nascita | readonly | sempre | — |
| `textField` | Text | Telefono | readonly | sempre | — |
| `textField` | Text | Cellulare | readonly | sempre | — |
| `BOA_ANC_Sezione_IndirizzoResidenza_DatiCliente` | View sezione embedded | (indirizzo residenza) | readonly | sempre | — |

### BOA_ANC_Sezione_DatiCarta

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `boxLayout` (isCollapsible: true) | Box collassabile | "Dati Carta Bloccata" | display-only | sempre | — |
| `textField` | Text | Tipo Carta | readonly | sempre | — |
| `textField` | Text | Numero Carta | readonly | sempre | — |
| `textField` | Text | Intestazione Carta | readonly | sempre | — |

### BOA_ANC_Sezione_Documenti

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `cardLayout` (style: INFO) | Card informativa | — | display-only | `isNullOrEmpty(ri!contenuti)` | Messaggio: "Nessun contenuto associato alla pratica" |
| `boxLayout` (isCollapsible: true) | Box allegati | label = `ri!documento.descrizione` (o "Documento") | display-only | `isNotNullOrEmpty(ri!contenuti)` | — |
| `forEach` → `boxLayout` (per ogni contenuto) | Box per file | `fv!item.nomeFile` | display-only | sempre | — |
| `richTextItem_v1` + `richTextIcon` (file-download) | Link download | "Non è stato possibile scaricare... QUI" | display-only | `not(UTILS_VerificaDocumento(fv!item.idDocAppian))` | [dynamicLink → startProcess BOA_ANC_ScaricaSingoloDoc] |
| `BOA_ANC_DocumentViewer_Section` | Document viewer | — | readonly | `UTILS_VerificaDocumento(fv!item.idDocAppian)` | visualizzazione documento inline |

### BOA_ANC_Task_VerificaDocumenti

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `ButtonWidget` (PRIMARY) | Button | "nascondi allegati" / "mostra allegati" | enabled | sempre | [save] toggle local!showAllegati; icon: eye-slash/eye |
| `ButtonWidget` (SECONDARY) | Button | "nascondi sezione" / "mostra sezione" | enabled | sempre | [save] toggle local!showSection; icon: eye-slash/eye |
| `columnLayout` (col-left) | Layout | — | display-only | `local!showSection = true` | — |
| `BOA_ANC_Sezione_DatiCliente` | Sezione embedded | "Dati Cliente" | readonly | dentro col-left | — |
| `BOA_ANC_Sezione_DatiCarta` | Sezione embedded | "Dati Carta Bloccata" | readonly | dentro col-left | — |
| `BOA_ANC_Contenuti_Section` | Sezione embedded | (contenuti denuncia) | readonly | dentro col-left | — |
| `BOA_ANC_Task_CheckList` | Sezione embedded | (checklist) | editable (readOnly: false) | dentro col-left | — |
| `columnLayout` (col-right) | Layout | — | display-only | `local!showAllegati = true` | — |
| `BOA_ANC_Sezione_Documenti` | Sezione embedded | (allegati) | readonly | dentro col-right | — |

### BOA_ANC_Task_Riepilogo

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `richTextDisplayField` | Display icona + testo esito | — | display-only | sempre | Icona check-circle/times-circle + testo "Controlli su {documento} superati con esito POSITIVO/NEGATIVO"; color: POSITIVE/NEGATIVE |
| `cardLayout` | Card motivazioni | — | display-only | `isNotNullOrEmpty(local!motivazioni)` | Testo "Con le seguenti motivazioni:" + lista puntata |
| `columnsLayout` + `forEach` | Grid card esiti | — | display-only | sempre | Una card per ogni esito (icona + descrizione colorata) |
| `columnsLayout` | Layout note interne | — | display-only | `and(isNotNullOrEmpty(local!esitoControlli), esitoSD != BOA_ANC_ESITI_SD[1])` | — |
| `paragraphField` | Textarea | Note Interne | editable | `esitoSD != primo esito positivo` | binding: ri!nota.testoNota |

### BOA_ANC_Task_Lavorazione

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `columnsLayout` (spacing: SPARSE, showDividers: true) | Layout 2 colonne | — | — | sempre | — |
| col-left (NARROW / EXTRA_NARROW) | Sidebar | — | — | sempre | width dipende da `local!collapseNav` |
| `BOA_ANC_Task_MenuLaterale` | Nav sidebar | 3 voci: Dati Pratica / Verifica Documento / Riepilogo | — | sempre | [dynamicLink] → cambia `local!activeCollapsibleNavSection` |
| col-right (content) | Contenuto attivo | — | — | sempre | `choose(local!activeCollapsibleNavSection, ...)` |
| `BOA_ANC_Sezione_DatiPratica` | View sezione | "Dati Pratica" | readonly | `local!activeCollapsibleNavSection = 1` | — |
| `BOA_ANC_Task_VerificaDocumenti` | View sezione | "Verifica Documento" | `readOnly: isNotNullOrEmpty(ri!pratica.esitoSD)` | `local!activeCollapsibleNavSection = 2` | — |
| `BOA_ANC_Task_Riepilogo` | View sezione | "Riepilogo" | editable (note) | `local!activeCollapsibleNavSection = 3` | — |
| `ButtonWidget` | Button PRIMARY | "chiudi pratica" | enabled; submit: true | `local!activeCollapsibleNavSection = 3` | [startProcess] BOA_ANC_SalvataggioDati → salva e chiude pratica |
| `ButtonWidget` | Button PRIMARY | "Salva e prosegui" | disabled: `isNotNullOrEmpty(ri!pratica.esitoSD)`; submit: false | `local!activeCollapsibleNavSection = 2` | [startProcess] BOA_ANC_SalvataggioDati → salva dati + va a step 3 |
| `ButtonWidget` | Button SECONDARY | "Modifica" | enabled | — | [startProcess] BOA_ANC_SalvataggioDati → reset esitoSD e riabilita lavorazione |

### BOA_ANC_Task_TipizzazioneDocumenti

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `cardLayout` (style: INFO) | Card informativa | — | display-only | `isNullOrEmpty(ri!contenuti)` | Messaggio: "Nessun contenuto associato alla pratica" |
| `columnsLayout` | Layout 2 colonne | — | — | `isNotNullOrEmpty(ri!contenuti)` | — |
| `boxLayout` col-left | Box | "Tipizzazione Documento" | — | — | — |
| `cardLayout` (style: INFO) | Card info istruzioni | — | display-only | sempre | Testo istruzioni tipizzazione |
| `dropdownField_v1` | Dropdown | Tipo Documento | editable | sempre | required: true; choiceLabels: local!tipiDocAttivi.descrizione; choiceValues: local!tipiDocAttivi.codiceDocId; placeholder: "--- Selezionare il tipo documento ---"; ref: BOA_ANC_GetTipiDocumentoAttivi |
| `ButtonWidget` | Button PRIMARY | "Conferma" | disabled: `isNullOrEmpty(local!tipoDocSelected)`; submit: false | sempre | confirmHeader: "ATTENZIONE"; confirmMessage: "E' stato selezionato {tipo} come tipologia..."; [startProcess] BOA_ANC_Processo_TipizzaDoc |
| `boxLayout` col-right | Box | label = `ri!documento.descrizione` | — | — | Preview contenuti documento (forEach) |

### BOA_ANC_Summary

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `BOA_ANC_Header` | Header pratica | — | display-only | sempre | Mostra ID case e info pratica |
| `sideBySideLayout` (icone tab) | Tab selettore (icone) | — | display-only | sempre | 3 icone: file-o / file-image-o / legal (colore ACCENT) |
| `sideBySideLayout` (label tab) | Tab selettore (label) | — | display-only | sempre | 3 link: "Dati Pratica" / "Dati Lavorazione" / "Esito" (dynamicLink → local!selezione) |
| `cardLayout` (showBorder: false) | Container tab Pratica | — | — | `local!selezione = "Pratica"` | — |
| `BOA_ANC_Sezione_DatiPratica` | View embedded | "Dati Pratica" | readonly | `local!selezione = "Pratica"` | — |
| `cardLayout` (showBorder: false) | Container tab Lavorazione | — | — | `local!selezione = "Video"` | — |
| `BOA_ANC_Task_VerificaDocumenti` | View embedded | (verifica documenti readonly) | readonly | `local!selezione = "Video"` | — |
| `cardLayout` (showBorder: false) | Container tab Esito | — | — | `local!selezione = "Esito"` | — |
| `BOA_ANC_Summary_Esito` | View embedded | (esito SD + note) | readonly | `local!selezione = "Esito"` | — |

### BOA_ANC_Summary_Esito

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `boxLayout` | Box container | "Esito Verifiche Back Office" | display-only | sempre | — |
| `textField` | Text | Esito Scrivania Digitale | readonly | sempre | default: "La pratica non è stata ancora esitata" se null |
| `dateField` | Date | Data Esito Scrivania Digitale (dd/mm/yyyy HH:mm) | readonly | `showWhen: isNotNullOrEmpty(ri!pratica.dataEsitoSD)` | — |
| `boxLayout` (nested) | Box | "Note" | display-only | `showWhen: isNotNullOrEmpty(ri!nota)` | — |
| `textField` | Text | Data (dd/mm/yyyy HH:mm:ss) | readonly | dentro box Note | — |
| `textField` | Text | Operatore | readonly | dentro box Note | — |
| `paragraphField` | Textarea | Testo | readonly | dentro box Note | — |

### BOA_ANC_Intertfaccia_RiassegnazioneTask

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Interazione |
|---|---|---|---|---|---|
| `boxLayout` | Box | "Dettagli riassegnazione" | display-only | sempre | — |
| `richTextDisplayField` | Display | "Processo: {BOA_ANC_NOMEAPPLICAZIONE}" | display-only | sempre | — |
| `radioButtonField` | Radio button | "Scegli Tipologia di Riassegnazione" | editable | `not(isnull(local!processo))` | required: true; options: "Riassegna Attività al Gruppo Operatore" (value: BOA ANC Operatori) / "Riassegna Attività a Utenti" (value: "Utenti") |
| `pickerFieldUsers` | User picker | "Inserisci utenti a cui assegnare le attività" | editable | `local!tipoRiassegnazione = "Utenti"` | required: true; groupFilter: BOA ANC Operatori |
| `boxLayout` | Box | "Le attività dei miei processi" | — | `not(isnull(local!processo))` | — |
| `BOA_ANC_Filtri_TaskReportSupervisoreGruppi` | Form filtri embedded | — | editable | dentro box | — |
| `gridField_v1` | Grid/Table | — | readonly/selectable | dentro box | pageSize: 8; selectionRequired: true |
| `ButtonWidget` (PRIMARY) | Button | "Riassegna Attività" (presumibile) | enabled | — | [startProcess] BOA_ANC_Single_Riassegnazione o BOA_ANC_ProcessoSupervisore_RiassegnazioneTask |

**Grid attività riassegnazione — Colonne** (in ordine):
Processo | Pratica | Nome Attività | Assegnatario | Owner | Data Assegnazione | Data Presa in carico | ~~Data Scadenza~~ (showWhen: false) | Stato (icona)

### Pratica_ANC — Record Type List View

| Componente SAIL | Tipo UX | Label | Stato | ShowWhen | Note |
|---|---|---|---|---|---|
| `recordGridField` | Grid/Table | — | readonly | sempre | Griglia principale lista pratiche |

**Colonne List View** (in ordine di dichiarazione):

| Colonna | showWhen | Tipo | Note |
|---|---|---|---|
| Id | false | number | — |
| Pratica N. | sempre | recordLink | link → record detail |
| Canale | false | text | — |
| Codice Fiscale | sempre | text | — |
| Codice Cliente | sempre | text | — |
| Data Apertura | sempre | date | formato default |
| Data Ultima Modifica | sempre | date | formato default |
| Data Chiusura | sempre | date | formato default |
| Data Inserimento Richiesta | sempre | date | formato default |
| Esito SD | sempre | text | — |
| Operatore | sempre | text | — |
| Stato | sempre | text | — |
| Data Scadenza | false | date | — |
| Segnalazioni | sempre | richTextIcon | icona visuale |

---

## STEP 3b — Screen Specification per Schermata

---

### SCREEN: BOA_ANC_Interfaccia_DashBoard_Operatore

```
SCREEN: BOA_ANC_Interfaccia_DashBoard_Operatore
TYPE:   Dashboard (View)
URL:    /scrivania-digitale-operatore-anc/home
WIDTH:  WIDE
HEADER: (nessun header formLayout — billboard come hero)

[BILLBOARD BANNER]  height: MEDIUM  — background image (documento 59228)
  overlay: barOverlay style: LIGHT  position: BOTTOM
  COL-LEFT:
    ■ [avatar] immagine utente loggato  [image, style: AVATAR]  display-only
    ■ [text] {firstName} {lastName}     size: LARGE, style: STRONG  display-only
    ■ [text] {ruolo}                    style: EMPHASIS            display-only
  COL-CENTER: (vuota)
  COL-RIGHT:
    ■ N° Attività     [richText display]  text: total count attività  size: MEDIUM  display-only
    ■ Pratiche Attive [richText display]  text: count pratiche aperte  color: ACCENT  size: MEDIUM  display-only
    ■ Pratiche Chiuse [richText display]  text: count pratiche chiuse  color: #008000  size: MEDIUM  display-only

[BODY — 2 COLONNE]
  COL-LEFT:
    ■ [BOX] "Azioni"  style: BOA_STYLE_POSTE
        ■ (forEach sulle azioni assegnate al gruppo utente)
            ■ [richTextIcon]  icon: {fv!item.icona}  color: ACCENT  link: startProcessLink → {processo azione}
            ■ [richTextItem]  text: {fv!item.nomeazione}  color: ACCENT  link: startProcessLink → {processo azione}

    ■ [BOX] "Link Favoriti"  style: BOA_STYLE_POSTE
        ~ ■ (lista link) [richTextIcon remove] + [richTextIcon edit] + [icon tipo] + [richTextItem link]
              showWhen: and(local!utenteLinkFavoriti <> "", not(local!gestisciLinks))
        ~ ■ "Nessun link presente"  style: EMPHASIS
              showWhen: and(local!utenteLinkFavoriti = "", not(local!gestisciLinks))
        ~ ■ [richTextIcon plus-square-o caption="Aggiungi nuovo link"]  align: RIGHT
              showWhen: not(local!gestisciLinks)
        ~ [CARD form gestione link]  showBorder: false
              showWhen: local!gestisciLinks = true
              □ Titolo Link *  [text]  editable  required
              □ Link         *  [text]  editable  required
                  validation: "Inserire un link valido."  when: not(isValidURL(value))
              □ Tipo Link    *  [dropdown]  editable  required
                  options: [{"label": "Interno", "value": "Interno"}, {"label": "Esterno", "value": "Esterno"}, {"label": "Legacy", "value": "Legacy"}]

FOOTER / ACTIONS (dentro card form Link Favoriti):
  [PRIMARY]  "Esci"   → reset form + chiude card
  [PRIMARY]  "Salva"  → writeToDataStore + reload
```

---

### SCREEN: BOA_ANC_ListaAttivita

```
SCREEN: BOA_ANC_ListaAttivita
TYPE:   List
URL:    /scrivania-digitale-operatore-anc/task
WIDTH:  WIDE
HEADER: (nessun header — pagina aperta)

[FILTRI — BOA_ANC_FiltriUtente_ListaAttivita]
  ROW 1 (sideBySideLayout):
    □ Stato          [dropdown]  editable  placeholder: "Tutti"
        options: [{"label": "In Coda", "value": 0}, {"label": "In Lavorazione", "value": 1}]
    □ Tipo Pratica   [multi-dropdown]  disabled (sempre disabilitato)  placeholder: "Tutti"
        options: ref: BOA_ANC_NOMEAPPLICAZIONE (chiave processo)
    □ Pratica N.     [text]      editable
    □ Nome Attività  [text]      editable

  ROW 2 (sideBySideLayout):
    □ ~~Data Scadenza DA~~ (commentata, non visibile)
    □ ~~Data Scadenza A~~  (commentata, non visibile)
    □ Assegnatari    [user-group picker]  editable  maxSelections: 1
        groupFilter: BOA ANC Operatori
        disabled: isnull(ri!tipoProcesso)
    □ Utente in carico [user picker]  editable  maxSelections: 1
        groupFilter: BOA ALL Users

  ACTIONS FILTRI:
    [DESTRUCTIVE]  "Applica Filtri"        → toggle local!applicaFiltri + reset paging
    [PRIMARY]      "Applica e Salva Filtri" → startProcess BOA_ANC_SalvaFiltriUtente + applica

[CHECKBOX]
  □ "Visualizza le attività a me assegnate"  [checkbox]  editable
      disabled: isnull(local!myTasksTipoProcesso)
      choiceValues: loggedInUser()

[SECTION BOX: "Ultimi {BOA_ANC_NUMBER_MAXFILTRITOSHOW} Filtri Salvati"]
  instructions: "Selezionare un filtro per utilizzarlo e cliccare su applica filtri"
  emptyGridMessage: "Nessuna Filtro Salvato"
  ■ [grid]  readonly/selectable  selectionStyle: ROW_HIGHLIGHT
      Columns (in order):
        Stato           [text]
        Tipo Pratica    [text]
        Pratica N.      [text]
        Nome Attività   [text]
        Data Scadenza Da [date]
        Data Scadenza A  [date]
        Assegnatario    [text/group]
        Utente in carico [text]
      Row action: [select] → popola filtri con valori riga selezionata

[GRID ATTIVITÀ PRINCIPALI]  (dati da report process analytics)
  pageSize: 8
  sort: Stato desc, Tipo Pratica desc, Data Assegnazione desc
  Columns: (estratte da report — campi c0-c18):
    Nome Attività    [text/richText]
    Stato            [text/number]   c1
    Tipo Pratica     [text]          c8/c10
    Pratica N.       [text]          c5
    Assegnatario     [text]          c6
    Utente in carico [text]
    Data Scadenza    [date]          c11
  Row action: [navigate] → apertura task di lavorazione
```

---

### SCREEN: BOA_ANC_Interfaccia_DashBoard_Supervisore

```
SCREEN: BOA_ANC_Interfaccia_DashBoard_Supervisore
TYPE:   Dashboard (View)
URL:    /scrivania-digitale-supervisore-anc/home
WIDTH:  WIDE

[BILLBOARD BANNER]  height: MEDIUM  backgroundColor: #FFEC00
  overlay: barOverlay style: LIGHT
  (struttura identica al Dashboard Operatore: avatar + nome + ruolo Supervisore + contatori)
  ■ N° Attività     [richText display]  display-only
  ■ Pratiche Attive [richText display]  color: ACCENT  display-only
  ■ Pratiche Chiuse [richText display]  color: #008000  display-only
    (nota: backgroundColor billboard giallo #FFEC00 distingue visivamente la dashboard Supervisore)

[BODY — struttura da completare con sezioni grafici]
  ■ [BOX] "Azioni"  (stessa struttura dell'Operatore)
  ■ [BOA_ANC_GraficiPraticheGiornaliere_Section]   grafici pratiche giornaliere
  ■ [BOA_ANC_GraficiPraticheGiornaliereLavorate_Section]  grafici pratiche lavorate
  ■ [BOA_ANC_GraficiPraticheByStato_Section]       grafici pratiche per stato
```

---

### SCREEN: BOA_ANC_Task_Lavorazione

```
SCREEN: BOA_ANC_Task_Lavorazione
TYPE:   Form (Task multi-step)
URL:    (task aperto da BOA_ANC_ListaAttivita o avvio attività)
WIDTH:  WIDE  (layout columnsLayout spacing: SPARSE)

LAYOUT: 2 colonne con divider
  COL-LEFT (sidebar)  width: NARROW (espansa) / EXTRA_NARROW (collassata)
    [BOA_ANC_Task_MenuLaterale]  sidebar navigazione verticale
      3 voci navigate:
        [1] icon: "briefcase"           "Dati Pratica"
        [2] icon: "check-square-o"      "Verifica Documento"
        [3] icon: "address-card-o"      "Riepilogo"
            ~ showWhen (voce 3 cliccabile): isNotNullOrEmpty(ri!pratica.esitoSD)
      Voce attiva evidenziata (style: ACCENT)
      [richTextIcon angle-double-left/right]  espandi/comprimi sidebar

  COL-RIGHT (content area)  — contenuto cambia in base a voce attiva

  --- TAB 1: Dati Pratica (local!activeCollapsibleNavSection = 1) ---
  [BOA_ANC_Sezione_DatiPratica]  readOnly: false
    ■ "Dati Pratica"  [boxLayout]
    ■ Data Apertura          [text]  readonly  (dd/mm/yyyy HH:mm:ss)
    ■ Data Ultima Modifica   [text]  readonly  (dd/mm/yyyy HH:mm:ss)
    ■ Stato                  [text]  readonly
    ■ Codice Cliente         [text]  readonly
    ■ Codice Fiscale         [text]  readonly
    ■ Canale                 [text]  readonly
    ~ ■ Data Chiusura         [text]  readonly  showWhen: isNotNullOrEmpty(pratica.dataChiusura)
    ~ ■ Esito SD              [text]  readonly  showWhen: and(isNotNullOrEmpty(pratica.esitoSD), ri!readOnly)

  --- TAB 2: Verifica Documento (local!activeCollapsibleNavSection = 2) ---
  [BOA_ANC_Task_VerificaDocumenti]  readOnly: isNotNullOrEmpty(ri!pratica.esitoSD)
    [BUTTON BAR TOGGLE]
      [PRIMARY]   "nascondi allegati" / "mostra allegati"   icon: eye-slash/eye
      [SECONDARY] "nascondi sezione" / "mostra sezione"     icon: eye-slash/eye
    COL-LEFT  showWhen: local!showSection
      [BOA_ANC_Sezione_DatiCliente]     "Dati Cliente"  (collassabile)
      [BOA_ANC_Sezione_DatiCarta]       "Dati Carta Bloccata"  (collassabile)
      [BOA_ANC_Contenuti_Section]       (contenuti denuncia)
      [BOA_ANC_Task_CheckList]          readOnly: false
    COL-RIGHT  showWhen: local!showAllegati
      [BOA_ANC_Sezione_Documenti]       (allegati)

  --- TAB 3: Riepilogo (local!activeCollapsibleNavSection = 3) ---
  [BOA_ANC_Task_Riepilogo]
    ■ [richTextIcon + richTextItem]  esito POSITIVO/NEGATIVO  color: POSITIVE/NEGATIVE  size: MEDIUM
    ~ [CARD motivazioni]  showWhen: isNotNullOrEmpty(motivazioni)
        ■ "Con le seguenti motivazioni:"  style: STRONG
        ■ [bulleted list] motivazioni
    ■ [columnsLayout forEach] card per ogni esito checklist
    ~ COL-LEFT [paragraphField]  "Note Interne"  editable
        showWhen: and(isNotNullOrEmpty(esitoControlli), esitoSD != primoEsitoPositivo)

FOOTER / ACTIONS:
  ~ [PRIMARY/SUBMIT]  "chiudi pratica"    showWhen: local!activeCollapsibleNavSection = 3
      validate: true; loadingIndicator: true
      → startProcess BOA_ANC_SalvataggioDati (chiude pratica)
  ~ [PRIMARY]  "Salva e prosegui"         showWhen: local!activeCollapsibleNavSection = 2
      disabled: isNotNullOrEmpty(ri!pratica.esitoSD); validate: true; loadingIndicator: true
      → startProcess BOA_ANC_SalvataggioDati (salva + passa a tab 3)
  [SECONDARY]  "Modifica"                 (sempre; loadingIndicator: true)
      → startProcess BOA_ANC_SalvataggioDati (reset esitoSD → riabilita lavorazione)
```

---

### SCREEN: BOA_ANC_Task_TipizzazioneDocumenti

```
SCREEN: BOA_ANC_Task_TipizzazioneDocumenti
TYPE:   Form (Task)
URL:    (task aperto da processo BOA_ANC_Processo_TipizzaDoc)
WIDTH:  WIDE

~ [CARD INFO]  showWhen: isNullOrEmpty(ri!contenuti)  style: INFO
    ■ icon: info-circle  color: ACCENT
    ■ "Nessun contenuto associato alla pratica"  style: STRONG  color: SECONDARY

~ [2 COLONNE]  showWhen: isNotNullOrEmpty(ri!contenuti)
  COL-LEFT:
    ■ [BOX] "Tipizzazione Documento"  style: BOA_STYLE_POSTE
        [CARD INFO istruzioni]  style: INFO  shape: SEMI_ROUNDED
          ■ "Prima di procedere con la lavorazione della pratica è necessario definire la tipologia
             di documento associato. Nell'eventualità non fosse stato possibile scaricare il contenuto,
             selezionare comunque un tipo di documento e chiudere la pratica con esito negativo."
             color: SECONDARY  size: MEDIUM
        □ Tipo Documento *  [dropdown]  editable  required
            placeholder: "--- Selezionare il tipo documento ---"
            options: ref: BOA_ANC_GetTipiDocumentoAttivi (descrizione / codiceDocId)

  COL-RIGHT:
    ■ [BOX] label = ri!documento.descrizione (o "Documento")  isCollapsible: true
        (forEach ri!contenuti: visualizzazione file)

FOOTER / ACTIONS:
  [PRIMARY]  "Conferma"
      disabled: isNullOrEmpty(local!tipoDocSelected)
      validate: true; loadingIndicator: true; icon: check
      confirmHeader: "ATTENZIONE"
      confirmMessage: "E' stato selezionato {local!tipoDocSelected.descrizione} come tipologia.
                       Attenzione: non sarà possibile modificare il tipo documento in futuro.
                       Confermare la selezione?"
      → startProcess BOA_ANC_Processo_TipizzaDoc
```

---

### SCREEN: BOA_ANC_Summary

```
SCREEN: BOA_ANC_Summary
TYPE:   View (Summary pratica / Record related action view)
URL:    (accessibile da Record Type Pratica_ANC — summary view o azione correlata)
WIDTH:  WIDE

HEADER:
  [BOA_ANC_Header]  idCase: ri!idCase  (header pratica con ID e info case)

[TAB NAVIGATOR — side-by-side con link dinamici]
  ROW icone:
    ■ icon: file-o        color: ACCENT  size: LARGE  align: CENTER
    ■ icon: file-image-o  color: ACCENT  size: LARGE  align: CENTER
    ■ icon: legal         color: ACCENT  size: LARGE  align: CENTER
  ROW labels:
    ■ "Dati Pratica"     [dynamicLink → local!selezione = "Pratica"]   color: ACCENT  size: MEDIUM  style: STRONG
    ■ "Dati Lavorazione" [dynamicLink → local!selezione = "Video"]     color: ACCENT  size: MEDIUM  style: STRONG
    ■ "Esito"            [dynamicLink → local!selezione = "Esito"]     color: ACCENT  size: MEDIUM  style: STRONG

--- TAB "Dati Pratica" (local!selezione = "Pratica") ---
  ~ [CARD showBorder: false]  showWhen: local!selezione = "Pratica"
    [BOA_ANC_Sezione_DatiPratica]  idCase: ri!idCase  readOnly: true
      ■ Data Apertura          [text]  readonly
      ■ Data Ultima Modifica   [text]  readonly
      ■ Stato                  [text]  readonly
      ■ Codice Cliente         [text]  readonly
      ■ Codice Fiscale         [text]  readonly
      ■ Canale                 [text]  readonly
      ~ ■ Data Chiusura         [text]  readonly  showWhen: isNotNullOrEmpty
      ~ ■ Esito SD              [text]  readonly  showWhen: and(isNotNullOrEmpty, readOnly)

--- TAB "Dati Lavorazione" (local!selezione = "Video") ---
  ~ [CARD showBorder: false]  showWhen: local!selezione = "Video"
    [BOA_ANC_Task_VerificaDocumenti]  readOnly: true
      (struttura identica al tab 2 di BOA_ANC_Task_Lavorazione, readOnly: true)

--- TAB "Esito" (local!selezione = "Esito") ---
  ~ [CARD showBorder: false]  showWhen: local!selezione = "Esito"
    [BOA_ANC_Summary_Esito]
      ■ [BOX] "Esito Verifiche Back Office"
          ■ Esito Scrivania Digitale      [text]  readonly  default: "La pratica non è stata ancora esitata"
          ~ ■ Data Esito Scrivania Digitale [date]  readonly  showWhen: isNotNullOrEmpty(pratica.dataEsitoSD)  (dd/mm/yyyy HH:mm)
          ~ [BOX] "Note"  showWhen: isNotNullOrEmpty(nota)
              ■ Data       [text]  readonly  (dd/mm/yyyy HH:mm:ss)
              ■ Operatore  [text]  readonly
              ■ Testo      [textarea]  readonly
```

---

### SCREEN: BOA_ANC_Intertfaccia_RiassegnazioneTask

```
SCREEN: BOA_ANC_Intertfaccia_RiassegnazioneTask
TYPE:   Form + List
URL:    /scrivania-digitale-supervisore-anc/riassegna-attivita (via processo)
WIDTH:  WIDE

[BOX] "Dettagli riassegnazione"  style: BOA_STYLE_POSTE
  ■ "Processo: {BOA_ANC_NOMEAPPLICAZIONE}"  label: STRONG  display-only
  ~ [2 COLONNE]  showWhen: not(isnull(local!processo))
      COL-LEFT:
        □ Scegli Tipologia di Riassegnazione *  [radio]  editable  required
            options:
              [{"label": "Riassegna Attività al Gruppo Operatore", "value": "BOA ANC Operatori"},
               {"label": "Riassegna Attività a Utenti",            "value": "Utenti"}]
      ~ COL-RIGHT:
          ~ □ Inserisci utenti a cui assegnare le attività *  [user picker]  editable
                showWhen: local!tipoRiassegnazione = "Utenti"
                required: true; groupFilter: BOA ANC Operatori

[BOX] "Le attività dei miei processi"  style: BOA_STYLE_POSTE  showWhen: not(isnull(local!processo))
  [BOA_ANC_Filtri_TaskReportSupervisoreGruppi]  (filtri: data creazione, data scadenza, assegnatario, gruppo, pratica)
  ■ [grid]  selectable  selectionRequired: true  pageSize: 8
      Columns (in order):
        Processo         [text]          c10
        Pratica          [text/link]     c18
        Nome Attività    [richText]      c0
        Assegnatario     [text/user]     c4   (BOA_ANC_VerificaUtenteGruppo_TaskReport)
        Owner            [text/user]     c16
        Data Assegnazione [date]         c3   (dd/MM/yyyy HH:mm:ss)
        Data Presa in carico [date]      c12  (dd/MM/yyyy HH:mm:ss)
        ~~Data Scadenza~~ [date]  showWhen: false
        Stato            [image/icon]    c1   (GREY=Assegnato, GREEN=In Lavorazione)
      Row action: [select multi] → accumula in local!selectedRows

FOOTER / ACTIONS:
  [PRIMARY]  "Riassegna"  (azione di submit riassegnazione ai destinatari selezionati)
      → startProcess BOA_ANC_Single_Riassegnazione o BOA_ANC_ProcessoSupervisore_RiassegnazioneTask
```

---

### SCREEN: Pratica_ANC — Record Type (List View)

```
SCREEN: Pratica_ANC (Record Type List View)
TYPE:   List
URL:    /scrivania-digitale-operatore-anc/pratiche
        /scrivania-digitale-supervisore-anc/pratiche
WIDTH:  WIDE

■ [recordGridField]  readonly  sortable
    Columns (in order):
      Id                       [number]   showWhen: false
      Pratica N.               [recordLink → detail view]
      Canale                   [text]     showWhen: false
      Codice Fiscale           [text]
      Codice Cliente           [text]
      Data Apertura            [date]
      Data Ultima Modifica     [date]
      Data Chiusura            [date]
      Data Inserimento Richiesta [date]
      Esito SD                 [text]
      Operatore                [text]
      Stato                    [text]
      Data Scadenza            [date]    showWhen: false
      Segnalazioni             [richTextIcon]
    Row action: [recordLink] → Pratica_ANC Summary View (BOA_ANC_Summary)
```

---

### SCREEN: BOA_ANC_Sezione_DatiCliente

```
SCREEN: BOA_ANC_Sezione_DatiCliente
TYPE:   View (sezione embedded)
URL:    (embedded in BOA_ANC_Task_VerificaDocumenti)

[BOX] "Dati Cliente"  style: BOA_STYLE_POSTE  isCollapsible: true  isInitiallyCollapsed: false

  ROW 1 (sideBySideLayout):
    ■ Nome              [text]  readonly  binding: ri!cliente.nome
    ■ Cognome           [text]  readonly  binding: ri!cliente.cognome
    ■ Sesso             [text]  readonly  binding: ri!cliente.sesso  default: "N/A"

  ROW 2 (sideBySideLayout):
    ■ Codice Fiscale    [text]  readonly  binding: ri!cliente.codiceFiscale
    ■ Data di Nascita   [text]  readonly  binding: ri!cliente.dataNascita  (dd/mm/yyyy)
    ■ Comune di Nascita [text]  readonly  binding: ri!cliente.comuneNascita

  ROW 3 (sideBySideLayout):
    ■ Provincia di Nascita  [text]  readonly  binding: ri!cliente.provinciaNascita
    ■ Nazione di Nascita    [text]  readonly  binding: ri!cliente.nazioneNascita
    ■ Telefono              [text]  readonly  binding: ri!cliente.telefono

  ROW 4 (sideBySideLayout):
    ■ Cellulare [text]  readonly  binding: ri!cliente.cellulare
    ■ (vuoto)
    ■ (vuoto)

  [BOA_ANC_Sezione_IndirizzoResidenza_DatiCliente]  (sezione indirizzo residenza — embedded)
```

---

### SCREEN: BOA_ANC_Sezione_DatiCarta

```
SCREEN: BOA_ANC_Sezione_DatiCarta
TYPE:   View (sezione embedded)
URL:    (embedded in BOA_ANC_Task_VerificaDocumenti)

[BOX] "Dati Carta Bloccata"  style: BOA_STYLE_POSTE  isCollapsible: true  isInitiallyCollapsed: false

  ROW 1 (sideBySideLayout):
    ■ Tipo Carta          [text]  readonly  binding: ri!datiCarta.tipoCarta
    ■ Numero Carta        [text]  readonly  binding: ri!datiCarta.numeroCarta
    ■ Intestazione Carta  [text]  readonly  binding: ri!datiCarta.intestazioneCarta
```

---

### SCREEN: BOA_ANC_Summary_Esito

```
SCREEN: BOA_ANC_Summary_Esito
TYPE:   View (sezione embedded)
URL:    (embedded in BOA_ANC_Summary, tab "Esito")

[BOX] "Esito Verifiche Back Office"  style: BOA_STYLE_POSTE

  ROW 1 (sideBySideLayout):
    ■ Esito Scrivania Digitale          [text]  readonly
        binding: ri!pratica.esitoSD
        default: "La pratica non è stata ancora esitata"
    ~ ■ Data Esito Scrivania Digitale   [date]  readonly
        showWhen: isNotNullOrEmpty(ri!pratica.dataEsitoSD)
        binding: ri!pratica.dataEsitoSD  (dd/mm/yyyy HH:mm)

  ~ [BOX] "Note"  style: BOA_STYLE_POSTE  showWhen: isNotNullOrEmpty(ri!nota)
      ROW 1 (sideBySideLayout):
        ■ Data       [text]  readonly  binding: ri!nota.datacreazione  (dd/mm/yyyy HH:mm:ss)
        ■ Operatore  [text]  readonly  binding: ri!nota.operatore  (nome utente)
      ■ Testo        [textarea/paragraph]  readonly  binding: ri!nota.testoNota
```

---

## Note Operative

### Dipendenze non risolte in questo documento

| Dipendenza | Necessaria per | Layer sorgente |
|---|---|---|
| Valori dropdown `BOA_ANC_GetTipiDocumentoAttivi` | Opzioni reali del dropdown Tipo Documento in BOA_ANC_Task_TipizzazioneDocumenti | DBA (`04_dba.md`) |
| Schema BOA_ANC_Pratica / BOA_ANC_Cliente / BOA_ANC_DatiCarta | Tipi campo precisi per binding e validazione | DBA (`04_dba.md`) |
| Endpoint API di submit (BOA_ANC_SalvataggioDati, BOA_ANC_Processo_CambioStato) | Cosa succede server-side ai click dei bottoni | BPM (`02_bpm.md`) |
| Significato condizioni showWhen (es. `ri!pratica.esitoSD != BOA_ANC_ESITI_SD[1]`) | Interpretazione business delle regole di visibilità | Functional (`01_functional.md`) |
| BOA_ANC_CheckList_Section (9120508) — struttura interna | Specifica completa delle voci checklist | file content/9120508.xml (non analizzato in dettaglio) |
| BOA_ANC_Header (9077771) — struttura interna | Campi esatti dell'header pratica | file content/9077771.xml (non analizzato in dettaglio) |
| Sezioni grafici dashboard Supervisore (9113035, 9113078, 9113178) | Specifiche grafici | file content/9113035/9113078/9113178.xml |

### Gap segnalati

- `BOA_ANC_Sezione_IndirizzoResidenza_DatiCliente` (9093562): file presente nell'export, non letto in dettaglio — campi indirizzo residenza (presumibilmente: via, numero civico, CAP, comune, provincia).
- Pagina Supervisore "Riassegna Attività" punta direttamente a `ProcessModel` — non a una interface rule. La IR BOA_ANC_Intertfaccia_RiassegnazioneTask è il form del task generato dal processo.
- `BOA_ANC_Task_MenuLaterale`: la voce Riepilogo (terza) è cliccabile **solo** se `isNotNullOrEmpty(ri!pratica.esitoSD)` — vincolo visivo: appare in stile STANDARD (grigio) finché l'esito non è definito.

---

# FASE 2 — STEP 4 → STEP 9

---

## STEP 4 — Layout Structure

### Struttura Layout per Schermata

#### BOA_ANC_Interfaccia_DashBoard_Operatore  [pageWidth: WIDE]

```
BOA_ANC_Interfaccia_DashBoard_Operatore
├── a!localVariables (dati utente, contatori, azioni, link favoriti)
├── a!billboardLayout_v1  [height: MEDIUM, marginBelow: NONE]
│   └── overlay: barOverlay [style: LIGHT, position: BOTTOM]
│       └── a!columnsLayout
│           ├── col-left
│           │   ├── a!imageField_v1 (style: AVATAR)  — avatar utente
│           │   ├── a!richTextDisplayField  — nome utente (LARGE + STRONG)
│           │   └── a!richTextDisplayField  — ruolo (EMPHASIS)
│           ├── col-center  (vuota)
│           └── col-right
│               ├── a!richTextDisplayField  — "N° Attività" (count)
│               ├── a!richTextDisplayField  — "Pratiche Attive" (color: ACCENT)
│               └── a!richTextDisplayField  — "Pratiche Chiuse" (color: #008000)
├── a!columnsLayout  [body]
│   ├── col-left
│   │   ├── a!boxLayout  "Azioni"  style: BOA_STYLE_POSTE
│   │   │   └── forEach(azioni): a!richTextIcon + a!richTextItem  → startProcessLink
│   │   └── a!boxLayout  "Link Favoriti"  style: BOA_STYLE_POSTE
│   │       ├── forEach(links): richTextIcon(remove) + richTextIcon(edit) + richTextItem → safeLink
│   │       │    showWhen: and(local!utenteLinkFavoriti<>"", not(local!gestisciLinks))
│   │       ├── a!richTextDisplayField  "Nessun link presente"
│   │       │    showWhen: and(local!utenteLinkFavoriti="", not(local!gestisciLinks))
│   │       ├── a!richTextIcon  plus-square-o  → dynamicLink (apre form)
│   │       └── a!cardLayout  showBorder: false  showWhen: local!gestisciLinks=true
│   │           ├── a!textField  "Titolo Link"  editable
│   │           ├── a!textField  "Link"  editable  validation: "Inserire un link valido."
│   │           ├── a!dropdownField_v1  "Tipo Link"  editable
│   │           └── a!ButtonLayout
│   │               ├── a!ButtonWidget  "Esci"  PRIMARY
│   │               └── a!ButtonWidget  "Salva"  PRIMARY
│   └── col-right  (vuota nella struttura base — contiene grafici su Supervisore)
```

#### BOA_ANC_Interfaccia_DashBoard_Supervisore  [pageWidth: WIDE]

```
BOA_ANC_Interfaccia_DashBoard_Supervisore
├── a!localVariables (stessi del Operatore + dati grafici)
├── a!billboardLayout_v1  [height: MEDIUM, backgroundColor: #FFEC00]
│   └── overlay: barOverlay [style: LIGHT]
│       └── (struttura identica a Operatore: avatar + nome + ruolo Supervisore + contatori)
├── a!columnsLayout  [body]
│   ├── col-left
│   │   └── a!boxLayout  "Azioni"  (stessa struttura Operatore)
│   └── col-right
│       ├── BOA_ANC_GraficiPraticheGiornaliere_Section
│       ├── BOA_ANC_GraficiPraticheGiornaliereLavorate_Section
│       └── BOA_ANC_GraficiPraticheByStato_Section
```

#### BOA_ANC_ListaAttivita  [pageWidth: WIDE]

```
BOA_ANC_ListaAttivita
├── a!localVariables (pagingInfo, filtri, selected)
├── BOA_ANC_FiltriUtente_ListaAttivita  [embedded — form filtri]
│   ├── a!sideBySideLayout ROW-1
│   │   ├── a!dropdownField_v1  "Stato"
│   │   ├── a!multipleDropdownField_v1  "Tipo Pratica"  (disabled)
│   │   ├── a!textField  "Pratica N."
│   │   └── a!textField  "Nome Attività"
│   ├── a!sideBySideLayout ROW-2
│   │   ├── a!pickerFieldUsersAndGroups  "Assegnatari"
│   │   └── a!pickerFieldUsers  "Utente in carico"
│   └── a!ButtonLayout
│       ├── a!ButtonWidget  "Applica Filtri"  DESTRUCTIVE
│       └── a!ButtonWidget  "Applica e Salva Filtri"  PRIMARY
├── a!checkboxField  "Visualizza le attività a me assegnate"
├── a!boxLayout  "Ultimi N Filtri Salvati"
│   └── a!gridField_v1  [filtri salvati]  selectionStyle: ROW_HIGHLIGHT
│       └── cols: Stato | Tipo Pratica | Pratica N. | Nome Attività | Data Scadenza Da/A | Assegnatario | Utente in carico
└── a!gridField_v1  [attività principali]  pageSize: 8
    └── cols: Nome Attività | Stato | Tipo Pratica | Pratica N. | Assegnatario | Utente in carico | Data Scadenza
```

#### BOA_ANC_Task_Lavorazione  [pageWidth: WIDE]

```
BOA_ANC_Task_Lavorazione
├── a!localVariables (collapsibleNavSections, collapseNav, activeSection, salva, modifica, checkList)
├── a!columnsLayout  [spacing: SPARSE, showDividers: true]
│   ├── col-left  [width: NARROW/EXTRA_NARROW (toggle collapseNav)]
│   │   └── BOA_ANC_Task_MenuLaterale
│   │       ├── voce 1  "Dati Pratica"        icon: briefcase         → activeSection=1
│   │       ├── voce 2  "Verifica Documento"  icon: check-square-o    → activeSection=2
│   │       └── voce 3  "Riepilogo"           icon: address-card-o    → activeSection=3
│   │                    link showWhen: isNotNullOrEmpty(ri!pratica.esitoSD)
│   └── col-right  [content area — choose(activeSection)]
│       ├── choose=1: BOA_ANC_Sezione_DatiPratica  (readOnly: false)
│       ├── choose=2: BOA_ANC_Task_VerificaDocumenti  (readOnly: isNotNullOrEmpty(esitoSD))
│       └── choose=3: BOA_ANC_Task_Riepilogo
├── a!richTextDisplayField  (spacer)
└── a!ButtonLayout
    ├── primaryButtons
    │   ├── a!ButtonWidget  "chiudi pratica"   submit:true  showWhen: activeSection=3
    │   └── a!ButtonWidget  "Salva e prosegui" submit:false showWhen: activeSection=2
    │                        disabled: isNotNullOrEmpty(ri!pratica.esitoSD)
    └── secondaryButtons
        └── a!ButtonWidget  "Modifica"
```

#### BOA_ANC_Task_VerificaDocumenti  [pageWidth: WIDE — embedded]

```
BOA_ANC_Task_VerificaDocumenti
├── a!localVariables (showAllegati: true, showSection: true)
├── a!ButtonLayout  [toggle buttons]
│   ├── a!ButtonWidget  "nascondi allegati" / "mostra allegati"  PRIMARY  icon: eye-slash/eye
│   └── a!ButtonWidget  "nascondi sezione" / "mostra sezione"    SECONDARY  icon: eye-slash/eye
└── a!columnsLayout
    ├── col-left  showWhen: local!showSection=true
    │   ├── BOA_ANC_Sezione_DatiCliente      (isCollapsible: true)
    │   ├── BOA_ANC_Sezione_DatiCarta        (isCollapsible: true)
    │   ├── BOA_ANC_Contenuti_Section
    │   └── BOA_ANC_Task_CheckList           (readOnly: false)
    └── col-right  showWhen: local!showAllegati=true
        └── BOA_ANC_Sezione_Documenti
```

#### BOA_ANC_Summary  [pageWidth: WIDE — Record Summary View]

```
BOA_ANC_Summary
├── a!localVariables (selezione: "Pratica", case, pratica, contenuti, datiCarta, cliente, checkList, documento, nota)
├── BOA_ANC_Header
│   └── a!billboardLayout_v1  [height: SHORT, marginBelow: NONE]
│       └── overlay: fullOverlay [style: NONE]
│           ├── a!columnsLayout → a!milestoneField  (stati pratica)
│           └── a!columnsLayout  [info pratica]
│               ├── col-left  [MEDIUM]: avatar app + nome applicazione + pratica N.
│               └── col-right: pratica.stato + canale + CF
├── a!richTextDisplayField  (spacer)
├── a!sideBySideLayout  [icone tab]
│   ├── icon: file-o        ACCENT LARGE CENTER
│   ├── icon: file-image-o  ACCENT LARGE CENTER
│   └── icon: legal         ACCENT LARGE CENTER
├── a!sideBySideLayout  [label tab]
│   ├── "Dati Pratica"     dynamicLink → local!selezione="Pratica"  ACCENT MEDIUM STRONG
│   ├── "Dati Lavorazione" dynamicLink → local!selezione="Video"    ACCENT MEDIUM STRONG
│   └── "Esito"            dynamicLink → local!selezione="Esito"    ACCENT MEDIUM STRONG
├── a!cardLayout  showWhen: local!selezione="Pratica"
│   └── BOA_ANC_Sezione_DatiPratica  (readOnly: true)
├── a!cardLayout  showWhen: local!selezione="Video"
│   └── BOA_ANC_Task_VerificaDocumenti  (readOnly: true)
└── a!cardLayout  showWhen: local!selezione="Esito"
    └── BOA_ANC_Summary_Esito
```

#### BOA_ANC_Intertfaccia_RiassegnazioneTask  [pageWidth: WIDE]

```
BOA_ANC_Intertfaccia_RiassegnazioneTask
├── a!localVariables (selection, selectedRows, tipoRiassegnazione, nuoviUtentiAssegnatari, processo)
├── a!boxLayout  "Dettagli riassegnazione"  style: BOA_STYLE_POSTE
│   ├── a!sideBySideLayout → richTextItem "Processo: {BOA_ANC_NOMEAPPLICAZIONE}"
│   └── a!columnsLayout  showWhen: not(isnull(local!processo))
│       ├── col-left
│       │   └── a!radioButtonField  "Scegli Tipologia di Riassegnazione"  required
│       └── col-right
│           └── a!pickerFieldUsers  "Inserisci utenti..."  showWhen: tipoRiassegnazione="Utenti"
├── a!boxLayout  "Le attività dei miei processi"  showWhen: not(isnull(local!processo))
│   ├── BOA_ANC_Filtri_TaskReportSupervisoreGruppi  (filtri embedded)
│   └── a!gridField_v1  selectable  selectionRequired: true  pageSize: 8
│       └── cols: Processo | Pratica | Nome Attività | Assegnatario | Owner | Data Assegnazione | Data Presa in carico | Stato
└── a!ButtonLayout
    └── a!ButtonWidget  "Riassegna"  PRIMARY
```

#### BOA_ANC_Task_TipizzazioneDocumenti  [pageWidth: WIDE]

```
BOA_ANC_Task_TipizzazioneDocumenti
├── a!localVariables (tipoDocSelected, tipiDocAttivi)
├── a!cardLayout  style: INFO  showWhen: isNullOrEmpty(ri!contenuti)
│   └── richTextIcon info-circle + richTextItem "Nessun contenuto associato alla pratica"
└── a!columnsLayout  showWhen: isNotNullOrEmpty(ri!contenuti)
    ├── col-left
    │   └── a!boxLayout  "Tipizzazione Documento"  style: BOA_STYLE_POSTE
    │       ├── a!cardLayout  style: INFO  shape: SEMI_ROUNDED  (istruzioni)
    │       └── a!dropdownField_v1  "Tipo Documento"  required
    │           └── a!ButtonLayout
    │               └── a!ButtonWidget  "Conferma"  PRIMARY
    │                    disabled: isNullOrEmpty(local!tipoDocSelected)
    │                    confirmHeader: "ATTENZIONE"  confirmMessage: (dialog testo)
    └── col-right
        └── a!boxLayout  label: ri!documento.descrizione  isCollapsible: true
            └── forEach(ri!contenuti): file preview
```

---

### Tabella Pattern Layout

| Interface Rule | Layout Radice | Struttura | Header | Sidebar | Footer/Actions |
|---|---|---|---|---|---|
| BOA_ANC_Interfaccia_DashBoard_Operatore | billboardLayout_v1 | Billboard + 2-col body | Billboard (hero) | no | dentro card form |
| BOA_ANC_Interfaccia_DashBoard_Supervisore | billboardLayout_v1 | Billboard + 2-col body | Billboard (hero) | no | dentro card form |
| BOA_ANC_ListaAttivita | gridField_v1 (principale) | Form filtri + grids | no | no | a!ButtonLayout dentro filtri |
| BOA_ANC_Task_Lavorazione | columnsLayout (SPARSE) | 2-col: sidebar + content (choose) | no | sì (BOA_ANC_Task_MenuLaterale) | sì (a!ButtonLayout) |
| BOA_ANC_Task_VerificaDocumenti | columnsLayout | 2-col: dati + allegati | no | no | a!ButtonLayout (toggle) in cima |
| BOA_ANC_Task_Riepilogo | richTextDisplayField + columnsLayout | Single-col + 2-col forEach esiti | no | no | no (bottoni in BOA_ANC_Task_Lavorazione) |
| BOA_ANC_Summary | sideBySideLayout (tab) | Header + tab links + cardLayout per tab | sì (BOA_ANC_Header milestoneField) | no | no |
| BOA_ANC_Summary_Esito | boxLayout | Single-col + nested box Note | no | no | no |
| BOA_ANC_Sezione_DatiPratica | boxLayout | Single-col (sideBySideLayout rows) | sì (boxLayout label) | no | no |
| BOA_ANC_Sezione_DatiCliente | boxLayout (collassabile) | 4× sideBySideLayout rows + nested IR | sì (boxLayout "Dati Cliente") | no | no |
| BOA_ANC_Sezione_DatiCarta | boxLayout (collassabile) | 1× sideBySideLayout row | sì (boxLayout "Dati Carta Bloccata") | no | no |
| BOA_ANC_Intertfaccia_RiassegnazioneTask | boxLayout + gridField_v1 | 2 boxLayout + 2-col inside + grid | no | no | sì (a!ButtonLayout "Riassegna") |
| BOA_ANC_Task_TipizzazioneDocumenti | columnsLayout | 2-col: form + preview | no | no | sì (a!ButtonWidget dentro boxLayout) |
| Pratica_ANC (List View) | recordGridField | Single-col grid | no | no | no |
| BOA_ANC_Header | billboardLayout_v1 (SHORT) | Billboard + milestoneField + 2-col info | Billboard (SHORT) | no | no |


---

## STEP 5 — Navigation & Interaction Map

### Mappa Interazioni

| Schermata (IR) | Trigger | Tipo Interazione | Target / Effetto |
|---|---|---|---|
| BOA_ANC_Interfaccia_DashBoard_Operatore | richTextIcon+richTextItem "{nomeAzione}" (forEach) | start-process | → avvia processo azione del gruppo (dinamico) |
| BOA_ANC_Interfaccia_DashBoard_Operatore | richTextIcon "edit" link favorito | edit-toggle | → local!gestisciLinks = true (apre card form) |
| BOA_ANC_Interfaccia_DashBoard_Operatore | richTextIcon "remove" link favorito | delete-link | → local!gestisciLinks = true + rimozione |
| BOA_ANC_Interfaccia_DashBoard_Operatore | richTextItem "{titoloLink}" | navigate | → safeLink → url favorito |
| BOA_ANC_Interfaccia_DashBoard_Operatore | richTextIcon "plus-square-o" Aggiungi link | edit-toggle | → local!gestisciLinks = true (apre card form) |
| BOA_ANC_Interfaccia_DashBoard_Operatore | Button "Esci" (card form) | edit-toggle | → local!gestisciLinks = false (chiude card form) |
| BOA_ANC_Interfaccia_DashBoard_Operatore | Button "Salva" (card form) | submit | → writeToDataStore (salva link favorito) + reload |
| BOA_ANC_Interfaccia_DashBoard_Supervisore | richTextIcon+richTextItem "{nomeAzione}" | start-process | → avvia processo azione del gruppo Supervisore |
| BOA_ANC_ListaAttivita | Row grid attività | navigate | → apertura task di lavorazione (BOA_ANC_Task_Lavorazione) |
| BOA_ANC_ListaAttivita | Row grid filtri salvati | select | → popola form filtri con valori riga selezionata |
| BOA_ANC_FiltriUtente_ListaAttivita | Button "Applica Filtri" | search/filter | → toggle local!applicaFiltri + reset pagingInfo |
| BOA_ANC_FiltriUtente_ListaAttivita | Button "Applica e Salva Filtri" | submit | → startProcess BOA_ANC_SalvaFiltriUtente + applica filtri |
| BOA_ANC_Task_Lavorazione | Link sidebar "Dati Pratica" | tab-navigate | → local!activeCollapsibleNavSection = 1 |
| BOA_ANC_Task_Lavorazione | Link sidebar "Verifica Documento" | tab-navigate | → local!activeCollapsibleNavSection = 2 |
| BOA_ANC_Task_Lavorazione | Link sidebar "Riepilogo" | tab-navigate | → local!activeCollapsibleNavSection = 3 (solo se esitoSD non null) |
| BOA_ANC_Task_Lavorazione | richTextIcon angle-double-left/right sidebar | collapse-toggle | → local!collapseNav toggle (sidebar NARROW/EXTRA_NARROW) |
| BOA_ANC_Task_Lavorazione | Button "Salva e prosegui" | submit | → startProcess BOA_ANC_SalvataggioDati → salva dati + activeSection=3 |
| BOA_ANC_Task_Lavorazione | Button "chiudi pratica" | submit | → startProcess BOA_ANC_SalvataggioDati → chiude pratica (stato: chiuso) |
| BOA_ANC_Task_Lavorazione | Button "Modifica" | submit | → startProcess BOA_ANC_SalvataggioDati → reset esitoSD → riabilita lavorazione |
| BOA_ANC_Task_VerificaDocumenti | Button "nascondi allegati" / "mostra allegati" | visibility-toggle | → local!showAllegati toggle |
| BOA_ANC_Task_VerificaDocumenti | Button "nascondi sezione" / "mostra sezione" | visibility-toggle | → local!showSection toggle |
| BOA_ANC_Sezione_Documenti | dynamicLink "QUI" (download file) | start-process | → startProcess BOA_ANC_ScaricaSingoloDoc (download singolo documento) |
| BOA_ANC_Summary | dynamicLink "Dati Pratica" | tab-navigate | → local!selezione = "Pratica" |
| BOA_ANC_Summary | dynamicLink "Dati Lavorazione" | tab-navigate | → local!selezione = "Video" |
| BOA_ANC_Summary | dynamicLink "Esito" | tab-navigate | → local!selezione = "Esito" |
| BOA_ANC_Task_TipizzazioneDocumenti | Button "Conferma" | submit+modal-confirm | → confirmHeader "ATTENZIONE" + startProcess BOA_ANC_Processo_TipizzaDoc |
| BOA_ANC_Intertfaccia_RiassegnazioneTask | Grid row selection | multi-select | → accumula local!selectedRows |
| BOA_ANC_Intertfaccia_RiassegnazioneTask | Button "Riassegna" | submit | → startProcess BOA_ANC_ProcessoSupervisore_RiassegnazioneTask |
| Pratica_ANC (List View) | recordLink "Pratica N." | navigate | → Pratica_ANC Summary View (BOA_ANC_Summary) |
| Pratica_ANC (List View) | Row click | navigate | → Pratica_ANC Summary View (BOA_ANC_Summary) |

---

### Diagramma Navigation Flow

```
Entry Point: /scrivania-digitale-operatore-anc/home
  └── BOA_ANC_Interfaccia_DashBoard_Operatore
        ├── [start-process] richTextItem "{nomeAzione}" → processo azione (dinamico per gruppo)
        ├── [safeLink] link favorito → url favorito (interno/esterno/legacy)
        └── [edit-toggle] icona edit/plus/remove → card form gestione link favoriti
              └── [submit] "Salva" → writeToDataStore

Entry Point: /scrivania-digitale-operatore-anc/task
  └── BOA_ANC_ListaAttivita
        ├── [search/filter] "Applica Filtri" → aggiorna grid attività
        ├── [submit] "Applica e Salva Filtri" → startProcess BOA_ANC_SalvaFiltriUtente
        └── [navigate] Row grid attività → BOA_ANC_Task_Lavorazione (task form)
              ├── [tab-navigate] sidebar "Dati Pratica"      → BOA_ANC_Sezione_DatiPratica (readonly)
              ├── [tab-navigate] sidebar "Verifica Documento" → BOA_ANC_Task_VerificaDocumenti
              │     └── [start-process] link "QUI" download → BOA_ANC_ScaricaSingoloDoc
              ├── [tab-navigate] sidebar "Riepilogo"          → BOA_ANC_Task_Riepilogo
              ├── [submit] "Salva e prosegui" → BOA_ANC_SalvataggioDati → activeSection=3
              ├── [submit] "chiudi pratica"   → BOA_ANC_SalvataggioDati → chiude pratica
              └── [submit] "Modifica"         → BOA_ANC_SalvataggioDati → reset esitoSD

Entry Point: /scrivania-digitale-operatore-anc/pratiche
  └── Pratica_ANC (Record List View)
        └── [navigate] recordLink / row → BOA_ANC_Summary (Record Summary View)
              ├── [tab-navigate] "Dati Pratica"     → BOA_ANC_Sezione_DatiPratica (readonly)
              ├── [tab-navigate] "Dati Lavorazione" → BOA_ANC_Task_VerificaDocumenti (readonly)
              └── [tab-navigate] "Esito"            → BOA_ANC_Summary_Esito (readonly)

Entry Point: /scrivania-digitale-supervisore-anc/home
  └── BOA_ANC_Interfaccia_DashBoard_Supervisore
        └── [start-process] richTextItem "{nomeAzione}" → processo azione Supervisore

Entry Point: /scrivania-digitale-supervisore-anc/pratiche
  └── Pratica_ANC (Record List View)  [stesso del Operatore]

Entry Point: /scrivania-digitale-supervisore-anc/riassegna-attivita
  └── [avvio processo] BOA_ANC_ProcessoSupervisore_RiassegnazioneTask
        └── BOA_ANC_Intertfaccia_RiassegnazioneTask (task form)
              ├── [search/filter] BOA_ANC_Filtri_TaskReportSupervisoreGruppi → aggiorna grid attività
              ├── [multi-select] row grid → accumula selectedRows
              └── [submit] "Riassegna" → startProcess BOA_ANC_ProcessoSupervisore_RiassegnazioneTask

Task esterno (non navigabile da site):
  → BOA_ANC_Task_TipizzazioneDocumenti (processo BOA_ANC_Processo_TipizzaDoc)
        └── [submit+confirm] "Conferma" → confirmDialog "ATTENZIONE" → startProcess BOA_ANC_Processo_TipizzaDoc
```


---

## STEP 6 — Runtime State Extraction

### Tabella Stati Runtime

| Schermata (IR) | Componente | Label | Stato Predefinito | Condizione Dinamica | Tipo Condizione |
|---|---|---|---|---|---|
| BOA_ANC_Task_Lavorazione | BOA_ANC_Task_VerificaDocumenti (embedded) | (intera sezione) | editable | `readOnly: isNotNullOrEmpty(ri!pratica.esitoSD)` | state-dependent (pratica già esitata → readonly) |
| BOA_ANC_Task_Lavorazione | a!ButtonWidget "Salva e prosegui" | Salva e prosegui | enabled | `disabled: isNotNullOrEmpty(ri!pratica.esitoSD)` | state-dependent |
| BOA_ANC_Task_Lavorazione | a!ButtonWidget "chiudi pratica" | chiudi pratica | hidden | `showWhen: local!activeCollapsibleNavSection = 3` | step-dependent |
| BOA_ANC_Task_Lavorazione | a!ButtonWidget "Salva e prosegui" | Salva e prosegui | hidden | `showWhen: local!activeCollapsibleNavSection = 2` | step-dependent |
| BOA_ANC_Task_Lavorazione | voce 3 sidebar "Riepilogo" | Riepilogo | navigazione disabilitata | `link showWhen: isNotNullOrEmpty(ri!pratica.esitoSD)` | state-dependent |
| BOA_ANC_Interfaccia_DashBoard_Operatore | forEach link favoriti | lista link | hidden | `showWhen: and(local!utenteLinkFavoriti<>"", not(local!gestisciLinks))` | conditional-visibility |
| BOA_ANC_Interfaccia_DashBoard_Operatore | richTextDisplayField "Nessun link presente" | Nessun link presente | visible | `showWhen: and(local!utenteLinkFavoriti="", not(local!gestisciLinks))` | conditional-visibility |
| BOA_ANC_Interfaccia_DashBoard_Operatore | cardLayout form gestione link | (card form) | hidden | `showWhen: local!gestisciLinks = true` | edit-toggle |
| BOA_ANC_Interfaccia_DashBoard_Operatore | a!textField "Link" | Link | editable | validation: `not(isValidURL(value))` | input-validation |
| BOA_ANC_Sezione_DatiPratica | sideBySideItem a!textField "Data Chiusura" | Data Chiusura | hidden | `showWhen: isNotNullOrEmpty(pratica.dataChiusura)` | conditional-visibility |
| BOA_ANC_Sezione_DatiPratica | sideBySideItem a!textField "Esito SD" | Esito SD | hidden | `showWhen: and(isNotNullOrEmpty(pratica.esitoSD), ri!readOnly)` | conditional-visibility (visibile solo in modalità readonly) |
| BOA_ANC_Summary_Esito | a!dateField "Data Esito Scrivania Digitale" | Data Esito Scrivania Digitale | hidden | `showWhen: isNotNullOrEmpty(ri!pratica.dataEsitoSD)` | conditional-visibility |
| BOA_ANC_Summary_Esito | a!boxLayout "Note" | Note | hidden | `showWhen: isNotNullOrEmpty(ri!nota)` | conditional-visibility |
| BOA_ANC_Task_Riepilogo | a!cardLayout motivazioni | (card motivazioni) | hidden | `showWhen: isNotNullOrEmpty(local!motivazioni)` | conditional-visibility |
| BOA_ANC_Task_Riepilogo | a!paragraphField "Note Interne" | Note Interne | hidden | `showWhen: and(isNotNullOrEmpty(local!esitoControlli), local!esitoControlli != BOA_ANC_ESITI_SD[1])` | state-dependent (esito negativo) |
| BOA_ANC_Task_TipizzazioneDocumenti | a!cardLayout INFO "Nessun contenuto" | (card) | visible | `showWhen: isNullOrEmpty(ri!contenuti)` | conditional-visibility |
| BOA_ANC_Task_TipizzazioneDocumenti | a!columnsLayout (form + preview) | (layout 2 col) | hidden | `showWhen: isNotNullOrEmpty(ri!contenuti)` | conditional-visibility |
| BOA_ANC_Task_TipizzazioneDocumenti | a!ButtonWidget "Conferma" | Conferma | disabled | `disabled: isNullOrEmpty(local!tipoDocSelected)` | input-dependent |
| BOA_ANC_Intertfaccia_RiassegnazioneTask | a!pickerFieldUsers "Inserisci utenti..." | Inserisci utenti... | hidden | `showWhen: local!tipoRiassegnazione = "Utenti"` | radio-selection-dependent |
| BOA_ANC_Intertfaccia_RiassegnazioneTask | a!columnsLayout radio+picker | (layout) | hidden | `showWhen: not(isnull(local!processo))` | state-dependent |
| BOA_ANC_Intertfaccia_RiassegnazioneTask | a!boxLayout "Le attività dei miei processi" | Le attività dei miei processi | hidden | `showWhen: not(isnull(local!processo))` | state-dependent |
| BOA_ANC_FiltriUtente_ListaAttivita | a!pickerFieldUsersAndGroups "Assegnatari" | Assegnatari | disabled | `disabled: isnull(ri!tipoProcesso)` | input-dependent |
| BOA_ANC_FiltriUtente_ListaAttivita | a!multipleDropdownField_v1 "Tipo Pratica" | Tipo Pratica | disabled | `disabled: true` (hardcoded) | always-disabled |
| BOA_ANC_ListaAttivita | a!checkboxField "Visualizza le attività a me assegnate" | Visualizza le attività a me assegnate | disabled | `disabled: isnull(local!myTasksTipoProcesso)` | state-dependent |
| BOA_ANC_Task_VerificaDocumenti | col-left (sezione dati) | (intera colonna) | visible | `showWhen: local!showSection = true` | visibility-toggle |
| BOA_ANC_Task_VerificaDocumenti | col-right (allegati) | (intera colonna) | visible | `showWhen: local!showAllegati = true` | visibility-toggle |
| BOA_ANC_Sezione_Documenti | a!boxLayout allegati | (box per file) | hidden | `showWhen: isNotNullOrEmpty(ri!contenuti)` | conditional-visibility |
| BOA_ANC_Sezione_Documenti | a!cardLayout "Nessun contenuto..." | (card info) | visible | `showWhen: isNullOrEmpty(ri!contenuti)` | conditional-visibility |
| BOA_ANC_Summary | a!cardLayout tab "Dati Pratica" | (tab content) | hidden | `showWhen: local!selezione = "Pratica"` | tab-selection |
| BOA_ANC_Summary | a!cardLayout tab "Dati Lavorazione" | (tab content) | hidden | `showWhen: local!selezione = "Video"` | tab-selection |
| BOA_ANC_Summary | a!cardLayout tab "Esito" | (tab content) | hidden | `showWhen: local!selezione = "Esito"` | tab-selection |

---

### Messaggi di Validazione / Errore

| Schermata (IR) | Campo / Componente | Messaggio di Validazione (verbatim) | Condizione Trigger |
|---|---|---|---|
| BOA_ANC_Interfaccia_DashBoard_Operatore | a!textField "Link" | `"Inserire un link valido."` | `not(isValidURL(value))` |
| BOA_ANC_Task_TipizzazioneDocumenti | a!ButtonWidget "Conferma" — confirmMessage | `"E' stato selezionato {local!tipoDocSelected.descrizione} come tipologia. Attenzione: non sarà possibile modificare il tipo documento in futuro. Confermare la selezione?"` | Al click del bottone Conferma (dialog modale) |
| BOA_ANC_Task_TipizzazioneDocumenti | a!ButtonWidget "Conferma" — confirmHeader | `"ATTENZIONE"` | Al click del bottone Conferma (header dialog) |
| BOA_ANC_FiltriUtente_ListaAttivita | a!radioButtonField "Scegli Tipologia di Riassegnazione" | (required: true — messaggio Appian standard) | Campo non valorizzato al submit |
| BOA_ANC_Intertfaccia_RiassegnazioneTask | a!pickerFieldUsers "Inserisci utenti..." | (required: true — messaggio Appian standard) | Campo non valorizzato al submit con tipoRiassegnazione="Utenti" |


---

## STEP 7 — Reusable Patterns

### Pattern Identificati

| Pattern | Schermate coinvolte | Componenti chiave | Note |
|---|---|---|---|
| **List / Detail** | BOA_ANC_ListaAttivita (list) → BOA_ANC_Task_Lavorazione (detail) | `a!gridField_v1` → task form (sidebar + content) | Row grid apre task di lavorazione |
| **List / Detail** | Pratica_ANC List View (list) → BOA_ANC_Summary (detail) | `recordGridField` → `a!sideBySideLayout`+card tabs | recordLink su "Pratica N." o click riga |
| **Create / Edit / View** | BOA_ANC_Task_Lavorazione | `a!ButtonWidget "Modifica"` + `readOnly: isNotNullOrEmpty(esitoSD)` | Pratica esitata → tutto readonly; "Modifica" resetta esitoSD e riabilita |
| **Create / Edit / View** | BOA_ANC_Interfaccia_DashBoard_Operatore (link favoriti) | `a!textField`, `a!dropdownField_v1`, card showWhen: gestisciLinks | Inline edit dei link favoriti senza navigazione |
| **Multi-step Form (custom wizard)** | BOA_ANC_Task_Lavorazione | `a!columnsLayout` + `BOA_ANC_Task_MenuLaterale` + `choose(activeSection, ...)` | Pattern sidebar-nav + choose(): Dati Pratica (1) → Verifica Documento (2) → Riepilogo (3); step 3 sbloccato solo dopo esito SD |
| **Search / Result** | BOA_ANC_ListaAttivita | `BOA_ANC_FiltriUtente_ListaAttivita` + `a!gridField_v1` (attività) | Form filtri aggiorna dinamicamente la griglia attività; filtri salvabili per riuso |
| **Search / Result** | BOA_ANC_Intertfaccia_RiassegnazioneTask | `BOA_ANC_Filtri_TaskReportSupervisoreGruppi` + `a!gridField_v1` | Filtri data + assegnatario + gruppo → aggiorna grid attività riassegnabili |
| **Modal Confirmation** | BOA_ANC_Task_TipizzazioneDocumenti | `a!ButtonWidget "Conferma"` con `confirmHeader`/`confirmMessage` | Dialog di conferma prima dell'avvio processo non reversibile (tipizzazione documento) |
| **Document Viewer / Download Flow** | BOA_ANC_Sezione_Documenti | `forEach(contenuti)` + `BOA_ANC_DocumentViewer_Section` + `dynamicLink → startProcess BOA_ANC_ScaricaSingoloDoc` | Viewer inline se documento disponibile; link download alternativo se non recuperabile |
| **Status / Milestone Display** | BOA_ANC_Header (embedded in BOA_ANC_Summary) | `a!milestoneField` (stati pratica) | Barra stati orizzontale sopra header pratica; step attivo evidenziato |
| **Status / Milestone Display** | BOA_ANC_Task_Riepilogo | `a!richTextIcon` check-circle/times-circle + color POSITIVE/NEGATIVE | Indicatore esito visivo con card esiti (RESPINTA/APPROVATA) per ogni controllo |
| **Tab Navigation** | BOA_ANC_Summary | `a!sideBySideLayout` (icone+label) + `a!dynamicLink → local!selezione` + `a!cardLayout showWhen` | Tab custom via dynamicLink: Dati Pratica / Dati Lavorazione / Esito; contenuto condizionale per tab attivo |
| **Tab Navigation (sidebar)** | BOA_ANC_Task_Lavorazione | `BOA_ANC_Task_MenuLaterale` + `choose(local!activeCollapsibleNavSection, ...)` | Tab verticale collassabile; step 3 sbloccato condizionalmente |
| **Dashboard / Card Grid** | BOA_ANC_Interfaccia_DashBoard_Operatore | `a!billboardLayout_v1` + `a!boxLayout "Azioni"` + `forEach(cardLayout)` | Billboard hero + contatori + azioni per gruppo + link favoriti personalizzabili |
| **Dashboard / Card Grid** | BOA_ANC_Interfaccia_DashBoard_Supervisore | `a!billboardLayout_v1` + boxLayout "Azioni" + sezioni grafici | Come Operatore + grafici pratiche per Supervisore; billboard giallo (#FFEC00) distingue il ruolo |
| **Collapsible Section** | BOA_ANC_Sezione_DatiCliente | `a!boxLayout isCollapsible: true` | Box "Dati Cliente" collassabile — utente può nascondere il blocco |
| **Collapsible Section** | BOA_ANC_Sezione_DatiCarta | `a!boxLayout isCollapsible: true` | Box "Dati Carta Bloccata" collassabile |
| **Collapsible Section** | BOA_ANC_Task_TipizzazioneDocumenti col-right | `a!boxLayout isCollapsible: true` | Preview documento collassabile |


---

## STEP 8 — Theme & Style Evidence

### Tabella Evidence Stile

| Categoria | Elemento | Valore Osservato | Dove |
|---|---|---|---|
| Icon (navbar) | Page "Home" (Operatore + Supervisore) | `f015` (fa-home) | site/ae0c311c...xml, site/d72fc841...xml |
| Icon (navbar) | Page "Attività" (Operatore) | `f0ca` (fa-list-ul) | site/ae0c311c...xml |
| Icon (navbar) | Page "Pratiche" (Operatore + Supervisore) | `f016` (fa-file) | site/ae0c311c...xml, site/d72fc841...xml |
| Icon (navbar) | Page "Riassegna Attività" (Supervisore) | `f0c0` (fa-group) | site/d72fc841...xml |
| Button style | BOA_ANC_Task_Lavorazione "chiudi pratica" | PRIMARY (submit) | content/_9087111.xml |
| Button style | BOA_ANC_Task_Lavorazione "Salva e prosegui" | PRIMARY | content/_9087111.xml |
| Button style | BOA_ANC_Task_Lavorazione "Modifica" | SECONDARY | content/_9087111.xml |
| Button style | BOA_ANC_FiltriUtente_ListaAttivita "Applica Filtri" | DESTRUCTIVE | content/_9099170.xml |
| Button style | BOA_ANC_FiltriUtente_ListaAttivita "Applica e Salva Filtri" | PRIMARY | content/_9099170.xml |
| Button style | BOA_ANC_Task_TipizzazioneDocumenti "Conferma" | PRIMARY | content/_10798994.xml |
| Button style | BOA_ANC_Task_VerificaDocumenti "nascondi/mostra allegati" | PRIMARY | content/_9092690.xml |
| Button style | BOA_ANC_Task_VerificaDocumenti "nascondi/mostra sezione" | SECONDARY | content/_9092690.xml |
| Button style | BOA_ANC_Interfaccia_DashBoard_Operatore "Esci" | PRIMARY | content/_9097037.xml |
| Button style | BOA_ANC_Interfaccia_DashBoard_Operatore "Salva" | PRIMARY | content/_9097037.xml |
| Button style | BOA_ANC_Intertfaccia_RiassegnazioneTask "Riassegna" | PRIMARY | content/_9115083.xml |
| Accent color | Site Operatore + Supervisore | `#0047BB` | site/ae0c311c...xml, site/d72fc841...xml |
| Loading bar color | Site Operatore + Supervisore | `#0047BB` | site/ae0c311c...xml, site/d72fc841...xml |
| Selected tab background | Site Operatore + Supervisore | `#0047BB` | site/ae0c311c...xml, site/d72fc841...xml |
| Billboard background | BOA_ANC_Interfaccia_DashBoard_Supervisore | `#FFEC00` (giallo Poste) | content/_9113190.xml — distingue visivamente ruolo Supervisore |
| Billboard background | BOA_ANC_Interfaccia_DashBoard_Operatore | default (tema site) | content/_9097037.xml |
| RichText color | Contatori dashboard "Pratiche Attive" | `ACCENT` (#0047BB) | content/_9097037.xml |
| RichText color | Contatori dashboard "Pratiche Chiuse" | `#008000` (verde) | content/_9097037.xml |
| RichText color | Tab link BOA_ANC_Summary | `ACCENT` (#0047BB) | content/_9099789.xml |
| Icon color | Tab icone BOA_ANC_Summary (file-o, file-image-o, legal) | `ACCENT` (#0047BB) | content/_9099789.xml |
| Esito color | BOA_ANC_Task_Riepilogo — esito POSITIVO | `POSITIVE` (verde sistema) + icon: check-circle | content/_9107408.xml |
| Esito color | BOA_ANC_Task_Riepilogo — esito NEGATIVO | `NEGATIVE` (rosso sistema) + icon: times-circle | content/_9107408.xml |
| Esito color | BOA_ANC_EsitiAttivita card "APPROVATA" | `POSITIVE` + icon: check-circle | content/_9107462.xml |
| Esito color | BOA_ANC_EsitiAttivita card "RESPINTA" | `NEGATIVE` + icon: times-circle | content/_9107462.xml |
| Esito color | BOA_ANC_EsitiAttivita card neutro | `STANDARD` (grigio sistema) | content/_9107462.xml |
| Box style | Tutti i boxLayout principali | `BOA_STYLE_POSTE` (styleClass custom) | content/multiple |
| Page width default | Tutti i siti | `WIDE` | site/ae0c311c...xml, site/d72fc841...xml |
| Button shape | Site globale | `SQUARED` | site/ae0c311c...xml, site/d72fc841...xml |
| Input shape | Site globale | `SQUARED` | site/ae0c311c...xml, site/d72fc841...xml |
| Dialog shape | Site globale | `SQUARED` | site/ae0c311c...xml, site/d72fc841...xml |
| Button label case | Site globale | `UPPERCASE` | site/ae0c311c...xml, site/d72fc841...xml |
| Navigation style | Site globale | `TOPBAR` (STYLE1) | site/ae0c311c...xml, site/d72fc841...xml |
| Logo | Site header | `https://www.poste.it/img/1453895043057/2X/logo-poste-italiane.png` | site/ae0c311c...xml |
| Logo alt text | Site header | `"PosteItaliane"` | site/ae0c311c...xml |
| Tasks visibility | Site globale | `HIDDEN` (task Appian nascosti nel sito) | site/ae0c311c...xml, site/d72fc841...xml |

---

### FontAwesome Icon Mapping (navbar)

| Page | iconId (hex) | FontAwesome Name | Significato UX |
|---|---|---|---|
| Home (Operatore) | f015 | fa-home | Pagina principale / Dashboard |
| Attività | f0ca | fa-list-ul | Lista attività / task |
| Pratiche (Operatore + Supervisore) | f016 | fa-file | Lista pratiche / record |
| Home (Supervisore) | f015 | fa-home | Pagina principale / Dashboard |
| Riassegna Attività | f0c0 | fa-group | Riassegnazione / team |

---

### Palette Applicativa (sintesi)

| Token | Valore | Utilizzo |
|---|---|---|
| Accent / Brand Blue | `#0047BB` | Navbar selected tab, link ACCENT, icone tab, contatori, loading bar |
| Brand Yellow | `#FFEC00` | Billboard background dashboard Supervisore (ruolo distinguisher) |
| Positive Green | sistema `POSITIVE` | Esito APPROVATA, icon check-circle |
| Negative Red | sistema `NEGATIVE` | Esito RESPINTA, icon times-circle |
| Neutral Grey | sistema `STANDARD` | Card esiti non attivi |
| Brand Green | `#008000` | "Pratiche Chiuse" counter in dashboard |
| Box style | `BOA_STYLE_POSTE` | Classe CSS custom per boxLayout principali (colore bordo/sfondo Poste) |


---

## STEP 9 — UX Reconstruction Handoff

### Application UX Summary

```
Application:        Attivazione Nuova Carta
Prefix:             BOA_ANC
Export version:     20260506

Sites:              2
  - Scrivania Digitale Operatore ANC   (/scrivania-digitale-operatore-anc)   — 3 pages
  - Scrivania Digitale Supervisore ANC (/scrivania-digitale-supervisore-anc) — 3 pages

Pages (total):      6
  Entry points:
    /scrivania-digitale-operatore-anc/home
    /scrivania-digitale-operatore-anc/task
    /scrivania-digitale-operatore-anc/pratiche
    /scrivania-digitale-supervisore-anc/home
    /scrivania-digitale-supervisore-anc/pratiche
    /scrivania-digitale-supervisore-anc/riassegna-attivita

Interface Rules:    26
  Dashboard:        2  (BOA_ANC_Interfaccia_DashBoard_Operatore, BOA_ANC_Interfaccia_DashBoard_Supervisore)
  List:             2  (BOA_ANC_ListaAttivita, Pratica_ANC List View)
  Form (Task):      3  (BOA_ANC_Task_Lavorazione, BOA_ANC_Task_TipizzazioneDocumenti, BOA_ANC_Intertfaccia_RiassegnazioneTask)
  View (Summary):   1  (BOA_ANC_Summary)
  View (sezione):   9  (DatiPratica, DatiCliente, IndirizzoResidenza, DatiCarta, Documenti,
                         Contenuti_Section, Task_VerificaDocumenti, Task_Riepilogo, Summary_Esito)
  Form (filtri):    2  (BOA_ANC_FiltriUtente_ListaAttivita, BOA_ANC_Filtri_TaskReportSupervisoreGruppi)
  Component (nav):  1  (BOA_ANC_Task_MenuLaterale)
  Component (header):1 (BOA_ANC_Header)
  Component (list): 1  (BOA_ANC_EsitiAttivita)
  Checklist:        2  (BOA_ANC_Task_CheckList, BOA_ANC_CheckList_Section)
  Grafici:          3  (GraficiPraticheGiornaliere_Section, GraficiPraticheGiornaliereLavorate_Section, GraficiPraticheByStato_Section)

Record Types:       1  (Pratica_ANC — list view + summary view)

Theme:
  Brand color:      #0047BB (Poste Italiane blue)
  Accent:           #0047BB
  Button shape:     SQUARED
  Nav style:        TOPBAR
  Button labels:    UPPERCASE
  Logo:             Poste Italiane (URL esterno)
```

---

### Screen Catalogue

| # | Screen ID | Screen Name | Type | URL | Componenti (count) | Interazioni | Reusable Pattern |
|---|---|---|---|---|---|---|---|
| 1 | BOA_ANC_Interfaccia_DashBoard_Operatore | Dashboard Operatore | Dashboard | /scrivania-digitale-operatore-anc/home | 15+ | navigate, start-process, edit-toggle, submit | Dashboard/Card Grid + Create/Edit/View |
| 2 | BOA_ANC_ListaAttivita | Lista Attività | List | /scrivania-digitale-operatore-anc/task | 8 | search/filter, submit, navigate | List/Detail + Search/Result |
| 3 | Pratica_ANC (List View) | Pratiche (Lista) | List | /scrivania-digitale-operatore-anc/pratiche  /scrivania-digitale-supervisore-anc/pratiche | 14 colonne | navigate (recordLink) | List/Detail |
| 4 | BOA_ANC_Interfaccia_DashBoard_Supervisore | Dashboard Supervisore | Dashboard | /scrivania-digitale-supervisore-anc/home | 18+ | navigate, start-process | Dashboard/Card Grid |
| 5 | BOA_ANC_Intertfaccia_RiassegnazioneTask | Riassegnazione Task | Form+List | /scrivania-digitale-supervisore-anc/riassegna-attivita (via processo) | 8 | search/filter, multi-select, submit | Search/Result |
| 6 | BOA_ANC_Task_Lavorazione | Lavorazione Pratica | Form (multi-step) | (task — aperto da grid attività) | 10+ | tab-navigate, visibility-toggle, submit, start-process | Multi-step (custom wizard) + List/Detail |
| 7 | BOA_ANC_Task_TipizzazioneDocumenti | Tipizzazione Documento | Form (Task) | (task — processo BOA_ANC_Processo_TipizzaDoc) | 4 | submit, modal-confirm | Modal Confirmation |
| 8 | BOA_ANC_Summary | Summary Pratica | View (Record Summary) | (record summary — da recordLink) | 8+ | tab-navigate, start-process (azioni correlate) | Tab Navigation + List/Detail |
| 9 | BOA_ANC_Sezione_DatiPratica | Dati Pratica | View (sezione) | embedded in Task_Lavorazione, Summary | 9 | — | — |
| 10 | BOA_ANC_Task_VerificaDocumenti | Verifica Documenti | View (sezione) | embedded in Task_Lavorazione, Summary | 8 | visibility-toggle, navigate (download) | — |
| 11 | BOA_ANC_Task_Riepilogo | Riepilogo Esito | View+Form (sezione) | embedded in Task_Lavorazione | 5 | — | Status/Milestone Display |
| 12 | BOA_ANC_Summary_Esito | Esito Verifiche | View (sezione) | embedded in Summary | 6 | — | — |
| 13 | BOA_ANC_Sezione_DatiCliente | Dati Cliente | View (sezione) | embedded in Task_VerificaDocumenti | 11 | — | Collapsible Section |
| 14 | BOA_ANC_Sezione_DatiCarta | Dati Carta Bloccata | View (sezione) | embedded in Task_VerificaDocumenti | 3 | — | Collapsible Section |
| 15 | BOA_ANC_Header | Header Pratica | Component (header) | embedded in Summary | 4 | — | Status/Milestone Display |

---

### Component Summary

| Tipo Componente | Count | Schermate (IR) |
|---|---|---|
| `a!billboardLayout_v1` | 3 | BOA_ANC_Interfaccia_DashBoard_Operatore, BOA_ANC_Interfaccia_DashBoard_Supervisore, BOA_ANC_Header |
| `a!milestoneField` | 1 | BOA_ANC_Header |
| `a!gridField_v1` | 4 | BOA_ANC_ListaAttivita (×2), BOA_ANC_Intertfaccia_RiassegnazioneTask, Pratica_ANC List View |
| `recordGridField` | 1 | Pratica_ANC List View |
| `a!boxLayout` (BOA_STYLE_POSTE) | 8+ | BOA_ANC_Sezione_DatiPratica, BOA_ANC_Sezione_DatiCliente, BOA_ANC_Sezione_DatiCarta, BOA_ANC_Sezione_Documenti, BOA_ANC_Summary_Esito, BOA_ANC_Intertfaccia_RiassegnazioneTask (×2), BOA_ANC_Task_TipizzazioneDocumenti (×2) |
| `a!boxLayout` (isCollapsible: true) | 3 | BOA_ANC_Sezione_DatiCliente, BOA_ANC_Sezione_DatiCarta, BOA_ANC_Task_TipizzazioneDocumenti (col-right) |
| `a!cardLayout` (style: INFO) | 3 | BOA_ANC_Sezione_Documenti, BOA_ANC_Task_TipizzazioneDocumenti (×2) |
| `a!cardLayout` (showBorder: false, tab content) | 3 | BOA_ANC_Summary (×3 per tab) |
| `a!columnsLayout` | 8+ | BOA_ANC_Task_Lavorazione, BOA_ANC_Task_VerificaDocumenti, BOA_ANC_Sezione_DatiCliente, BOA_ANC_Summary, BOA_ANC_Intertfaccia_RiassegnazioneTask, BOA_ANC_Task_Riepilogo, BOA_ANC_Header, BOA_ANC_Task_TipizzazioneDocumenti |
| `a!sideBySideLayout` | 6+ | BOA_ANC_FiltriUtente_ListaAttivita (×2), BOA_ANC_Summary (×2), BOA_ANC_Sezione_DatiCliente (×4), BOA_ANC_Intertfaccia_RiassegnazioneTask |
| `a!dropdownField_v1` | 3 | BOA_ANC_FiltriUtente_ListaAttivita, BOA_ANC_Interfaccia_DashBoard_Operatore (card link), BOA_ANC_Task_TipizzazioneDocumenti |
| `a!multipleDropdownField_v1` | 1 | BOA_ANC_FiltriUtente_ListaAttivita ("Tipo Pratica" — disabled) |
| `a!radioButtonField` | 1 | BOA_ANC_Intertfaccia_RiassegnazioneTask |
| `a!checkboxField` | 1 | BOA_ANC_ListaAttivita |
| `a!pickerFieldUsersAndGroups` | 1 | BOA_ANC_FiltriUtente_ListaAttivita |
| `a!pickerFieldUsers` | 2 | BOA_ANC_FiltriUtente_ListaAttivita, BOA_ANC_Intertfaccia_RiassegnazioneTask |
| `a!textField` (readonly) | 20+ | BOA_ANC_Sezione_DatiPratica (×7), BOA_ANC_Sezione_DatiCliente (×10), BOA_ANC_Sezione_DatiCarta (×3), BOA_ANC_Summary_Esito (×3) |
| `a!textField` (editable) | 3 | BOA_ANC_Interfaccia_DashBoard_Operatore (card form: Titolo Link, Link), BOA_ANC_FiltriUtente_ListaAttivita (×2) |
| `a!paragraphField` (editable) | 1 | BOA_ANC_Task_Riepilogo ("Note Interne") |
| `a!paragraphField` (readonly) | 1 | BOA_ANC_Summary_Esito ("Testo" nota) |
| `a!dateField` (readonly) | 1 | BOA_ANC_Summary_Esito ("Data Esito SD") |
| `a!richTextDisplayField` | 15+ | BOA_ANC_Interfaccia_DashBoard_Operatore (contatori, azioni), BOA_ANC_Summary (tab labels), BOA_ANC_Task_Riepilogo (esito), BOA_ANC_Intertfaccia_RiassegnazioneTask (processo), spacer multipli |
| `a!imageField_v1` (style: AVATAR) | 2 | BOA_ANC_Interfaccia_DashBoard_Operatore, BOA_ANC_Header |
| `a!ButtonWidget` PRIMARY | 10+ | Task_Lavorazione, FiltriUtente, DashBoard_Operatore, DashBoard_Supervisore, Task_TipizzazioneDocumenti, Task_VerificaDocumenti, Intertfaccia_RiassegnazioneTask |
| `a!ButtonWidget` SECONDARY | 3 | BOA_ANC_Task_Lavorazione, BOA_ANC_Task_VerificaDocumenti |
| `a!ButtonWidget` DESTRUCTIVE | 1 | BOA_ANC_FiltriUtente_ListaAttivita ("Applica Filtri") |
| `a!dynamicLink` | 8+ | BOA_ANC_Interfaccia_DashBoard_Operatore (×3), BOA_ANC_Summary (×3), BOA_ANC_Task_MenuLaterale (×3), BOA_ANC_Sezione_Documenti |
| `a!safeLink` | 1+ | BOA_ANC_Interfaccia_DashBoard_Operatore (link favoriti) |
| `a!startProcessLink` (via ButtonWidget) | 5 | BOA_ANC_Interfaccia_DashBoard_Operatore, BOA_ANC_Task_Lavorazione (×3), BOA_ANC_FiltriUtente_ListaAttivita, BOA_ANC_Intertfaccia_RiassegnazioneTask, BOA_ANC_Task_TipizzazioneDocumenti |
| `a!recordLink` | 1 | Pratica_ANC List View ("Pratica N.") |
| `a!forEach` (renderList) | 5 | BOA_ANC_Interfaccia_DashBoard_Operatore (azioni, link favoriti), BOA_ANC_Sezione_Documenti, BOA_ANC_Task_Riepilogo (esiti), BOA_ANC_Task_MenuLaterale |


---

### Navigation Map (Machine-readable)

```json
{
  "application": "Attivazione Nuova Carta",
  "prefix": "BOA_ANC",
  "sites": [
    {
      "name": "Scrivania Digitale Operatore ANC",
      "baseUrl": "/scrivania-digitale-operatore-anc",
      "accentColor": "#0047BB",
      "navStyle": "TOPBAR",
      "buttonShape": "SQUARED",
      "pages": [
        {
          "name": "Home",
          "url": "/scrivania-digitale-operatore-anc/home",
          "icon": "f015",
          "iconName": "fa-home",
          "visibility": "always",
          "width": "WIDE",
          "interfaceRule": "BOA_ANC_Interfaccia_DashBoard_Operatore",
          "type": "Interface Rule"
        },
        {
          "name": "Attività",
          "url": "/scrivania-digitale-operatore-anc/task",
          "icon": "f0ca",
          "iconName": "fa-list-ul",
          "visibility": "always",
          "width": "WIDE",
          "interfaceRule": "BOA_ANC_ListaAttivita",
          "type": "Interface Rule"
        },
        {
          "name": "Pratiche",
          "url": "/scrivania-digitale-operatore-anc/pratiche",
          "icon": "f016",
          "iconName": "fa-file",
          "visibility": "always",
          "width": "WIDE",
          "interfaceRule": "Pratica_ANC",
          "type": "Record Type"
        }
      ]
    },
    {
      "name": "Scrivania Digitale Supervisore ANC",
      "baseUrl": "/scrivania-digitale-supervisore-anc",
      "accentColor": "#0047BB",
      "navStyle": "TOPBAR",
      "buttonShape": "SQUARED",
      "pages": [
        {
          "name": "Home",
          "url": "/scrivania-digitale-supervisore-anc/home",
          "icon": "f015",
          "iconName": "fa-home",
          "visibility": "always",
          "width": "WIDE",
          "interfaceRule": "BOA_ANC_Interfaccia_DashBoard_Supervisore",
          "type": "Interface Rule"
        },
        {
          "name": "Pratiche",
          "url": "/scrivania-digitale-supervisore-anc/pratiche",
          "icon": "f016",
          "iconName": "fa-file",
          "visibility": "always",
          "width": "WIDE",
          "interfaceRule": "Pratica_ANC",
          "type": "Record Type"
        },
        {
          "name": "Riassegna Attività",
          "url": "/scrivania-digitale-supervisore-anc/riassegna-attivita",
          "icon": "f0c0",
          "iconName": "fa-group",
          "visibility": "always",
          "width": "WIDE",
          "interfaceRule": "BOA_ANC_Intertfaccia_RiassegnazioneTask",
          "type": "Process Model (generates task form)"
        }
      ]
    }
  ],
  "navigationFlows": [
    {
      "from": "BOA_ANC_Interfaccia_DashBoard_Operatore",
      "trigger": "richTextItem \"{nomeAzione}\" (forEach azioni gruppo)",
      "type": "start-process",
      "target": "{processo azione — dinamico per gruppo}"
    },
    {
      "from": "BOA_ANC_Interfaccia_DashBoard_Operatore",
      "trigger": "richTextItem \"{titoloLink}\" (link favorito)",
      "type": "navigate",
      "target": "{url favorito — safeLink}"
    },
    {
      "from": "BOA_ANC_Interfaccia_DashBoard_Operatore",
      "trigger": "Button \"Salva\" (card form link favoriti)",
      "type": "submit",
      "target": "writeToDataStore (salva link favorito)"
    },
    {
      "from": "BOA_ANC_ListaAttivita",
      "trigger": "Button \"Applica e Salva Filtri\"",
      "type": "submit",
      "target": "BOA_ANC_SalvaFiltriUtente"
    },
    {
      "from": "BOA_ANC_ListaAttivita",
      "trigger": "Row click grid attività",
      "type": "navigate",
      "target": "BOA_ANC_Task_Lavorazione (task form)"
    },
    {
      "from": "BOA_ANC_Task_Lavorazione",
      "trigger": "Button \"Salva e prosegui\"",
      "type": "submit",
      "target": "BOA_ANC_SalvataggioDati → activeSection=3"
    },
    {
      "from": "BOA_ANC_Task_Lavorazione",
      "trigger": "Button \"chiudi pratica\"",
      "type": "submit",
      "target": "BOA_ANC_SalvataggioDati → chiude pratica"
    },
    {
      "from": "BOA_ANC_Task_Lavorazione",
      "trigger": "Button \"Modifica\"",
      "type": "submit",
      "target": "BOA_ANC_SalvataggioDati → reset esitoSD"
    },
    {
      "from": "BOA_ANC_Sezione_Documenti",
      "trigger": "dynamicLink \"QUI\" (download documento)",
      "type": "start-process",
      "target": "BOA_ANC_ScaricaSingoloDoc"
    },
    {
      "from": "BOA_ANC_Summary",
      "trigger": "dynamicLink \"Dati Pratica\"",
      "type": "tab-navigate",
      "target": "local!selezione = \"Pratica\""
    },
    {
      "from": "BOA_ANC_Summary",
      "trigger": "dynamicLink \"Dati Lavorazione\"",
      "type": "tab-navigate",
      "target": "local!selezione = \"Video\""
    },
    {
      "from": "BOA_ANC_Summary",
      "trigger": "dynamicLink \"Esito\"",
      "type": "tab-navigate",
      "target": "local!selezione = \"Esito\""
    },
    {
      "from": "BOA_ANC_Task_TipizzazioneDocumenti",
      "trigger": "Button \"Conferma\"",
      "type": "submit+modal-confirm",
      "target": "BOA_ANC_Processo_TipizzaDoc"
    },
    {
      "from": "BOA_ANC_Intertfaccia_RiassegnazioneTask",
      "trigger": "Button \"Riassegna\"",
      "type": "submit",
      "target": "BOA_ANC_ProcessoSupervisore_RiassegnazioneTask"
    },
    {
      "from": "Pratica_ANC (List View)",
      "trigger": "recordLink \"Pratica N.\" / row click",
      "type": "navigate",
      "target": "BOA_ANC_Summary (Record Summary View)"
    }
  ]
}
```


---

### Screen Specifications (Machine-readable)

```json
{
  "screenCatalogue": [
    {
      "screenId": "BOA_ANC_Interfaccia_DashBoard_Operatore",
      "screenName": "Dashboard Operatore",
      "type": "Dashboard",
      "url": "/scrivania-digitale-operatore-anc/home",
      "width": "WIDE",
      "header": null,
      "sections": [
        {
          "title": "Billboard Hero",
          "layout": "billboard",
          "billboardHeight": "MEDIUM",
          "columns": [
            {
              "position": "left",
              "fields": [
                {"order":1,"sailType":"a!imageField_v1","uxType":"avatar","label":"","state":"display-only","binding":"loggedInUser()"},
                {"order":2,"sailType":"a!richTextDisplayField","uxType":"display","label":"Nome Cognome utente","state":"display-only","textSize":"LARGE","textStyle":"STRONG"},
                {"order":3,"sailType":"a!richTextDisplayField","uxType":"display","label":"Ruolo","state":"display-only","textStyle":"EMPHASIS"}
              ]
            },
            {
              "position": "right",
              "fields": [
                {"order":1,"sailType":"a!richTextDisplayField","uxType":"display","label":"N° Attività","state":"display-only","binding":"local!taskReport.totalCount"},
                {"order":2,"sailType":"a!richTextDisplayField","uxType":"display","label":"Pratiche Attive","state":"display-only","color":"ACCENT","binding":"local!casiAttiviReport.totalCount"},
                {"order":3,"sailType":"a!richTextDisplayField","uxType":"display","label":"Pratiche Chiuse","state":"display-only","color":"#008000","binding":"local!casiChiusi.totalCount"}
              ]
            }
          ]
        },
        {
          "title": "Azioni",
          "layout": "boxLayout",
          "boxStyle": "BOA_STYLE_POSTE",
          "columns": [
            {
              "position": "full",
              "fields": [
                {"order":1,"sailType":"a!forEach(richTextIcon+richTextItem)","uxType":"action-link","label":"{nomeAzione}","state":"display-only","interaction":"startProcessLink → {processo azione}"}
              ]
            }
          ]
        },
        {
          "title": "Link Favoriti",
          "layout": "boxLayout",
          "boxStyle": "BOA_STYLE_POSTE",
          "columns": [
            {
              "position": "full",
              "fields": [
                {"order":1,"sailType":"a!forEach(richTextItem)","uxType":"link","label":"{titoloLink}","state":"display-only","showWhen":"and(local!utenteLinkFavoriti<>\"\", not(local!gestisciLinks))","interaction":"safeLink → url favorito"},
                {"order":2,"sailType":"a!richTextDisplayField","uxType":"display","label":"Nessun link presente","state":"display-only","showWhen":"and(local!utenteLinkFavoriti=\"\", not(local!gestisciLinks))"},
                {"order":3,"sailType":"a!richTextIcon","uxType":"icon-button","label":"Aggiungi nuovo link","icon":"plus-square-o","state":"display-only","showWhen":"not(local!gestisciLinks)","interaction":"dynamicLink → local!gestisciLinks=true"},
                {"order":4,"sailType":"a!cardLayout","uxType":"inline-form","label":"","state":"display-only","showWhen":"local!gestisciLinks=true",
                  "fields": [
                    {"order":1,"sailType":"a!textField","uxType":"text","label":"Titolo Link","state":"editable","required":true,"binding":"local!gestionelinkFavorito.titoloLink"},
                    {"order":2,"sailType":"a!textField","uxType":"text","label":"Link","state":"editable","required":true,"validation":"\"Inserire un link valido.\"","binding":"local!gestionelinkFavorito.link"},
                    {"order":3,"sailType":"a!dropdownField_v1","uxType":"dropdown","label":"Tipo Link","state":"editable","required":true,"options":[{"label":"Interno","value":"Interno"},{"label":"Esterno","value":"Esterno"},{"label":"Legacy","value":"Legacy"}],"binding":"local!gestionelinkFavorito.tipo"}
                  ]
                }
              ]
            }
          ]
        }
      ],
      "actions": [
        {"order":1,"label":"Esci","style":"PRIMARY","type":"edit-toggle","target":"local!gestisciLinks=false","showWhen":"local!gestisciLinks=true"},
        {"order":2,"label":"Salva","style":"PRIMARY","type":"submit","target":"writeToDataStore","showWhen":"local!gestisciLinks=true"}
      ]
    },
    {
      "screenId": "BOA_ANC_ListaAttivita",
      "screenName": "Lista Attività",
      "type": "List",
      "url": "/scrivania-digitale-operatore-anc/task",
      "width": "WIDE",
      "header": null,
      "sections": [
        {
          "title": "Filtri (BOA_ANC_FiltriUtente_ListaAttivita)",
          "layout": "embedded-form",
          "columns": [
            {
              "position": "full",
              "fields": [
                {"order":1,"sailType":"a!dropdownField_v1","uxType":"dropdown","label":"Stato","state":"editable","placeholder":"Tutti","options":[{"label":"In Coda","value":0},{"label":"In Lavorazione","value":1}]},
                {"order":2,"sailType":"a!multipleDropdownField_v1","uxType":"multi-dropdown","label":"Tipo Pratica","state":"disabled","placeholder":"Tutti","note":"sempre disabilitato (hardcoded disabled:true)"},
                {"order":3,"sailType":"a!textField","uxType":"text","label":"Pratica N.","state":"editable"},
                {"order":4,"sailType":"a!textField","uxType":"text","label":"Nome Attività","state":"editable"},
                {"order":5,"sailType":"a!pickerFieldUsersAndGroups","uxType":"user-group-picker","label":"Assegnatari","state":"editable","maxSelections":1,"groupFilter":"BOA ANC Operatori","disabled":"isnull(ri!tipoProcesso)"},
                {"order":6,"sailType":"a!pickerFieldUsers","uxType":"user-picker","label":"Utente in carico","state":"editable","maxSelections":1,"groupFilter":"BOA ALL Users"}
              ]
            }
          ]
        },
        {
          "title": "(checkbox)",
          "layout": "single-column",
          "columns": [
            {
              "position": "full",
              "fields": [
                {"order":1,"sailType":"a!checkboxField","uxType":"checkbox","label":"Visualizza le attività a me assegnate","state":"editable","disabled":"isnull(local!myTasksTipoProcesso)","binding":"local!currentUser"}
              ]
            }
          ]
        },
        {
          "title": "Ultimi N Filtri Salvati",
          "layout": "boxLayout",
          "columns": [
            {
              "position": "full",
              "fields": [
                {"order":1,"sailType":"a!gridField_v1","uxType":"grid","label":"","state":"readonly","selectionStyle":"ROW_HIGHLIGHT","instructions":"Selezionare un filtro per utilizzarlo e cliccare su applica filtri",
                  "columns": [
                    {"label":"Stato","type":"text"},{"label":"Tipo Pratica","type":"text"},{"label":"Pratica N.","type":"text"},{"label":"Nome Attività","type":"text"},{"label":"Data Scadenza Da","type":"date"},{"label":"Data Scadenza A","type":"date"},{"label":"Assegnatario","type":"text"},{"label":"Utente in carico","type":"text"}
                  ],
                  "rowAction": "select → popola filtri"
                }
              ]
            }
          ]
        },
        {
          "title": "Grid Attività",
          "layout": "single-column",
          "columns": [
            {
              "position": "full",
              "fields": [
                {"order":1,"sailType":"a!gridField_v1","uxType":"grid","label":"","state":"readonly","pageSize":8,"sort":"c1 desc, c10 desc, c3 desc",
                  "columns": [
                    {"label":"Nome Attività","type":"richText"},{"label":"Stato","type":"text"},{"label":"Tipo Pratica","type":"text"},{"label":"Pratica N.","type":"text"},{"label":"Assegnatario","type":"text"},{"label":"Utente in carico","type":"text"},{"label":"Data Scadenza","type":"date"}
                  ],
                  "rowAction": "navigate → BOA_ANC_Task_Lavorazione"
                }
              ]
            }
          ]
        }
      ],
      "actions": [
        {"order":1,"label":"Applica Filtri","style":"DESTRUCTIVE","type":"search/filter","target":"toggle local!applicaFiltri"},
        {"order":2,"label":"Applica e Salva Filtri","style":"PRIMARY","type":"submit","target":"BOA_ANC_SalvaFiltriUtente"}
      ]
    },
    {
      "screenId": "Pratica_ANC_ListView",
      "screenName": "Pratiche (Lista Record)",
      "type": "List",
      "url": "/scrivania-digitale-operatore-anc/pratiche AND /scrivania-digitale-supervisore-anc/pratiche",
      "width": "WIDE",
      "header": null,
      "sections": [
        {
          "title": "Grid Pratiche",
          "layout": "single-column",
          "columns": [
            {
              "position": "full",
              "fields": [
                {"order":1,"sailType":"recordGridField","uxType":"grid","label":"","state":"readonly","sortable":true,
                  "columns": [
                    {"label":"Id","type":"number","showWhen":false},
                    {"label":"Pratica N.","type":"recordLink","interaction":"navigate → BOA_ANC_Summary"},
                    {"label":"Canale","type":"text","showWhen":false},
                    {"label":"Codice Fiscale","type":"text"},
                    {"label":"Codice Cliente","type":"text"},
                    {"label":"Data Apertura","type":"date"},
                    {"label":"Data Ultima Modifica","type":"date"},
                    {"label":"Data Chiusura","type":"date"},
                    {"label":"Data Inserimento Richiesta","type":"date"},
                    {"label":"Esito SD","type":"text"},
                    {"label":"Operatore","type":"text"},
                    {"label":"Stato","type":"text"},
                    {"label":"Data Scadenza","type":"date","showWhen":false},
                    {"label":"Segnalazioni","type":"richTextIcon"}
                  ],
                  "rowAction": "recordLink → BOA_ANC_Summary"
                }
              ]
            }
          ]
        }
      ],
      "actions": []
    },
    {
      "screenId": "BOA_ANC_Task_Lavorazione",
      "screenName": "Lavorazione Pratica",
      "type": "Form (multi-step)",
      "url": "(task aperto da grid attività o avvio attività)",
      "width": "WIDE",
      "header": null,
      "layout": "2-col sidebar+content",
      "sidebar": {
        "component": "BOA_ANC_Task_MenuLaterale",
        "width": "NARROW (expanded) / EXTRA_NARROW (collapsed)",
        "items": [
          {"step":1,"label":"Dati Pratica","icon":"briefcase","activatesSection":1},
          {"step":2,"label":"Verifica Documento","icon":"check-square-o","activatesSection":2},
          {"step":3,"label":"Riepilogo","icon":"address-card-o","activatesSection":3,"linkShowWhen":"isNotNullOrEmpty(ri!pratica.esitoSD)"}
        ]
      },
      "contentSections": [
        {
          "activatedBy": "local!activeCollapsibleNavSection=1",
          "component": "BOA_ANC_Sezione_DatiPratica",
          "readOnly": "false"
        },
        {
          "activatedBy": "local!activeCollapsibleNavSection=2",
          "component": "BOA_ANC_Task_VerificaDocumenti",
          "readOnly": "isNotNullOrEmpty(ri!pratica.esitoSD)"
        },
        {
          "activatedBy": "local!activeCollapsibleNavSection=3",
          "component": "BOA_ANC_Task_Riepilogo",
          "readOnly": "editable (Note Interne) se esito negativo"
        }
      ],
      "actions": [
        {"order":1,"label":"chiudi pratica","style":"PRIMARY","type":"submit","target":"BOA_ANC_SalvataggioDati","showWhen":"local!activeCollapsibleNavSection=3","validate":true},
        {"order":2,"label":"Salva e prosegui","style":"PRIMARY","type":"submit","target":"BOA_ANC_SalvataggioDati → activeSection=3","showWhen":"local!activeCollapsibleNavSection=2","disabled":"isNotNullOrEmpty(ri!pratica.esitoSD)","validate":true},
        {"order":3,"label":"Modifica","style":"SECONDARY","type":"submit","target":"BOA_ANC_SalvataggioDati → reset esitoSD","showWhen":"always"}
      ]
    },
    {
      "screenId": "BOA_ANC_Task_TipizzazioneDocumenti",
      "screenName": "Tipizzazione Documento",
      "type": "Form (Task)",
      "url": "(task — processo BOA_ANC_Processo_TipizzaDoc)",
      "width": "WIDE",
      "sections": [
        {
          "title": "No contenuti",
          "showWhen": "isNullOrEmpty(ri!contenuti)",
          "columns": [
            {"position":"full","fields":[
              {"order":1,"sailType":"a!cardLayout","uxType":"info-card","label":"Nessun contenuto associato alla pratica","state":"display-only"}
            ]}
          ]
        },
        {
          "title": "Tipizzazione Documento",
          "showWhen": "isNotNullOrEmpty(ri!contenuti)",
          "layout": "2-col",
          "columns": [
            {
              "position": "left",
              "fields": [
                {"order":1,"sailType":"a!cardLayout","uxType":"info-card","label":"(istruzioni tipizzazione)","state":"display-only"},
                {"order":2,"sailType":"a!dropdownField_v1","uxType":"dropdown","label":"Tipo Documento","state":"editable","required":true,"placeholder":"--- Selezionare il tipo documento ---","options":"ref: BOA_ANC_GetTipiDocumentoAttivi (descrizione / codiceDocId)","binding":"local!tipoDocSelected"}
              ]
            },
            {
              "position": "right",
              "fields": [
                {"order":1,"sailType":"a!boxLayout","uxType":"document-preview","label":"{ri!documento.descrizione}","state":"display-only","isCollapsible":true,"note":"forEach ri!contenuti: visualizzazione file"}
              ]
            }
          ]
        }
      ],
      "actions": [
        {"order":1,"label":"Conferma","style":"PRIMARY","type":"submit","target":"BOA_ANC_Processo_TipizzaDoc","disabled":"isNullOrEmpty(local!tipoDocSelected)","confirmHeader":"ATTENZIONE","confirmMessage":"E' stato selezionato {local!tipoDocSelected.descrizione} come tipologia. Attenzione: non sarà possibile modificare il tipo documento in futuro. Confermare la selezione?","validate":true}
      ]
    },
    {
      "screenId": "BOA_ANC_Summary",
      "screenName": "Summary Pratica",
      "type": "View (Record Summary)",
      "url": "(record summary — da recordLink Pratica_ANC)",
      "width": "WIDE",
      "header": "BOA_ANC_Header (milestoneField stati + info pratica)",
      "tabs": [
        {"key":"Pratica","label":"Dati Pratica","icon":"file-o","component":"BOA_ANC_Sezione_DatiPratica","readOnly":true},
        {"key":"Video","label":"Dati Lavorazione","icon":"file-image-o","component":"BOA_ANC_Task_VerificaDocumenti","readOnly":true},
        {"key":"Esito","label":"Esito","icon":"legal","component":"BOA_ANC_Summary_Esito","readOnly":true}
      ],
      "tabNavigation": "dynamicLink → local!selezione",
      "actions": []
    },
    {
      "screenId": "BOA_ANC_Sezione_DatiPratica",
      "screenName": "Dati Pratica (sezione)",
      "type": "View (sezione embedded)",
      "url": "embedded in BOA_ANC_Task_Lavorazione (tab 1), BOA_ANC_Summary (tab Pratica)",
      "width": "WIDE",
      "header": "BOX label: \"Dati Pratica\" (BOA_STYLE_POSTE)",
      "sections": [
        {
          "layout": "sideBySideLayout rows",
          "columns": [
            {"position":"full","fields":[
              {"order":1,"sailType":"a!textField","uxType":"text","label":"Data Apertura","state":"readonly","binding":"ri!pratica.dataApertura","format":"dd/mm/yyyy HH:mm:ss"},
              {"order":2,"sailType":"a!textField","uxType":"text","label":"Data Ultima Modifica","state":"readonly","binding":"ri!pratica.dataUltimaModifica","format":"dd/mm/yyyy HH:mm:ss"},
              {"order":3,"sailType":"a!textField","uxType":"text","label":"Stato","state":"readonly","binding":"ri!pratica.stato"},
              {"order":4,"sailType":"a!textField","uxType":"text","label":"Codice Cliente","state":"readonly","binding":"ri!pratica.codiceCliente","default":"\" \""},
              {"order":5,"sailType":"a!textField","uxType":"text","label":"Codice Fiscale","state":"readonly","binding":"ri!pratica.codiceFiscale","default":"\"Codice Fiscale non indicato\""},
              {"order":6,"sailType":"a!textField","uxType":"text","label":"Canale","state":"readonly","binding":"ri!pratica.canale"},
              {"order":7,"sailType":"a!textField","uxType":"text","label":"Data Chiusura","state":"readonly","showWhen":"isNotNullOrEmpty(pratica.dataChiusura)","binding":"ri!pratica.dataChiusura"},
              {"order":8,"sailType":"a!textField","uxType":"text","label":"Esito SD","state":"readonly","showWhen":"and(isNotNullOrEmpty(pratica.esitoSD), ri!readOnly)","binding":"ri!pratica.esitoSD","default":"\"Nessun esito ancora definito\""}
            ]}
          ]
        }
      ],
      "actions": []
    },
    {
      "screenId": "BOA_ANC_Summary_Esito",
      "screenName": "Esito Verifiche Back Office (sezione)",
      "type": "View (sezione embedded)",
      "url": "embedded in BOA_ANC_Summary (tab Esito)",
      "header": "BOX label: \"Esito Verifiche Back Office\" (BOA_STYLE_POSTE)",
      "sections": [
        {
          "layout": "sideBySideLayout",
          "columns": [
            {"position":"full","fields":[
              {"order":1,"sailType":"a!textField","uxType":"text","label":"Esito Scrivania Digitale","state":"readonly","binding":"ri!pratica.esitoSD","default":"\"La pratica non è stata ancora esitata\""},
              {"order":2,"sailType":"a!dateField","uxType":"date","label":"Data Esito Scrivania Digitale","state":"readonly","showWhen":"isNotNullOrEmpty(ri!pratica.dataEsitoSD)","binding":"ri!pratica.dataEsitoSD","format":"dd/mm/yyyy HH:mm"}
            ]}
          ]
        },
        {
          "title": "Note",
          "layout": "boxLayout (BOA_STYLE_POSTE)",
          "showWhen": "isNotNullOrEmpty(ri!nota)",
          "columns": [
            {"position":"full","fields":[
              {"order":1,"sailType":"a!textField","uxType":"text","label":"Data","state":"readonly","binding":"ri!nota.datacreazione","format":"dd/mm/yyyy HH:mm:ss"},
              {"order":2,"sailType":"a!textField","uxType":"text","label":"Operatore","state":"readonly","binding":"ri!nota.operatore"},
              {"order":3,"sailType":"a!paragraphField","uxType":"textarea","label":"Testo","state":"readonly","binding":"ri!nota.testoNota"}
            ]}
          ]
        }
      ],
      "actions": []
    },
    {
      "screenId": "BOA_ANC_Intertfaccia_RiassegnazioneTask",
      "screenName": "Riassegnazione Task",
      "type": "Form+List",
      "url": "/scrivania-digitale-supervisore-anc/riassegna-attivita (via processo)",
      "width": "WIDE",
      "sections": [
        {
          "title": "Dettagli riassegnazione",
          "layout": "boxLayout (BOA_STYLE_POSTE)",
          "columns": [
            {
              "position": "left",
              "fields": [
                {"order":1,"sailType":"a!richTextDisplayField","uxType":"display","label":"Processo: {BOA_ANC_NOMEAPPLICAZIONE}","state":"display-only"},
                {"order":2,"sailType":"a!radioButtonField","uxType":"radio","label":"Scegli Tipologia di Riassegnazione","state":"editable","required":true,"showWhen":"not(isnull(local!processo))","options":[{"label":"Riassegna Attività al Gruppo Operatore","value":"BOA ANC Operatori"},{"label":"Riassegna Attività a Utenti","value":"Utenti"}],"binding":"local!tipoRiassegnazione"}
              ]
            },
            {
              "position": "right",
              "fields": [
                {"order":1,"sailType":"a!pickerFieldUsers","uxType":"user-picker","label":"Inserisci utenti a cui assegnare le attività","state":"editable","required":true,"showWhen":"local!tipoRiassegnazione=\"Utenti\"","groupFilter":"BOA ANC Operatori","binding":"local!nuoviUtentiAssegnatari"}
              ]
            }
          ]
        },
        {
          "title": "Le attività dei miei processi",
          "layout": "boxLayout (BOA_STYLE_POSTE)",
          "showWhen": "not(isnull(local!processo))",
          "columns": [
            {"position":"full","fields":[
              {"order":1,"sailType":"BOA_ANC_Filtri_TaskReportSupervisoreGruppi","uxType":"embedded-filter-form","label":""},
              {"order":2,"sailType":"a!gridField_v1","uxType":"grid","label":"","state":"readonly","selectable":true,"selectionRequired":true,"pageSize":8,
                "columns":[
                  {"label":"Processo","type":"text"},{"label":"Pratica","type":"text/link"},{"label":"Nome Attività","type":"richText"},{"label":"Assegnatario","type":"text"},{"label":"Owner","type":"text"},{"label":"Data Assegnazione","type":"date","format":"dd/MM/yyyy HH:mm:ss"},{"label":"Data Presa in carico","type":"date","format":"dd/MM/yyyy HH:mm:ss"},{"label":"Stato","type":"icon","note":"GREY=Assegnato, GREEN=In Lavorazione"}
                ],
                "rowAction": "multi-select → local!selectedRows"
              }
            ]}
          ]
        }
      ],
      "actions": [
        {"order":1,"label":"Riassegna","style":"PRIMARY","type":"submit","target":"BOA_ANC_ProcessoSupervisore_RiassegnazioneTask"}
      ]
    }
  ]
}
```


---

### Cross-layer Dependencies per Front-end Development

Il documento UX/UI (STEP 0-9) è **autosufficiente per struttura visuale, layout, navigazione e stati**.
Per una implementazione front-end completa, l'agente di re-platform deve integrare le seguenti dipendenze da altri layer:

| Dipendenza | Necessaria per | Layer sorgente |
|---|---|---|
| Valori dropdown `BOA_ANC_GetTipiDocumentoAttivi` | Opzioni reali (descrizione/codiceDocId) nel dropdown "Tipo Documento" di BOA_ANC_Task_TipizzazioneDocumenti | DBA (`04_dba.md`) |
| Schema tipo `BOA_ANC_Pratica` | Tipi campo precisi (string/integer/date) per binding e validazione in BOA_ANC_Sezione_DatiPratica | DBA (`04_dba.md`) |
| Schema tipo `BOA_ANC_Cliente` | Tipi campo per BOA_ANC_Sezione_DatiCliente (nome, cognome, CF, date, telefono, ecc.) | DBA (`04_dba.md`) |
| Schema tipo `BOA_ANC_DatiCarta` | Tipi campo per BOA_ANC_Sezione_DatiCarta (tipoCarta, numeroCarta, intestazione) | DBA (`04_dba.md`) |
| Schema tipo `BOA_ANC_Nota` | Tipi campo nota (testoNota, datacreazione, operatore) in BOA_ANC_Summary_Esito | DBA (`04_dba.md`) |
| Schema tipo `BOA_ANC_CheckList` (voce checklist) | Struttura interna checklist in BOA_ANC_Task_CheckList / BOA_ANC_CheckList_Section | DBA (`04_dba.md`) |
| Schema tipo `BOA_ANC_Contenuto` / `BOA_ANC_Documento` | Struttura allegati (nomeFile, idDocAppian, descrizione) in BOA_ANC_Sezione_Documenti | DBA (`04_dba.md`) |
| Lista stati pratica (`BOA_ANC_ESITI_SD`) | Valori possibili per esitoSD (costanti ESITI_SD[1]=positivo, [2]=negativo) usate in condizioni showWhen | Functional (`01_functional.md`) |
| Lista stati case (`BOA_ANC_StatiCase`) | Valori steps milestoneField in BOA_ANC_Header | Functional (`01_functional.md`) |
| Endpoint API submit `BOA_ANC_SalvataggioDati` | Cosa succede server-side al click di "chiudi pratica", "Salva e prosegui", "Modifica" | BPM (`02_bpm.md`) |
| Endpoint API `BOA_ANC_SalvaFiltriUtente` | Cosa succede al salvataggio filtri utente | BPM (`02_bpm.md`) |
| Endpoint API `BOA_ANC_Processo_TipizzaDoc` | Logica server-side al click "Conferma" in BOA_ANC_Task_TipizzazioneDocumenti | BPM (`02_bpm.md`) |
| Endpoint API `BOA_ANC_ScaricaSingoloDoc` | Modalità download documento (output file/stream) in BOA_ANC_Sezione_Documenti | BPM (`02_bpm.md`) |
| Endpoint API `BOA_ANC_ProcessoSupervisore_RiassegnazioneTask` | Logica riassegnazione task al click "Riassegna" | BPM (`02_bpm.md`) |
| Azioni disponibili per gruppo (dashboard) | Lista azioni dinamiche per gruppo Operatore/Supervisore (nomeAzione, processo, icona) | BPM (`02_bpm.md`) |
| Significato condizioni `showWhen` business | Interpretazione delle condizioni SAIL (es. `esitoSD != BOA_ANC_ESITI_SD[1]`) in termini di regole di visibilità | Functional (`01_functional.md`) |
| Struttura interna `BOA_ANC_CheckList_Section` (9120508) | Specifica completa voci checklist (labels, stati, dipendenze tra voci) | content/_9120508.xml |
| Struttura grafici dashboard Supervisore (9113035, 9113078, 9113178) | Tipo grafico (bar chart, pie, ecc.), metriche e assi | content/_9113035/9113078/9113178.xml |
| Struttura `BOA_ANC_Sezione_IndirizzoResidenza_DatiCliente` (9093562) | Campi indirizzo residenza (via, civico, CAP, comune, provincia) | content/_9093562.xml |
| Struttura `BOA_ANC_Contenuti_Section` (9168934) dettaglio | Colonne/layout del viewer contenuti denuncia | content/_9168934.xml |
| Struttura `BOA_ANC_Filtri_TaskReportSupervisoreGruppi` (9115247) | Campi filtro riassegnazione: data creazione, data scadenza, assegnatario, gruppo, pratica | content/_9115247.xml |

---

> **Garantito da questo documento (STEP 0-9)**:
> struttura visuale di tutte le schermate principali, ordine campi, layout contenitori (alberi SAIL),
> navigazione tra schermate, testi label/placeholder/tooltip/validazione verbatim,
> stati readonly/editable/conditional per ogni componente, opzioni dropdown inline,
> stili bottoni (PRIMARY/SECONDARY/DESTRUCTIVE), pattern UI classificati,
> screen specification fedele al layout originale Appian,
> navigation map e screen specifications in formato JSON machine-readable per re-platform.

---
*Documento generato da reverse engineering automatizzato — Export 20260506 — Analyzed 2026-05-20*
*Layer: UX/UI (FASE 1: STEP 0→3b + FASE 2: STEP 4→9)*
