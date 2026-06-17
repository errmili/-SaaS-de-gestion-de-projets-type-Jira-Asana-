# ============================================================
#   SAAS GESTION - SILENT STOPPER
#   Arrete uniquement les services lances par start-all
# ============================================================

$root = $PSScriptRoot
$logs = Join-Path $root 'logs'
$pidFile = Join-Path $logs 'pids.txt'

Write-Host ""
Write-Host "============================================================" -ForegroundColor Red
Write-Host "     ARRET DES MICROSERVICES SAAS GESTION" -ForegroundColor Red
Write-Host "============================================================" -ForegroundColor Red
Write-Host ""

if (-not (Test-Path $pidFile)) {
    Write-Host "  Aucun fichier pids.txt trouve." -ForegroundColor Yellow
    Write-Host "  Aucun service a arreter (ou deja arrete)." -ForegroundColor Yellow
} else {
    $lines = Get-Content $pidFile
    foreach ($line in $lines) {
        $parts = $line -split '\|'
        if ($parts.Count -lt 2) { continue }
        $svcPid = $parts[0]
        $svcName = $parts[1]

        Write-Host "  [STOP] $svcName (PID $svcPid)..." -ForegroundColor DarkGray
        # /T = kill tree (process et ses enfants) /F = force
        & taskkill /T /F /PID $svcPid 2>&1 | Out-Null
    }

    Remove-Item $pidFile -Force
    Write-Host ""
    Write-Host "  Fichier pids.txt nettoye." -ForegroundColor DarkGray
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "     ARRET TERMINE" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Start-Sleep -Seconds 2
