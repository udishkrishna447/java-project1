@echo off
title KEC (R2024) Student Academic System - Automated Demo
color 0A
mode con: cols=110 lines=45
cd /d "%~dp0"
cls
java -cp ".;mysql-connector-j-8.0.33.jar;sqlite-jdbc-3.45.1.0.jar;slf4j-api-1.7.36.jar;slf4j-simple-1.7.36.jar" AutoDemo
pause
