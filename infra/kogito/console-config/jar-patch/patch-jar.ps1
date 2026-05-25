param(
    [string]$JarPath,
    [string]$PatchedBundlePath,
    [string]$EntryName = "META-INF/resources/app.bundle.js"
)

Add-Type -Assembly "System.IO.Compression"
Add-Type -Assembly "System.IO.Compression.FileSystem"

Write-Host "Opening JAR: $JarPath"
$zip = [System.IO.Compression.ZipFile]::Open($JarPath, [System.IO.Compression.ZipArchiveMode]::Update)

$entry = $zip.GetEntry($EntryName)
if ($null -eq $entry) {
    Write-Host "ERROR: Entry '$EntryName' not found in JAR"
    $zip.Dispose()
    exit 1
}

Write-Host "Found entry: $EntryName (compressed size: $($entry.CompressedLength))"

# Delete old entry and create new one
$entry.Delete()
$newEntry = $zip.CreateEntry($EntryName, [System.IO.Compression.CompressionLevel]::Optimal)
$newEntryStream = $newEntry.Open()
$patchedBytes = [System.IO.File]::ReadAllBytes($PatchedBundlePath)
$newEntryStream.Write($patchedBytes, 0, $patchedBytes.Length)
$newEntryStream.Close()

$zip.Dispose()
Write-Host "DONE. New entry size: $($patchedBytes.Length) bytes"
