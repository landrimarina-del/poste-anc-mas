# =====================================================================
# QA Sprint 4 - GAP-BLOCKER-001 Document Transport
# Esegue tutti gli AC e stampa esito (PASS/FAIL/SKIP).
# NON modifica codice di produzione. Genera file in tools/qa/sprint4/out/.
# =====================================================================
[CmdletBinding()]
param(
    [switch]$SkipBringup,
    [string]$ApiKey = 'anc-poc-bpm-inbound-key',
    [string]$BackendUrl = 'http://localhost:8080'
)

$ErrorActionPreference = 'Stop'
$OutDir = Join-Path $PSScriptRoot 'out'
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$Results = [System.Collections.Generic.List[object]]::new()

function Add-Result($Id, $Status, $Notes) {
    $Results.Add([pscustomobject]@{ AC = $Id; Status = $Status; Notes = $Notes })
    Write-Host ("[{0}] {1} - {2}" -f $Status, $Id, $Notes)
}

function Invoke-OpenPractice {
    param([string]$IdWorkitem, [string]$NumPratica, [hashtable]$Contenuto, [int]$CodiceDocId = 3)
    $body = @{
        CANALE = 'QA'
        ID_WORKITEM = $IdWorkitem
        NUM_PRATICA = $NumPratica
        CF_CLIENTE = 'RSSMRA80A01H501U'
        DATA_INSERIMENTO_RICHIESTA = (Get-Date -Format 'dd/MM/yyyy HH:mm:ss')
        CLIENTE = @{ NOME='Mario'; COGNOME='Rossi'; DATANASCITA='01/01/1980'; COMUNENASCITA='Roma'; PROVINCIANASCITA='RM'; NAZIONENASCITA='Italia'; CITTADINANZA='Italiana' }
        DATI_CARTA_BLOCCATA = @{ I_NUMERO_CARTA='4000000000000000'; I_TIPO_CARTA='POSTEPAY' }
        DOCUMENTI = @(@{ CODICE_DOC_ID = $CodiceDocId; CONTENUTI = @($Contenuto) })
    } | ConvertTo-Json -Depth 10
    try {
        $resp = Invoke-WebRequest -Uri "$BackendUrl/api/v1/bpm/practices" -Method Post `
            -Headers @{ 'X-SD-API-Key' = $ApiKey; 'Content-Type' = 'application/json' } `
            -Body $body -UseBasicParsing -ErrorAction Stop
        return @{ Status = $resp.StatusCode; Body = ($resp.Content | ConvertFrom-Json) }
    } catch {
        $r = $_.Exception.Response
        $reader = New-Object System.IO.StreamReader($r.GetResponseStream())
        $payload = $reader.ReadToEnd()
        return @{ Status = [int]$r.StatusCode; Body = ($payload | ConvertFrom-Json -ErrorAction SilentlyContinue); Raw = $payload }
    }
}

function Exec-DbQuery([string]$Sql) {
    docker exec anc-db mariadb -uanc -panc anc -N -B -e $Sql 2>&1
}

# ------------------------------------------------------------------ Bring-up
if (-not $SkipBringup) {
    Write-Host '=== Bring-up stack ===' -ForegroundColor Cyan
    docker compose down -v | Out-Host
    docker compose up -d --build | Out-Host
    Write-Host 'Attendo readiness backend...'
    $deadline = (Get-Date).AddMinutes(3)
    while ((Get-Date) -lt $deadline) {
        try {
            $h = Invoke-RestMethod -Uri "$BackendUrl/actuator/health/readiness" -TimeoutSec 3
            if ($h.status -eq 'UP') { break }
        } catch {}
        Start-Sleep 3
    }
    docker compose ps | Out-File (Join-Path $OutDir 'compose-ps.txt')
}

