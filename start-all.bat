@echo off
REM Wrapper qui lance le script PowerShell avec bypass de l'execution policy
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-all.ps1"
pause
