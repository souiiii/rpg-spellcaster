@echo off
setlocal

echo =============================================
echo  RPG Spellcaster - Build Helper
echo =============================================
echo.

:: ── Check Java 17+ ───────────────────────────────────────────────────────────
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VER=%%v
)
echo [INFO] Detected Java version: %JAVA_VER%
echo [INFO] NOTE: Paper MC 1.20 requires Java 17 or newer to compile.
echo.

:: ── Check Maven ───────────────────────────────────────────────────────────────
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Maven found on PATH.
    echo.
    echo [INFO] Running: mvn clean package
    echo.
    mvn clean package
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo =============================================
        echo  BUILD SUCCESS!
        echo  JAR: target\rpg-spellcaster-1.0.0.jar
        echo =============================================
    ) else (
        echo [ERROR] Build failed. See errors above.
    )
) else (
    echo [WARN] Maven (mvn) not found on PATH.
    echo.
    echo  Please install Maven using one of these methods:
    echo   1. Download from https://maven.apache.org/download.cgi
    echo      and add the bin/ folder to your PATH, OR
    echo   2. Install via Scoop: scoop install maven
    echo   3. Install via Chocolatey: choco install maven
    echo   4. Open this project in IntelliJ IDEA which includes Maven.
    echo.
    echo  After installing, run this script again, or run:
    echo    mvn clean package
    echo.
)

pause
