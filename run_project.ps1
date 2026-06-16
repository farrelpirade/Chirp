Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "      [ CHIRP PROJECT LAUNCHER ]" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

function Get-JavaHomeFromCommand {
    param([string]$commandName)
    $cmd = Get-Command $commandName -ErrorAction SilentlyContinue
    if ($cmd) {
        $javaExe = $cmd.Source
        $javaBin = Split-Path $javaExe
        $javaHome = Split-Path $javaBin
        if (Test-Path (Join-Path $javaHome 'bin\java.exe')) {
            return $javaHome
        }

        $output = & "$javaExe" -XshowSettings:properties -version 2>&1 | Select-String -Pattern '^[\s]*java.home\s*=.*$'
        if ($output) {
            $home = ($output -replace '^[\s]*java.home\s*=\s*', '').Trim()
            if (Test-Path (Join-Path $home 'bin\java.exe')) {
                return $home
            }
        }
    }
    return $null
}

function Get-JavaHomeFromRegistry {
    $registryKeys = @(
        'HKLM:\SOFTWARE\JavaSoft\Java Development Kit',
        'HKLM:\SOFTWARE\WOW6432Node\JavaSoft\Java Development Kit',
        'HKLM:\SOFTWARE\JavaSoft\JDK',
        'HKLM:\SOFTWARE\WOW6432Node\JavaSoft\JDK'
    )

    foreach ($keyPath in $registryKeys) {
        try {
            $baseKey = Get-ItemProperty -Path $keyPath -ErrorAction Stop
            if ($baseKey.PSObject.Properties.Name -contains 'CurrentVersion') {
                $versionKey = Join-Path $keyPath $baseKey.CurrentVersion
                $versionProps = Get-ItemProperty -Path $versionKey -ErrorAction SilentlyContinue
                if ($versionProps -and $versionProps.JavaHome) {
                    return $versionProps.JavaHome
                }
            }
            elseif ($baseKey.JavaHome) {
                return $baseKey.JavaHome
            }
        } catch {
            continue
        }
    }
    return $null
}

function Install-JavaWithWinget {
    $winget = Get-Command winget -ErrorAction SilentlyContinue
    if (-not $winget) {
        return $false
    }

    $candidates = @(
        'EclipseAdoptium.Temurin.21.JDK',
        'Microsoft.OpenJDK.21'
    )

    foreach ($id in $candidates) {
        Write-Host "Attempting to install $id..." -ForegroundColor Cyan
        & winget install --id $id -e --accept-package-agreements --accept-source-agreements
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Successfully installed $id." -ForegroundColor Green
            return $true
        }
        Write-Host "Install attempt failed for $id." -ForegroundColor Yellow
    }

    return $false
}

function Get-JavaHomeFromCommonPaths {
    $commonDirs = @(
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Java",
        "C:\Program Files\Microsoft",
        "C:\Program Files (x86)\Java"
    )
    foreach ($dir in $commonDirs) {
        if (Test-Path $dir) {
            $subdirs = Get-ChildItem $dir -Directory | Where-Object { $_.Name -match '^(jdk|jre|openjdk)' }
            foreach ($subdir in $subdirs) {
                $candidate = $subdir.FullName
                if (Test-Path (Join-Path $candidate 'bin\java.exe')) {
                    return $candidate
                }
            }
        }
    }
    return $null
}

function Resolve-JavaHome {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
        return $env:JAVA_HOME
    }

    $candidate = Get-JavaHomeFromCommand 'javac'
    if (-not $candidate) {
        $candidate = Get-JavaHomeFromCommand 'java'
    }
    if ($candidate) {
        return $candidate
    }

    $candidate = Get-JavaHomeFromRegistry
    if ($candidate) {
        return $candidate
    }

    $candidate = Get-JavaHomeFromCommonPaths
    if ($candidate) {
        return $candidate
    }

    return $null
}

$resolvedJavaHome = Resolve-JavaHome
if (-not $resolvedJavaHome) {
    Write-Host "No Java installation was found." -ForegroundColor Yellow
    if (Install-JavaWithWinget) {
        Write-Host "Re-checking Java after installation..." -ForegroundColor Cyan
        $resolvedJavaHome = Resolve-JavaHome
    }
}

if (-not $resolvedJavaHome) {
    Write-Host "ERROR: Could not resolve a valid Java installation." -ForegroundColor Red
    Write-Host "Please install a Java JDK and ensure it is available in PATH or set JAVA_HOME manually." -ForegroundColor Yellow
    return
}

$env:JAVA_HOME = $resolvedJavaHome
Write-Host "Resolved JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Green

# 1. Launch Spring Boot Backend
Write-Host "[1/2] Launching Java Spring Boot Backend..." -ForegroundColor Green
Start-Process cmd -ArgumentList '/k "echo Starting Spring Boot backend... && cd backend && mvnw.cmd spring-boot:run"'

# 2. Check and launch Nuxt Frontend
Write-Host "[2/2] Launching Nuxt Frontend..." -ForegroundColor Green
if (-not (Test-Path "frontend\node_modules")) {
    Write-Host "node_modules not found. Installing dependencies first..." -ForegroundColor Yellow
    Push-Location frontend
    npm install
    Pop-Location
}

Start-Process cmd -ArgumentList '/k "echo Starting Nuxt frontend... && cd frontend && npm run dev"'

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Chirp Backend and Frontend are starting!" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Read-Host "Press Enter to exit..."
