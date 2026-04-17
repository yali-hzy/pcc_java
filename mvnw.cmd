@echo off
setlocal

set "BASE_DIR=%~dp0"
set "MVN_VERSION=3.9.9"
set "MAVEN_DIR=%BASE_DIR%.mvn\apache-maven-%MVN_VERSION%"
set "MAVEN_ZIP=%BASE_DIR%.mvn\apache-maven-%MVN_VERSION%-bin.zip"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MVN_VERSION%/binaries/apache-maven-%MVN_VERSION%-bin.zip"

if not exist "%MAVEN_DIR%\bin\mvn.cmd" (
  echo Maven %MVN_VERSION% not found in .mvn, downloading...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%'"
  if errorlevel 1 (
    echo Failed to download Maven from %MAVEN_URL%
    exit /b 1
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%BASE_DIR%.mvn' -Force"
  if errorlevel 1 (
    echo Failed to extract Maven zip.
    exit /b 1
  )
)

"%MAVEN_DIR%\bin\mvn.cmd" %*
exit /b %errorlevel%
