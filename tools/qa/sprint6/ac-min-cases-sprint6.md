# Casi AC minimi - Sprint 6 (US-E7.01..US-E7.05)

## Matrice casi

| ID | User Story | Scenario minimo | Atteso |
|---|---|---|---|
| AC-S6-01 | US-E7.01 | tipizzazione Carta | checklist Carta abilitata con controlli presenza/conformita |
| AC-S6-02 | US-E7.01 | card_present=NO | outcome RESPINTA |
| AC-S6-03 | US-E7.01 | card_present=SI e conformita=SI | outcome APPROVATA |
| AC-S6-04 | US-E7.02 | click CHIUDI PRATICA | stato IN_ATTESA_CONFERMA_BPM e task rimosso |
| AC-S6-05 | US-E7.03 | outbound esito verso bpm-stub | payload coerente con outcome OK/KO |
| AC-S6-06 | US-E7.04 | ACK BPM outcome=OK | stato CHIUSA_OK e data chiusura valorizzata |
| AC-S6-07 | US-E7.04 | ACK BPM outcome=KO | stato CHIUSA_KO e data chiusura valorizzata |
| AC-S6-08 | US-E7.04 | replay ACK sulla stessa pratica | stato finale invariato (idempotenza) |
| AC-S6-09 | US-E7.05 | verifica tab Stati/Cronologia | storico stati + audit presenti su close/finalize |

## Dati di test consigliati

- una pratica dedicata per ogni AC
- ruolo OPERATORE per checklist e close
- stub BPM raggiungibile per ack e verifica outbound

## Criterio di uscita Sprint 6

- AC-S6-01..AC-S6-09 tutti PASS, oppure difetti tracciati con severita e impatto in Defect List.