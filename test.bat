@echo off
echo ==========================================================
echo   Running CE-3 Automated Rubric Verification Tests
echo ==========================================================
if not exist "bin" mkdir bin
javac -d bin -sourcepath src src\com\library\test\InventoryTestRunner.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)
java -cp bin com.library.test.InventoryTestRunner
pause
