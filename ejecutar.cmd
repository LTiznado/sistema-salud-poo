@echo off
setlocal
cd /d "%~dp0"
if not exist "target\sistema-salud-poo-1.0.0.jar" (
    echo Primero compila con: mvnw.cmd clean verify
    exit /b 1
)
if defined JAVA_HOME (
    "%JAVA_HOME%\bin\java.exe" -jar "target\sistema-salud-poo-1.0.0.jar" %*
) else (
    java -jar "target\sistema-salud-poo-1.0.0.jar" %*
)
