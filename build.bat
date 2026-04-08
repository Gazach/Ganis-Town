@echo off
echo ===========================
echo   Building Java Project...
echo ===========================

:: Config
set OUT_DIR=out
set ASSETS_DIR=asset
set LIB_DIR=lib
set MAIN_CLASS=main

:: Create output folder if missing
if not exist %OUT_DIR% mkdir %OUT_DIR%

:: Collect ALL .java files from root + subfolders
dir /s /b *.java > sources.txt

:: Compile everything
echo Compiling...
javac -cp "%LIB_DIR%\*" -d %OUT_DIR% @sources.txt

echo Compiling finished with code %ERRORLEVEL%.

:: Check IMMEDIATELY after javac, before anything else touches ERRORLEVEL
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    del sources.txt
    pause
    exit /b 1
)

del sources.txt
echo [OK] Compilation successful!
echo.

:: Copy assets to out/asset/
echo Copying assets...
if not exist "%OUT_DIR%\%ASSETS_DIR%" mkdir "%OUT_DIR%\%ASSETS_DIR%"
xcopy /e /i /y "%ASSETS_DIR%\*" "%OUT_DIR%\%ASSETS_DIR%\" > nul
echo [OK] Assets copied!
echo.

:: Run
echo Running...
echo ===========================
java -cp "%OUT_DIR%;%LIB_DIR%\*" %MAIN_CLASS%

pause