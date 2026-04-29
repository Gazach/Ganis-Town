@echo off
echo ===========================
echo   Building Java Fat JAR...
echo ===========================

:: Config
set OUT_DIR=out
set BUILD_DIR=build
set TEMP_DIR=temp_extract
set ASSETS_DIR=asset
set LIB_DIR=lib
set MAIN_CLASS=main
set JAR_NAME=ganis_town.jar

:: Clean previous build (SAFE)
if exist %OUT_DIR% rmdir /s /q %OUT_DIR%
if exist %BUILD_DIR% rmdir /s /q %BUILD_DIR%
if exist %TEMP_DIR% rmdir /s /q %TEMP_DIR%
if exist %JAR_NAME% del %JAR_NAME%

mkdir %OUT_DIR%
mkdir %BUILD_DIR%
mkdir %TEMP_DIR%

:: ===========================
:: COMPILE SOURCE
:: ===========================
echo Compiling source...
dir /s /b *.java > sources.txt

javac -cp "%LIB_DIR%\*" -d %OUT_DIR% @sources.txt

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

:: ===========================
:: COPY CLASSES
:: ===========================
echo Copying compiled classes...
xcopy /e /i /y "%OUT_DIR%\*" "%BUILD_DIR%\" > nul

:: ===========================
:: COPY ASSETS
:: ===========================
echo Copying assets...
if exist "%ASSETS_DIR%" (
    xcopy /e /i /y "%ASSETS_DIR%\*" "%BUILD_DIR%\%ASSETS_DIR%\" > nul
)
echo [OK] Assets ready!
echo.

:: ===========================
:: EXTRACT LIBRARIES SAFELY
:: ===========================
echo Extracting libraries...

for %%f in (%LIB_DIR%\*.jar) do (
    echo   -> %%~nxf
    cd %TEMP_DIR%
    jar xf "..\%%f"
    cd ..
    xcopy /e /i /y "%TEMP_DIR%\*" "%BUILD_DIR%\" > nul
    rmdir /s /q %TEMP_DIR%
    mkdir %TEMP_DIR%
)

echo [OK] Libraries merged!
echo.

:: ===========================
:: CREATE MANIFEST
:: ===========================
echo Creating manifest...
echo Main-Class: %MAIN_CLASS% > manifest.txt
echo. >> manifest.txt

:: ===========================
:: BUILD JAR
:: ===========================
echo Building FAT JAR...
jar cfm %JAR_NAME% manifest.txt -C %BUILD_DIR% .

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] JAR creation failed!
    del manifest.txt
    pause
    exit /b 1
)

del manifest.txt
rmdir /s /q %TEMP_DIR%

echo [OK] FAT JAR created: %JAR_NAME%
echo.

:: ===========================
:: TEST RUN
:: ===========================
echo Running JAR...
echo ===========================
java -jar %JAR_NAME%

pause