$ErrorActionPreference = 'Stop'

$BaseUrl = if ($env:ANC_BASE_URL) { $env:ANC_BASE_URL } else { 'http://localhost:8080' }
$Username = if ($env:ANC_USERNAME) { $env:ANC_USERNAME } else { 'operatore' }
$Password = if ($env:ANC_PASSWORD) { $env:ANC_PASSWORD } else { 'operatore' }
$Token = $env:ANC_TOKEN
$BpmStubBaseUrl = $env:ANC_BPM_STUB_BASE_URL
$BpmStubOutcomeLogPath = if ($env:ANC_BPM_STUB_OUTCOME_LOG_PATH) { $env:ANC_BPM_STUB_OUTCOME_LOG_PATH } else { '/receive-outcome/logs' }
$OutDir = 'tools/qa/sprint6/out'
$OutFile = Join-Path $OutDir 'QA_Result_Sprint_6.md'

New-Item -ItemType Directory -Path $OutDir -Force | Out-Null

function Add-Result {
    param(
        [string]$Ac,
        [string]$Status,
        [string]$Note
    )

    $script:Results += [pscustomobject]@{
        AC = $Ac
        Esito = $Status
        Note = $Note
    }
}

function Get-TokenFromLogin {
    param(
        [string]$LoginBaseUrl,
        [string]$LoginUsername,
        $LoginPassword
    )

    try {
        $body = @{ username = $LoginUsername; password = $LoginPassword } | ConvertTo-Json
        $resp = Invoke-RestMethod -Method Post -Uri "$LoginBaseUrl/api/v1/auth/login" -ContentType 'application/json' -Body $body
        if ($resp.accessToken) { return [string]$resp.accessToken }
        if ($resp.token) { return [string]$resp.token }
        return $null
    }
    catch {
        return $null
    }
}

function Invoke-AncApi {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Payload
    )

    $headers = @{ Authorization = "Bearer $Token" }
    $uri = "$BaseUrl$Path"

    try {
        if ($null -ne $Payload) {
            $json = $Payload | ConvertTo-Json -Depth 10
            $resp = Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers -ContentType 'application/json' -Body $json
        }
        else {
            $resp = Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers
        }

        return [pscustomobject]@{
            HttpOk = $true
            StatusCode = 200
            Body = $resp
            Raw = ($resp | ConvertTo-Json -Depth 12 -Compress)
        }
    }
    catch {
        $statusCode = 0
        $raw = ''
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int]$_.Exception.Response.StatusCode
            try {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $raw = $reader.ReadToEnd()
            }
            catch {
                $raw = $_.Exception.Message
            }
        }
        else {
            $raw = $_.Exception.Message
        }

        return [pscustomobject]@{
            HttpOk = $false
            StatusCode = $statusCode
            Body = $null
            Raw = $raw
        }
    }
}

function Invoke-AnonymousGet {
    param([string]$AbsoluteUrl)

    try {
        $resp = Invoke-RestMethod -Method Get -Uri $AbsoluteUrl
        return [pscustomobject]@{
            HttpOk = $true
            StatusCode = 200
            Raw = ($resp | ConvertTo-Json -Depth 12 -Compress)
        }
    }
    catch {
        $statusCode = 0
        $raw = $_.Exception.Message
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }
        return [pscustomobject]@{
            HttpOk = $false
            StatusCode = $statusCode
            Raw = $raw
        }
    }
}

function Invoke-ChecklistCase {
    param(
        [string]$CaseId,
        [string]$PracticeId,
        [hashtable]$Payload,
        [int]$ExpectedStatusFamily,
        [string]$ExpectedToken
    )

    if ([string]::IsNullOrWhiteSpace($PracticeId)) {
        Add-Result -Ac $CaseId -Status 'BLOCKED-EXEC' -Note 'Practice ID non impostato in variabili ambiente.'
        return
    }

    $res = Invoke-AncApi -Method 'PUT' -Path "/api/v1/practices/$PracticeId/intake/checklist" -Payload $Payload
    $statusFamily = [math]::Floor($res.StatusCode / 100)

    if ($statusFamily -eq $ExpectedStatusFamily -and $res.Raw -like "*$ExpectedToken*") {
        Add-Result -Ac $CaseId -Status 'PASS' -Note "HTTP $($res.StatusCode), token atteso trovato: $ExpectedToken"
    }
    else {
        Add-Result -Ac $CaseId -Status 'FAIL' -Note "HTTP $($res.StatusCode), atteso ${ExpectedStatusFamily}xx e token $ExpectedToken"
    }
}

