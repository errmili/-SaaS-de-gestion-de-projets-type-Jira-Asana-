# ============================================================
#   SAAS GESTION - SILENT LAUNCHER (3 services)
#   Lance auth-service -> project-service -> file-service
#   notification-service et analytics-service exclus (pas finis)
# ============================================================

$ErrorActionPreference = 'Continue'
$root = $PSScriptRoot
$logs = Join-Path $root 'logs'
$pidFile = Join-Path $logs 'pids.txt'

# ============================================================
#   CONFIGURATION DES TEMPS D'ATTENTE
#   Augmente si tes services sont longs a demarrer
#   Diminue si ton PC est rapide
# ============================================================
$WAIT_AFTER_AUTH    = 45   # secondes apres auth-service
$WAIT_AFTER_PROJECT = 30   # secondes apres project-service
$WAIT_AFTER_FILE    = 20   # secondes apres file-service (juste pour confirmer)

# Creer le dossier logs
New-Item -ItemType Directory -Force -Path $logs | Out-Null
if (Test-Path $pidFile) { Remove-Item $pidFile -Force }

# ============================================================
#   FONCTION : lancer un microservice en arriere-plan
# ============================================================
function Start-MicroService {
    param([string]$Dir, [string]$Name)

    $svcPath = Join-Path $root $Dir
    $logFile = Join-Path $logs "$Dir.log"
    $errFile = Join-Path $logs "$Dir-error.log"
    $mvnwPath = Join-Path $svcPath 'mvnw.cmd'

    if (-not (Test-Path $svcPath)) {
        Write-Host "    [ERREUR] Dossier introuvable : $svcPath" -ForegroundColor Red
        return
    }
    if (-not (Test-Path $mvnwPath)) {
        Write-Host "    [ERREUR] mvnw.cmd introuvable dans : $svcPath" -ForegroundColor Red
        return
    }

    try {
        $proc = Start-Process -FilePath $mvnwPath `
            -ArgumentList 'spring-boot:run' `
            -WorkingDirectory $svcPath `
            -RedirectStandardOutput $logFile `
            -RedirectStandardError $errFile `
            -WindowStyle Hidden `
            -PassThru

        "$($proc.Id)|$Name|$Dir" | Out-File -Append -FilePath $pidFile -Encoding utf8
        Write-Host "    [LANCE] $Name (PID $($proc.Id)) - log : logs\$Dir.log" -ForegroundColor DarkGray
    } catch {
        Write-Host "    [ERREUR] Echec lancement $Name : $_" -ForegroundColor Red
    }
}

# ============================================================
#   FONCTION : attendre X secondes avec affichage
# ============================================================
function Wait-WithProgress {
    param([int]$Seconds, [string]$Reason)
    Write-Host "    [...] Attente $Seconds sec - $Reason" -ForegroundColor DarkYellow
    Start-Sleep -Seconds $Seconds
    Write-Host "    [OK] Pret pour la suite" -ForegroundColor Green
}

# ============================================================
#   DEMARRAGE
# ============================================================
Clear-Host
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "     SAAS GESTION - DEMARRAGE SILENCIEUX (3 services)" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Logs : $logs"
Write-Host "  Services exclus : notification-service, analytics-service"
Write-Host ""

Write-Host "[1/3] Auth Service..." -ForegroundColor Cyan
Start-MicroService -Dir 'auth-service' -Name 'Auth Service'
Wait-WithProgress -Seconds $WAIT_AFTER_AUTH -Reason "demarrage auth-service"

Write-Host "[2/3] Project Service..." -ForegroundColor Cyan
Start-MicroService -Dir 'project-service' -Name 'Project Service'
Wait-WithProgress -Seconds $WAIT_AFTER_PROJECT -Reason "demarrage project-service"

Write-Host "[3/3] File Service..." -ForegroundColor Cyan
Start-MicroService -Dir 'file-service' -Name 'File Service'
Wait-WithProgress -Seconds $WAIT_AFTER_FILE -Reason "demarrage file-service"

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "     LES 3 SERVICES SONT LANCES EN ARRIERE-PLAN" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Logs disponibles dans : $logs" -ForegroundColor White
Write-Host ""
Write-Host "  Pour suivre un log en temps reel :" -ForegroundColor Yellow
Write-Host "    Get-Content -Tail 30 -Wait logs\auth-service.log" -ForegroundColor Yellow
Write-Host "    Get-Content -Tail 30 -Wait logs\project-service.log" -ForegroundColor Yellow
Write-Host "    Get-Content -Tail 30 -Wait logs\file-service.log" -ForegroundColor Yellow
Write-Host ""
Write-Host "  Pour arreter : stop-all.bat" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
