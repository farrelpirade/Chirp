@echo off
title Chirp Project Launcher
echo ==========================================
echo       [ CHIRP PROJECT LAUNCHER ]
echo ==========================================
echo.

:: Set Java Home path for Java 25
set "JAVA_HOME=C:\Program Files\Java\jdk-25"

:: 1. Launch Spring Boot Backend
echo [1/2] Launching Java Spring Boot Backend...
start "Chirp Backend (Port 8080)" cmd /k "echo Starting Spring Boot backend... && cd backend && mvnw.cmd spring-boot:run"

:: 2. Install Nuxt Frontend dependencies if missing, and launch dev server
echo [2/2] Launching Nuxt Frontend...
if not exist "frontend\node_modules" (
    echo node_modules not found. Installing dependencies first...
    cd frontend
    call npm install
    cd ..
)

start "Chirp Frontend (Port 3000)" cmd /k "echo Starting Nuxt frontend... && cd frontend && npm run dev"

echo.
echo ==========================================
echo  Chirp Backend and Frontend are starting!
echo  - Backend log window is running.
echo  - Frontend log window is running.
echo ==========================================
echo.
pause