# ------------------------------------------------------------------ AC-DB-1 (idempotenza V9)
Write-Host '=== AC-DB-1 Re-run V9 ===' -ForegroundColor Cyan
docker compose restart backend | Out-Null
Start-Sleep 10
$logs = docker logs anc-backend --since 60s 2>&1 | Select-String -Pattern 'flyway|migrat' -SimpleMatch
$logs | Out-File (Join-Path $OutDir 'ac-db-1.log')
if ($logs -match 'ERROR|FAILED') { Add-Result 'AC-DB-1' 'FAIL' 'Errori Flyway nei log' }
else { Add-Result 'AC-DB-1' 'PASS' 'Re-run senza errori (vedi ac-db-1.log)' }

# ------------------------------------------------------------------ AC-DB-2 (CHECK)
Write-Host '=== AC-DB-2 CHECK ingestion_status BOGUS ===' -ForegroundColor Cyan
$out = Exec-DbQuery "INSERT INTO attachment(id,practice_id,id_doc,nome_file,estensione,checksum_sha256,ingestion_status,created_at) VALUES (UUID(),(SELECT id FROM practice LIMIT 1),'qa-bogus','x','pdf','x','BOGUS',NOW(3));"
$out | Out-File (Join-Path $OutDir 'ac-db-2.log')
if ($out -match 'CONSTRAINT|CHECK|chk_att_ingestion_status') { Add-Result 'AC-DB-2' 'PASS' 'CHECK ha respinto BOGUS' }
else { Add-Result 'AC-DB-2' 'FAIL' "Output: $out" }

# ------------------------------------------------------------------ AC-DB-3 (UNIQUE)
Write-Host '=== AC-DB-3 UNIQUE (practice_id,id_doc) ===' -ForegroundColor Cyan
# Esegue due insert con stessa coppia
$existingPractice = Exec-DbQuery "SELECT id FROM practice LIMIT 1;"
if (-not $existingPractice) { Add-Result 'AC-DB-3' 'SKIP' 'Nessuna pratica esistente'; }
else {
    $pid = ($existingPractice | Select-Object -First 1).Trim()
    $dup = "qa-dup-$(Get-Random)"
    Exec-DbQuery "INSERT INTO attachment(id,practice_id,id_doc,nome_file,estensione,checksum_sha256,ingestion_status,created_at) VALUES (UUID(),'$pid','$dup','x','pdf','x','PENDING',NOW(3));" | Out-Null
    $out = Exec-DbQuery "INSERT INTO attachment(id,practice_id,id_doc,nome_file,estensione,checksum_sha256,ingestion_status,created_at) VALUES (UUID(),'$pid','$dup','x','pdf','x','PENDING',NOW(3));"
    $out | Out-File (Join-Path $OutDir 'ac-db-3.log')
    if ($out -match 'Duplicate|uk_att_practice_id_doc') { Add-Result 'AC-DB-3' 'PASS' 'UNIQUE ha respinto duplicato' }
    else { Add-Result 'AC-DB-3' 'FAIL' "Output: $out" }
}

# ------------------------------------------------------------------ AC-INGEST-4 (happy path)
Write-Host '=== AC-INGEST-4 Pratica con sample.pdf ===' -ForegroundColor Cyan
$wi4 = "QA-S4-WI-$(Get-Random)"
$np4 = "QA-S4-NP-$(Get-Random)"
$idDoc4 = "QA-DOC-$(Get-Random)"
$cont = @{ NOME_FILE='sample'; ESTENSIONE='pdf'; ID_DOC=$idDoc4; LINKDOWNLOAD='http://bpm-stub/files/sample.pdf' }
$r = Invoke-OpenPractice -IdWorkitem $wi4 -NumPratica $np4 -Contenuto $cont
$r | ConvertTo-Json -Depth 8 | Out-File (Join-Path $OutDir 'ac-ingest-4.json')
$attRow = Exec-DbQuery "SELECT id,ingestion_status,storage_uri,size_bytes,mime_type FROM attachment WHERE id_doc='$idDoc4';"
$attRow | Out-File (Join-Path $OutDir 'ac-ingest-4-db.log')
if ($r.Body.resultCode -eq 0 -and $attRow -match 'AVAILABLE' -and $attRow -match 's3://anc-attachments/') {
    Add-Result 'AC-INGEST-4' 'PASS' "practiceId=$($r.Body.details.practiceId), storage_uri OK"
    $script:HappyAttId = ($attRow -split "`t")[0].Trim()
    $script:HappyPracticeId = $r.Body.details.practiceId
    $script:HappyNomeFile = 'sample'
} else { Add-Result 'AC-INGEST-4' 'FAIL' "resultCode=$($r.Body.resultCode); db=$attRow" }

