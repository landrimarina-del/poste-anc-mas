param(
    [ValidateSet("happy", "ko4", "ko5", "list")]
    [string]$Scenario = "happy",
    [string]$BackendBaseUrl = "http://localhost:8080"
)

function Invoke-JsonPost {
    param(
        [string]$Uri,
        [string]$JsonBody
    )

    return Invoke-RestMethod -Method Post -Uri $Uri -ContentType "application/json" -Body $JsonBody
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

switch ($Scenario) {
    "happy" {
        $payload = Get-Content (Join-Path $root "bpm-open-happy.json") -Raw
        $response = Invoke-JsonPost -Uri "$BackendBaseUrl/api/v1/bpm/practices" -JsonBody $payload
        $response | ConvertTo-Json -Depth 10
    }

    "ko4" {
        $payload = Get-Content (Join-Path $root "bpm-open-ko4-invalid-doc-code.json") -Raw
        $response = Invoke-JsonPost -Uri "$BackendBaseUrl/api/v1/bpm/practices" -JsonBody $payload
        $response | ConvertTo-Json -Depth 10
    }

    "ko5" {
        $payloadObj = Get-Content (Join-Path $root "bpm-open-happy.json") -Raw | ConvertFrom-Json
        $payloadObj.ID_WORKITEM = "WI-S1-DUP-" + [guid]::NewGuid().ToString("N")
        $payloadObj.NUM_PRATICA = "APP-DUP-" + [guid]::NewGuid().ToString("N").Substring(0, 8).ToUpper()
        $payload = $payloadObj | ConvertTo-Json -Depth 10

        $first = Invoke-JsonPost -Uri "$BackendBaseUrl/api/v1/bpm/practices" -JsonBody $payload
        $second = Invoke-JsonPost -Uri "$BackendBaseUrl/api/v1/bpm/practices" -JsonBody $payload

        [pscustomobject]@{
            firstCall  = $first
            secondCall = $second
        } | ConvertTo-Json -Depth 10
    }

    "list" {
        $basicAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("op.rossi:Demo1234!"))
        $headers = @{ Authorization = "Basic $basicAuth" }
        $response = Invoke-RestMethod -Method Get -Uri "$BackendBaseUrl/api/v1/practices" -Headers $headers
        $response | ConvertTo-Json -Depth 10
    }
}
