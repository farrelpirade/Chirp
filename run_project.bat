@echo off
title Chirp Project Launcher
echo ==========================================
echo       [ CHIRP PROJECT LAUNCHER ]
echo ==========================================
echo.
setlocal

call :ResolveJavaHome
if errorlevel 1 goto NoJava

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
exit /b 0

:ResolveJavaHome
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        echo Using existing JAVA_HOME=%JAVA_HOME%
        exit /b 0
    )
    echo WARNING: JAVA_HOME is set but does not point to a valid Java installation: %JAVA_HOME%
)

for /f "usebackq delims=" %%A in (`where java 2^>nul`) do (
    call :DeriveJavaHomeFromExe "%%~fA"
    if not errorlevel 1 goto JavaFound
)
for /f "usebackq delims=" %%A in (`where javac 2^>nul`) do (
    call :DeriveJavaHomeFromExe "%%~fA"
    if not errorlevel 1 goto JavaFound
)
call :ScanCommonPaths
if not errorlevel 1 goto JavaFound

echo WARNING: Java executable not found on PATH.
echo Attempting automatic Java install if winget is available...
call :InstallJava
if errorlevel 1 exit /b 1

rem Try detection again after install
for /f "usebackq delims=" %%A2 in (`where java 2^>nul`) do (
    call :DeriveJavaHomeFromExe "%%~fA2"
    if not errorlevel 1 goto JavaFound
)
for /f "usebackq delims=" %%A2 in (`where javac 2^>nul`) do (
    call :DeriveJavaHomeFromExe "%%~fA2"
    if not errorlevel 1 goto JavaFound
)
call :ScanCommonPaths
if not errorlevel 1 goto JavaFound

echo ERROR: Java executable still not found after automatic install.
exit /b 1

:JavaFound
if exist "%JAVA_HOME%\bin\java.exe" (
    echo Resolved JAVA_HOME=%JAVA_HOME%
    exit /b 0
)
echo ERROR: Failed to derive JAVA_HOME from %JAVA_EXE%.
exit /b 1

:DeriveJavaHomeFromExe
setlocal enabledelayedexpansion
set "JAVA_EXE=%~1"
for %%B in ("%JAVA_EXE%") do set "JAVA_BIN=%%~dpB"
for %%B in ("%JAVA_BIN%..") do set "JAVA_HOME=%%~fB"
if exist "!JAVA_HOME!\bin\java.exe" (
    endlocal & set "JAVA_HOME=%JAVA_HOME%"
    exit /b 0
)
for /f "tokens=2* delims==" %%B in ('cmd /c ""%JAVA_EXE%" -XshowSettings:properties -version 2^>^1 ^| findstr /i "java.home""') do (
    set "JAVA_HOME=%%C"
)
for %%B in ("!JAVA_HOME!") do set "JAVA_HOME=%%~B"
if defined JAVA_HOME if exist "!JAVA_HOME!\bin\java.exe" (
    endlocal & set "JAVA_HOME=%JAVA_HOME%"
    exit /b 0
)
endlocal
exit /b 1

:ScanCommonPaths
for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA_HOME=%%D"
        exit /b 0
    )
)
for /d %%D in ("C:\Program Files\Java\jdk-*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA_HOME=%%D"
        exit /b 0
    )
)
for /d %%D in ("C:\Program Files\Microsoft\jdk-*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA_HOME=%%D"
        exit /b 0
    )
)
for /d %%D in ("C:\Program Files (x86)\Java\jdk-*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA_HOME=%%D"
        exit /b 0
    )
)
exit /b 1

:InstallJava
where winget >nul 2>&1
if errorlevel 1 (
    echo WARNING: winget is not installed. Cannot auto-install Java from this script.
    exit /b 1
)

echo winget found. Installing Java 21 via winget...
 powershell -NoProfile -Command "& {try { winget install --id EclipseAdoptium.Temurin.21.JDK -e --accept-package-agreements --accept-source-agreements } catch { exit 1 }}"
if errorlevel 1 (
    echo First install attempt failed. Trying Microsoft.OpenJDK.21...
     powershell -NoProfile -Command "& {try { winget install --id Microsoft.OpenJDK.21 -e --accept-package-agreements --accept-source-agreements } catch { exit 1 }}"
)
if errorlevel 1 (
    echo ERROR: Automatic Java install failed. Please install a JDK manually.
    exit /b 1
)
echo Java installation completed. Please wait while the script re-checks Java.
exit /b 0

:NoJava
echo.
echo ERROR: Cannot start the backend without a valid Java installation.
echo Please install a Java JDK and ensure it is available in PATH or set JAVA_HOME correctly.
pause
exit /b 1