# ------------------------------------------------------------------ AC-INGEST-1 (host fuori allow-list)
Write-Host '=== AC-INGEST-1 Host fuori allow-list ===' -ForegroundColor Cyan
$wi1 = "QA-S4-WI-$(Get-Random)"
$cont1 = @{ NOME_FILE='foo'; ESTENSIONE='pdf'; ID_DOC="QA-DOC-$(Get-Random)"; LINKDOWNLOAD='http://example.com/foo.pdf' }
$r1 = Invoke-OpenPractice -IdWorkitem $wi1 -NumPratica "QA-NP-$(Get-Random)" -Contenuto $cont1
$r1 | ConvertTo-Json -Depth 8 | Out-File (Join-Path $OutDir 'ac-ingest-1.json')
$exists = Exec-DbQuery "SELECT COUNT(*) FROM practice WHERE id_workitem='$wi1';"
if ($r1.Body.resultCode -eq -4 -and $exists.Trim() -eq '0') { Add-Result 'AC-INGEST-1' 'PASS' 'resultCode=-4 e nessuna pratica' }
else { Add-Result 'AC-INGEST-1' 'FAIL' "resultCode=$($r1.Body.resultCode); practices=$exists" }

# ------------------------------------------------------------------ AC-INGEST-2 (mismatch estensione)
Write-Host '=== AC-INGEST-2 mismatch estensione ===' -ForegroundColor Cyan
$wi2 = "QA-S4-WI-$(Get-Random)"
$cont2 = @{ NOME_FILE='sample'; ESTENSIONE='png'; ID_DOC="QA-DOC-$(Get-Random)"; LINKDOWNLOAD='http://bpm-stub/files/sample.pdf' }
$r2 = Invoke-OpenPractice -IdWorkitem $wi2 -NumPratica "QA-NP-$(Get-Random)" -Contenuto $cont2
$r2 | ConvertTo-Json -Depth 8 | Out-File (Join-Path $OutDir 'ac-ingest-2.json')
if ($r2.Body.resultCode -eq -4) { Add-Result 'AC-INGEST-2' 'PASS' 'resultCode=-4 (mismatch)' }
else { Add-Result 'AC-INGEST-2' 'FAIL' "resultCode=$($r2.Body.resultCode)" }

# ------------------------------------------------------------------ AC-INGEST-3 (size cap)
Write-Host '=== AC-INGEST-3 size cap ===' -ForegroundColor Cyan
Add-Result 'AC-INGEST-3' 'SKIP' 'File >25MB non disponibile nel bpm-stub; documentato'

# ------------------------------------------------------------------ AC-VIEWER-1 (preview)
Write-Host '=== AC-VIEWER-1 preview ===' -ForegroundColor Cyan
if (-not $script:HappyAttId) { Add-Result 'AC-VIEWER-1' 'SKIP' 'AC-INGEST-4 fallito' }
else {
    try {
        $resp = Invoke-WebRequest -Uri "$BackendUrl/api/v1/attachments/$($script:HappyAttId)/preview" -UseBasicParsing
        $ct = $resp.Headers['Content-Type']
        if ($resp.StatusCode -eq 200 -and $ct -match 'application/pdf') {
            Add-Result 'AC-VIEWER-1' 'PASS' "200 / $ct"
        } else { Add-Result 'AC-VIEWER-1' 'FAIL' "Status=$($resp.StatusCode) CT=$ct" }
    } catch { Add-Result 'AC-VIEWER-1' 'FAIL' $_.Exception.Message }
}