function Invoke-ClosePracticeCase {
    param(
        [string]$CaseId,
        [string]$PracticeId
    )

    if ([string]::IsNullOrWhiteSpace($PracticeId)) {
        Add-Result -Ac $CaseId -Status 'BLOCKED-EXEC' -Note 'Practice ID non impostato in variabili ambiente.'
        return
    }

    $closeRes = Invoke-AncApi -Method 'POST' -Path "/api/v1/practices/$PracticeId/intake/close" -Payload @{}
    if ([math]::Floor($closeRes.StatusCode / 100) -ne 2) {
        Add-Result -Ac $CaseId -Status 'FAIL' -Note "CHIUDI PRATICA non riuscita (HTTP $($closeRes.StatusCode))."
        return
    }

    $detailRes = Invoke-AncApi -Method 'GET' -Path "/api/v1/practices/$PracticeId" -Payload $null
    if ([math]::Floor($detailRes.StatusCode / 100) -eq 2 -and $detailRes.Raw -like '*IN_ATTESA_CONFERMA_BPM*') {
        Add-Result -Ac $CaseId -Status 'PASS' -Note 'Task chiuso e pratica in IN_ATTESA_CONFERMA_BPM.'
    }
    else {
        Add-Result -Ac $CaseId -Status 'FAIL' -Note "Stato post close non coerente (HTTP dettaglio $($detailRes.StatusCode))."
    }
}

function Invoke-AckFinalizeCase {
    param(
        [string]$CaseId,
        [string]$PracticeId,
        [string]$AckOutcome,
        [string]$ExpectedFinalState,
        [string]$WorkitemId
    )

    if ([string]::IsNullOrWhiteSpace($PracticeId)) {
        Add-Result -Ac $CaseId -Status 'BLOCKED-EXEC' -Note 'Practice ID non impostato in variabili ambiente.'
        return
    }

    $payload = @{
        practiceId = $PracticeId
        outcome = $AckOutcome
    }

    if (-not [string]::IsNullOrWhiteSpace($WorkitemId)) {
        $payload.idWorkitem = $WorkitemId
    }

    $ackRes = Invoke-AncApi -Method 'POST' -Path '/api/v1/bpm/outcome-ack' -Payload $payload
    if ([math]::Floor($ackRes.StatusCode / 100) -ne 2) {
        Add-Result -Ac $CaseId -Status 'FAIL' -Note "ACK BPM non accettata (HTTP $($ackRes.StatusCode))."
        return
    }

    $detailRes = Invoke-AncApi -Method 'GET' -Path "/api/v1/practices/$PracticeId" -Payload $null
    if ([math]::Floor($detailRes.StatusCode / 100) -eq 2 -and $detailRes.Raw -like "*$ExpectedFinalState*" -and $detailRes.Raw -match 'dataChiusura|closureDate|closedAt') {
        Add-Result -Ac $CaseId -Status 'PASS' -Note "$ExpectedFinalState valorizzato con data chiusura."
    }
    else {
        Add-Result -Ac $CaseId -Status 'FAIL' -Note "Finalizzazione non coerente: atteso $ExpectedFinalState con data chiusura."
    }
}

function Invoke-AckIdempotenceCase {
    param(
        [string]$CaseId,
        [string]$PracticeId,
        [string]$AckOutcome,
        [string]$ExpectedFinalState,
        [string]$WorkitemId
    )

    if ([string]::IsNullOrWhiteSpace($PracticeId)) {
        Add-Result -Ac $CaseId -Status 'BLOCKED-EXEC' -Note 'Practice ID non impostato in variabili ambiente.'
        return
    }

    $payload = @{
        practiceId = $PracticeId
        outcome = $AckOutcome
    }

    if (-not [string]::IsNullOrWhiteSpace($WorkitemId)) {
        $payload.idWorkitem = $WorkitemId
    }

    $before = Invoke-AncApi -Method 'GET' -Path "/api/v1/practices/$PracticeId" -Payload $null
    $firstAck = Invoke-AncApi -Method 'POST' -Path '/api/v1/bpm/outcome-ack' -Payload $payload
    $secondAck = Invoke-AncApi -Method 'POST' -Path '/api/v1/bpm/outcome-ack' -Payload $payload
    $after = Invoke-AncApi -Method 'GET' -Path "/api/v1/practices/$PracticeId" -Payload $null

    if ([math]::Floor($firstAck.StatusCode / 100) -eq 2 -and [math]::Floor($secondAck.StatusCode / 100) -eq 2 -and $after.Raw -like "*$ExpectedFinalState*" -and $before.Raw -eq $after.Raw) {
        Add-Result -Ac $CaseId -Status 'PASS' -Note 'Replay ACK idempotente: stato finale invariato.'
    }
    else {
        Add-Result -Ac $CaseId -Status 'FAIL' -Note 'Replay ACK ha alterato risposta/stato oppure HTTP non 2xx.'
    }
}

