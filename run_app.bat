@echo off
title KEC (R2024) Student Academic System
cd /d "%~dp0"
echo ======================================================================
echo  Kongu Engineering College (Autonomous) - Regulation 2024
echo  Student Academic and Continuous Evaluation System
echo ======================================================================
echo.
echo Compiling source files...
javac -cp ".;mysql-connector-j-8.0.33.jar;sqlite-jdbc-3.45.1.0.jar;slf4j-api-1.7.36.jar;slf4j-simple-1.7.36.jar" DBConnection.java StudentDAO.java AcademicDAO.java MainApp.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful! Launching application...
echo.
java -cp ".;mysql-connector-j-8.0.33.jar;sqlite-jdbc-3.45.1.0.jar;slf4j-api-1.7.36.jar;slf4j-simple-1.7.36.jar" MainApp
pause