# ------------------------------------------------------------------ AC-VIEWER-2 (bpm-stub down)
Write-Host '=== AC-VIEWER-2 preview con bpm-stub stoppato ===' -ForegroundColor Cyan
docker compose stop bpm-stub | Out-Null
Start-Sleep 3
if (-not $script:HappyAttId) { Add-Result 'AC-VIEWER-2' 'SKIP' 'No attachment id' }
else {
    try {
        $resp = Invoke-WebRequest -Uri "$BackendUrl/api/v1/attachments/$($script:HappyAttId)/preview" -UseBasicParsing
        if ($resp.StatusCode -eq 200) { Add-Result 'AC-VIEWER-2' 'PASS' 'Preview servito da MinIO con bpm-stub down' }
        else { Add-Result 'AC-VIEWER-2' 'FAIL' "Status=$($resp.StatusCode)" }
    } catch { Add-Result 'AC-VIEWER-2' 'FAIL' $_.Exception.Message }
}
docker compose start bpm-stub | Out-Null

# ------------------------------------------------------------------ AC-DOWNLOAD-1
Write-Host '=== AC-DOWNLOAD-1 download ===' -ForegroundColor Cyan
if (-not $script:HappyAttId) { Add-Result 'AC-DOWNLOAD-1' 'SKIP' 'No attachment id' }
else {
    try {
        $resp = Invoke-WebRequest -Uri "$BackendUrl/api/v1/attachments/$($script:HappyAttId)/download" -UseBasicParsing
        $cd = $resp.Headers['Content-Disposition']
        if ($cd -match 'attachment;\s*filename="sample\.pdf"') { Add-Result 'AC-DOWNLOAD-1' 'PASS' $cd }
        else { Add-Result 'AC-DOWNLOAD-1' 'FAIL' "CD=$cd" }
    } catch { Add-Result 'AC-DOWNLOAD-1' 'FAIL' $_.Exception.Message }
}

# ------------------------------------------------------------------ AC-AUDIT-1
Write-Host '=== AC-AUDIT-1 audit_event ===' -ForegroundColor Cyan
if (-not $script:HappyPracticeId) { Add-Result 'AC-AUDIT-1' 'SKIP' 'No practice' }
else {
    $n = (Exec-DbQuery "SELECT COUNT(*) FROM audit_event WHERE event_type='ATTACHMENT_INGESTED' AND payload_json LIKE '%$($script:HappyPracticeId)%';").Trim()
    if ([int]$n -ge 1) { Add-Result 'AC-AUDIT-1' 'PASS' "rows=$n" }
    else { Add-Result 'AC-AUDIT-1' 'FAIL' "rows=$n" }
}

# ------------------------------------------------------------------ AC-IDEM-1
Write-Host '=== AC-IDEM-1 re-invio ID_WORKITEM ===' -ForegroundColor Cyan
if (-not $script:HappyAttId) { Add-Result 'AC-IDEM-1' 'SKIP' 'No attachment id' }
else {
    $before = (Exec-DbQuery "SELECT checksum_sha256,ingested_at FROM attachment WHERE id='$($script:HappyAttId)';")
    $cont = @{ NOME_FILE='sample'; ESTENSIONE='pdf'; ID_DOC='QA-IGNORED'; LINKDOWNLOAD='http://bpm-stub/files/sample.pdf' }
    $r = Invoke-OpenPractice -IdWorkitem $wi4 -NumPratica $np4 -Contenuto $cont
    $after = (Exec-DbQuery "SELECT checksum_sha256,ingested_at FROM attachment WHERE id='$($script:HappyAttId)';")
    if ($r.Body.resultCode -eq -5 -and $before -eq $after) { Add-Result 'AC-IDEM-1' 'PASS' 'resultCode=-5, checksum/timestamp invariati' }
    else { Add-Result 'AC-IDEM-1' 'FAIL' "resultCode=$($r.Body.resultCode); before=$before; after=$after" }
}

# ------------------------------------------------------------------ Summary
$Results | Format-Table -AutoSize | Out-Host
$Results | ConvertTo-Json -Depth 5 | Out-File (Join-Path $OutDir 'summary.json')
$Results | Export-Csv -NoTypeInformation -Path (Join-Path $OutDir 'summary.csv')
Write-Host ("Risultati salvati in {0}" -f $OutDir) -ForegroundColor Green
