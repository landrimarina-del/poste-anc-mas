# QA Sprint 5 - US-E6.01..E6.09

Questa cartella contiene runbook e script ripetibili per validare la vertical slice Sprint 5:
- checklist verbale
- outcome automatico Approvata/Respinta
- gestione bozza/modifica
- vincolo note interne

## Prerequisiti

- Stack ANC locale avviato via docker compose
- Backend raggiungibile (default: http://localhost:8080)
- Utente OPERATORE valido
- Pratiche di test tipizzate come Verbale e in stato IN_LAVORAZIONE
- PowerShell 7+ oppure Bash 4+

## Variabili ambiente supportate

- `ANC_BASE_URL` (default `http://localhost:8080`)
- `ANC_USERNAME` (default `operatore`)
- `ANC_PASSWORD` (default `operatore`)
- `ANC_TOKEN` (opzionale, se gia disponibile)
- `ANC_PRACTICE_ID_S5_01`
- `ANC_PRACTICE_ID_S5_02`
- `ANC_PRACTICE_ID_S5_03`
- `ANC_PRACTICE_ID_S5_04`
- `ANC_PRACTICE_ID_S5_05`
- `ANC_PRACTICE_ID_S5_06`
- `ANC_PRACTICE_ID_S5_07`

Nota: usare practice id distinti evita effetti collaterali tra test.

## Esecuzione

PowerShell:

```powershell
pwsh ./tools/qa/sprint5/run-sprint5-qa.ps1
```

Bash:

```bash
bash ./tools/qa/sprint5/run-sprint5-qa.sh
```

Entrambi gli script generano un file markdown di esito in:

- `tools/qa/sprint5/out/QA_Result_Sprint_5.md`

## Runbook manuale per UI

Gli AC con visibilita UI devono essere confermati manualmente:

1. Aprire pratica in milestone Verifica Documento.
2. Compilare checklist secondo caso AC.
3. Verificare card esito in Riepilogo.
4. Verificare visibilita campo Note interne:
   - presente solo con esito Respinta
   - assente con esito Approvata
5. Annotare evidenze (screenshot o log FE).

## Limitazione nota

Se la sessione non consente esecuzione comandi terminale, marcare i casi come `BLOCKED-EXEC` e usare le istruzioni dei due script come runbook operativo.