function Invoke-AuditHistoryCase {
    param(
        [string]$CaseId,
        [string]$PracticeId
    )

    if ([string]::IsNullOrWhiteSpace($PracticeId)) {
        Add-Result -Ac $CaseId -Status 'BLOCKED-EXEC' -Note 'Practice ID non impostato in variabili ambiente.'
        return
    }

    $states = Invoke-AncApi -Method 'GET' -Path "/api/v1/practices/$PracticeId/states" -Payload $null
    $history = Invoke-AncApi -Method 'GET' -Path "/api/v1/practices/$PracticeId/history" -Payload $null

    if ([math]::Floor($states.StatusCode / 100) -eq 2 -and [math]::Floor($history.StatusCode / 100) -eq 2 -and $states.Raw -like '*IN_ATTESA_CONFERMA_BPM*' -and ($states.Raw -like '*CHIUSA_OK*' -or $states.Raw -like '*CHIUSA_KO*') -and ($history.Raw -match 'close|chiudi|finalize|ack')) {
        Add-Result -Ac $CaseId -Status 'PASS' -Note 'Storico stati e audit coerenti su close/finalize.'
    }
    else {
        Add-Result -Ac $CaseId -Status 'FAIL' -Note 'Storico stati/audit non coerenti o non reperibili.'
    }
}

$Results = @()

if ([string]::IsNullOrWhiteSpace($Token)) {
    $Token = Get-TokenFromLogin -LoginBaseUrl $BaseUrl -LoginUsername $Username -LoginPassword $Password
}

if ([string]::IsNullOrWhiteSpace($Token)) {
    Add-Result -Ac 'AC-S6-01..AC-S6-09' -Status 'BLOCKED-EXEC' -Note 'Login/token non disponibile. Usare ANC_TOKEN o credenziali valide.'
}
else {
    Invoke-ChecklistCase -CaseId 'AC-S6-01' -PracticeId $env:ANC_PRACTICE_ID_S6_01 -Payload @{ cardPresent = $true; cardConformita = $true } -ExpectedStatusFamily 2 -ExpectedToken 'card'
    Invoke-ChecklistCase -CaseId 'AC-S6-02' -PracticeId $env:ANC_PRACTICE_ID_S6_02 -Payload @{ cardPresent = $false; cardConformita = $false } -ExpectedStatusFamily 2 -ExpectedToken 'RESPINTA'
    Invoke-ChecklistCase -CaseId 'AC-S6-03' -PracticeId $env:ANC_PRACTICE_ID_S6_03 -Payload @{ cardPresent = $true; cardConformita = $true } -ExpectedStatusFamily 2 -ExpectedToken 'APPROVATA'

    Invoke-ClosePracticeCase -CaseId 'AC-S6-04' -PracticeId $env:ANC_PRACTICE_ID_S6_04

    if ([string]::IsNullOrWhiteSpace($BpmStubBaseUrl)) {
        Add-Result -Ac 'AC-S6-05' -Status 'BLOCKED-EXEC' -Note 'ANC_BPM_STUB_BASE_URL non impostato. Impossibile verificare outbound verso bpm-stub.'
    }
    else {
        $logUrl = "$BpmStubBaseUrl$BpmStubOutcomeLogPath"
        $stubRes = Invoke-AnonymousGet -AbsoluteUrl $logUrl
        if ([math]::Floor($stubRes.StatusCode / 100) -eq 2 -and ($stubRes.Raw -like '*OK*' -or $stubRes.Raw -like '*KO*')) {
            Add-Result -Ac 'AC-S6-05' -Status 'PASS' -Note 'Outbound verso bpm-stub rilevato con payload esito.'
        }
        else {
            Add-Result -Ac 'AC-S6-05' -Status 'FAIL' -Note "Nessun outbound coerente rilevato su $logUrl."
        }
    }

    Invoke-AckFinalizeCase -CaseId 'AC-S6-06' -PracticeId $env:ANC_PRACTICE_ID_S6_06 -AckOutcome 'OK' -ExpectedFinalState 'CHIUSA_OK' -WorkitemId $env:ANC_WORKITEM_ID_S6_06
    Invoke-AckFinalizeCase -CaseId 'AC-S6-07' -PracticeId $env:ANC_PRACTICE_ID_S6_07 -AckOutcome 'KO' -ExpectedFinalState 'CHIUSA_KO' -WorkitemId $env:ANC_WORKITEM_ID_S6_07

    Invoke-AckIdempotenceCase -CaseId 'AC-S6-08' -PracticeId $env:ANC_PRACTICE_ID_S6_08 -AckOutcome 'OK' -ExpectedFinalState 'CHIUSA_OK' -WorkitemId $env:ANC_WORKITEM_ID_S6_08

    Invoke-AuditHistoryCase -CaseId 'AC-S6-09' -PracticeId $env:ANC_PRACTICE_ID_S6_09
}

$lines = @()
$lines += '# QA Result Sprint 6'
$lines += ''
$lines += '| AC | Esito | Note |'
$lines += '|---|---|---|'
foreach ($r in $Results) {
    $lines += "| $($r.AC) | $($r.Esito) | $($r.Note) |"
}

Set-Content -Path $OutFile -Value $lines -Encoding UTF8
Write-Output "Generato $OutFile"