@echo off
rem Self-bootstrapping Gradle Wrapper for Windows
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    gradle %*
    exit /b %ERRORLEVEL%
)

set GRADLE_VERSION=8.10.2
set GRADLE_HOME=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin

if not exist "%GRADLE_HOME%" (
    echo Downloading Gradle %GRADLE_VERSION%...
    mkdir "%GRADLE_HOME%"
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%TEMP%\gradle-%GRADLE_VERSION%-bin.zip'"
    powershell -Command "Expand-Archive -Path '%TEMP%\gradle-%GRADLE_VERSION%-bin.zip' -DestinationPath '%GRADLE_HOME%'"
    del "%TEMP%\gradle-%GRADLE_VERSION%-bin.zip"
)

for /r "%GRADLE_HOME%" %%i in (gradle.bat) do (
    if exist "%%i" (
        "%%i" %*
        exit /b %ERRORLEVEL%
    )
)

echo Gradle could not be located.
exit /b 1
