@echo off
REM Wrapper qui lance le script PowerShell d'arret
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-all.ps1"
