$ErrorActionPreference = 'Stop'

$BaseUrl = if ($env:ANC_BASE_URL) { $env:ANC_BASE_URL } else { 'http://localhost:8080' }
$Username = if ($env:ANC_USERNAME) { $env:ANC_USERNAME } else { 'operatore' }
$Password = if ($env:ANC_PASSWORD) { $env:ANC_PASSWORD } else { 'operatore' }
$Token = $env:ANC_TOKEN
$OutDir = 'tools/qa/sprint5/out'
$OutFile = Join-Path $OutDir 'QA_Result_Sprint_5.md'

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
        [string]$LoginPassword
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
            Raw = ($resp | ConvertTo-Json -Depth 10 -Compress)
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

function Run-ChecklistCase {
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

$Results = @()

if ([string]::IsNullOrWhiteSpace($Token)) {
    $Token = Get-TokenFromLogin -LoginBaseUrl $BaseUrl -LoginUsername $Username -LoginPassword $Password
}

if ([string]::IsNullOrWhiteSpace($Token)) {
    Add-Result -Ac 'AC-S5-01..07' -Status 'BLOCKED-EXEC' -Note 'Login/token non disponibile. Usare ANC_TOKEN o credenziali valide.'
}
else {
    Run-ChecklistCase -CaseId 'AC-S5-01' -PracticeId $env:ANC_PRACTICE_ID_S5_01 -Payload @{ documentoPresente = $false; formalOk = $false; leggibile = $false; datiClienteOk = $false } -ExpectedStatusFamily 2 -ExpectedToken 'RESPINTA'
    Run-ChecklistCase -CaseId 'AC-S5-02' -PracticeId $env:ANC_PRACTICE_ID_S5_02 -Payload @{ documentoPresente = $true; formalOk = $true; leggibile = $true; datiClienteOk = $true } -ExpectedStatusFamily 2 -ExpectedToken 'APPROVATA'
    Run-ChecklistCase -CaseId 'AC-S5-03' -PracticeId $env:ANC_PRACTICE_ID_S5_03 -Payload @{ documentoPresente = $true; formalOk = $false; causaliKo = @() } -ExpectedStatusFamily 4 -ExpectedToken 'causale'
    Run-ChecklistCase -CaseId 'AC-S5-04' -PracticeId $env:ANC_PRACTICE_ID_S5_04 -Payload @{ documentoPresente = $true; formalOk = $false; causaliKo = @('FIRME') } -ExpectedStatusFamily 2 -ExpectedToken 'RESPINTA'
    Run-ChecklistCase -CaseId 'AC-S5-05' -PracticeId $env:ANC_PRACTICE_ID_S5_05 -Payload @{ documentoPresente = $true; formalOk = $true; leggibile = $true; datiClienteOk = $true } -ExpectedStatusFamily 2 -ExpectedToken 'BOZZA'

    if ([string]::IsNullOrWhiteSpace($env:ANC_PRACTICE_ID_S5_06)) {
        Add-Result -Ac 'AC-S5-06' -Status 'BLOCKED-EXEC' -Note 'Practice ID non impostato (ANC_PRACTICE_ID_S5_06).'
    }
    else {
        $editRes = Invoke-AncApi -Method 'POST' -Path "/api/v1/practices/$($env:ANC_PRACTICE_ID_S5_06)/intake/checklist/edit" -Payload @{}
        if ([math]::Floor($editRes.StatusCode / 100) -eq 2 -and $editRes.Raw -like '*RIAPERTA*') {
            Add-Result -Ac 'AC-S5-06' -Status 'PASS' -Note 'MODIFICA ha riaperto la checklist in RIAPERTA.'
        }
        else {
            Add-Result -Ac 'AC-S5-06' -Status 'FAIL' -Note "MODIFICA non ha restituito RIAPERTA (HTTP $($editRes.StatusCode))."
        }
    }

    Add-Result -Ac 'AC-S5-07' -Status 'BLOCKED-EXEC' -Note 'Verifica UI manuale: note interne visibili solo per RESPINTA.'
}

$lines = @()
$lines += '# QA Result Sprint 5'
$lines += ''
$lines += '| AC | Esito | Note |'
$lines += '|---|---|---|'
foreach ($r in $Results) {
    $lines += "| $($r.AC) | $($r.Esito) | $($r.Note) |"
}

Set-Content -Path $OutFile -Value $lines -Encoding UTF8
Write-Output "Generato $OutFile"
