@echo off
title= Khoi dong toan bo server
set "ROOT=%~dp0.."

echo ============================================
echo  KY NGUYEN RONG THAN - khoi dong toan bo
echo ============================================
echo.

echo [1/5] MySQL :3306
start "MySQL" "%~dp01-mysql.bat"
echo       cho MySQL san sang...
timeout /t 12 /nobreak >nul

echo [2/5] UserCenter :9000
start "UserCenter" "%~dp02-usercenter.bat"
timeout /t 8 /nobreak >nul

echo [3/5] GameServer :9001 / GM :51011
start "GameServer" "%~dp03-gameserver.bat"
timeout /t 8 /nobreak >nul

echo [4/5] NapCard - Tomcat :80
start "NapCard" "%~dp04-napcard.bat"
timeout /t 5 /nobreak >nul

echo [5/5] Apache + PHP :81
start "Apache" "%~dp05-web.bat"

echo.
echo Da khoi dong xong. Kiem tra tung cua so console.
echo   GM panel : http://127.0.0.1:81/gm/
echo   Nap the  : http://127.0.0.1/
pause
