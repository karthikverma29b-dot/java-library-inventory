@echo off
echo ==========================================================
echo   Compiling and Launching Library Book Inventory System
echo ==========================================================
if not exist "bin" mkdir bin
javac -d bin -sourcepath src src\com\library\app\Main.java src\com\library\test\InventoryTestRunner.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)
java -cp bin com.library.app.Main
pause
