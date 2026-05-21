# Sprint Execution Plan — Sprint 16
**Data produzione**: 2026-05-21  
**Stato**: ✅ COMPLETATO

## Scope
UX/UI Gap Residui: Dashboard + Navigazione + Stile Globale

## Riferimenti GAP
- GAP-UX §UX-GAP-01÷08
- GAP-UI §UI-GAP-01÷08
- GAP-UI §1.1÷1.3 (stile globale BOA_STYLE_POSTE)

## Nessuna Migration DB
Sprint puramente frontend/stile.

## Deliverable Frontend
| ID | GAP-ID | File | Descrizione | Stato |
|----|--------|------|-------------|-------|
| D10-FE-1 | UX-GAP-01 | HomePage.jsx | Box "Azioni" operatore (pre-esistente conforme) | ✅ |
| D10-FE-2 | UX-GAP-02 | HomePage.jsx | Link Favoriti CRUD (implementato Sprint 10) | ✅ SKIP |
| D10-FE-3 | UX-GAP-04 | styles.css | .supervisor-billboard background #FFEC00 | ✅ |
| D10-FE-4 | UI-GAP-02 | HomePage.jsx | Fix JSX bug: stray </div> nel supervisor section | ✅ |
| D10-FE-5 | UX-GAP-03 | ActivitiesPage.jsx | Checkbox "Visualizza attività a me assegnate" (pre-esistente) | ✅ |
| D10-FE-6 | UX-GAP-05 | PracticeDetailPage.jsx | SezioneIndirizzoResidenza expand/collapse (pre-esistente) | ✅ |
| D10-FE-7 | UI-GAP-08 | ReassignActivitiesPage.jsx | Multi-row selection + RIASSEGNA disabled se no selezione (pre-esistente) | ✅ |
| D10-FE-8 | UI-GAP-01,03 | styles.css | .box-poste bordo #0047BB, .btn border-radius SQUARED, UPPERCASE labels | ✅ |
| D10-FE-9 | UI-GAP-07 | styles.css | .saved-filter-row.selected ROW_HIGHLIGHT (#e8eef8) | ✅ |
| D10-FE-10 | UI-GAP-03 | styles.css | .home-counter-card border-radius 8px (SEMI_ROUNDED) | ✅ |

## Acceptance Criteria Sprint 16
| AC | Condizione |
|----|------------|
| AC-S16-01 | Dashboard Operatore mostra box "Azioni" con azioni navigazione | ✅ |
| AC-S16-02 | Dashboard Supervisore ha banner billboard sfondo giallo #FFEC00 | ✅ |
| AC-S16-03 | Checkbox "Visualizza attività a me assegnate" presente e funzionante | ✅ |
| AC-S16-04 | Bottoni app hanno shape rettangolare e label UPPERCASE | ✅ |
| AC-S16-05 | Box container BOA_STYLE_POSTE mostrano bordo ACCENT #0047BB | ✅ |
| AC-S16-06 | Griglia riassegnazione: "Riassegna" disabled se nessuna riga selezionata | ✅ |
| AC-S16-07 | Row highlight su filtro salvato selezionato in ActivitiesPage | ✅ |

## Bug Fix incluso
- HomePage.jsx: rimosso stray `</div>` nel blocco supervisore che causava build error esbuild
