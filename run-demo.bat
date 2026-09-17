@echo off
setlocal
cd /d "%~dp0"
echo ===================================================
echo  FastAIState Demo
echo ===================================================
echo [1/3] Building FastAIState...
call "C:\Users\andre\tools\apache-maven-3.9.9\bin\mvn.cmd" install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo FastAIState build failed!
    exit /b %ERRORLEVEL%
)

echo [2/3] Compiling Demo...
cd examples\Demo
call "C:\Users\andre\tools\apache-maven-3.9.9\bin\mvn.cmd" compile
if %ERRORLEVEL% NEQ 0 (
    echo Demo compilation failed!
    exit /b %ERRORLEVEL%
)

echo [3/3] Running Demo...
call "C:\Users\andre\tools\apache-maven-3.9.9\bin\mvn.cmd" exec:java "-Dexec.mainClass=fastaistate.demo.Demo"
cd ..\..
pause