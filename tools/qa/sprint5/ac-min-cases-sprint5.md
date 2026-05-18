# Casi AC minimi - Sprint 5 (US-E6.01..E6.09)

## Matrice casi

| ID | User Story | Scenario minimo | Atteso |
|---|---|---|---|
| AC-S5-01 | US-E6.01, US-E6.02, US-E6.08 | `documento_presente=NO` | outcome `RESPINTA`, controlli successivi auto `KO`, conformita disabilitata |
| AC-S5-02 | US-E6.03, US-E6.08 | tutti i controlli obbligatori = `SI` | outcome `APPROVATA` |
| AC-S5-03 | US-E6.04 | `formal_ok=NO` senza causali | errore validazione, salvataggio bloccato |
| AC-S5-04 | US-E6.04, US-E6.08 | `formal_ok=NO` con >=1 causale | salvataggio OK, outcome `RESPINTA` |
| AC-S5-05 | US-E6.06 | click `SALVA E PROSEGUI` | checklist persistita in stato `BOZZA` |
| AC-S5-06 | US-E6.07 | click `MODIFICA` su bozza salvata | checklist riaperta in stato `RIAPERTA` |
| AC-S5-07 | US-E6.09 | esito `RESPINTA` vs `APPROVATA` | note interne visibili solo per `RESPINTA` |

## Dati di test consigliati

- Una pratica per ciascun caso.
- Tipizzazione gia confermata su `Verbale di denuncia`.
- Utente con ruolo `OPERATORE` proprietario del task.

## Criterio di uscita Sprint 5

- AC-S5-01..07 tutti `PASS`, oppure difetti tracciati in `Defect_List_Sprint_5.md` con severita e impatto.
