@echo off
setlocal
cd /d "%~dp0"
echo ===================================================
echo  Building FastAIState JMH Benchmarks Uber-Jar
echo ===================================================
echo [1/3] Building FastAIState...
call "C:\Users\andre\tools\apache-maven-3.9.9\bin\mvn.cmd" install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo FastAIState install failed!
    exit /b %ERRORLEVEL%
)

echo [2/3] Building Benchmark Uber-JAR...
cd examples\Benchmark
call "C:\Users\andre\tools\apache-maven-3.9.9\bin\mvn.cmd" clean package
if %ERRORLEVEL% NEQ 0 (
    echo Benchmark package failed!
    exit /b %ERRORLEVEL%
)

echo [3/3] Running JMH Benchmarks...
java -jar target\benchmarks.jar -f 1 -wi 2 -i 3 -tu ms -bm thrpt
pause