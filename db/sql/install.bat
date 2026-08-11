@echo off
:: ===========================================================
::  Nap database cho Ky Nguyen Rong Than (cai moi / server sach)
::  MySQL phai dang chay truoc: bin\1-mysql.bat
::
::  Bo cai nay sinh tu DB dang chay va da doi chieu 1:1:
::    - schema khop 100% (tru 5 bang rac _copy da co y loai)
::    - h_game_data: 148/148 bang khop dung so dong
::  Server se sach: KHONG co account, nhan vat, log.
::
::  CANH BAO: ghi de toan bo 6 database duoi day.
:: ===========================================================
setlocal
set "MYSQL=%~dp0..\mysql\bin\mysql.exe"
set "USER=root"
set "PASS=xpymw.com"
set "SQLDIR=%~dp0install"

if not exist "%MYSQL%" (
  echo Khong tim thay "%MYSQL%"
  pause & exit /b 1
)

echo.
echo   Se GHI DE cac database:
echo     user_center, h_game, h_game_data,
echo     h_game_log, h_game_global_log, nap_card
echo.
set /p OK="Go YES roi Enter de tiep tuc: "
if /i not "%OK%"=="YES" (echo Da huy. & pause & exit /b 1)

echo.
echo [0/6] Tao database...
"%MYSQL%" -u%USER% -p%PASS% --default-character-set=utf8 < "%SQLDIR%\00-create-databases.sql"
if errorlevel 1 goto :err

call :imp 1 user_center       01-user_center.sql
if errorlevel 1 goto :err
call :imp 2 h_game            02-h_game.sql
if errorlevel 1 goto :err
call :imp 3 h_game_data       03-h_game_data.sql
if errorlevel 1 goto :err
call :imp 4 h_game_log        04-h_game_log.sql
if errorlevel 1 goto :err
call :imp 5 h_game_global_log 05-h_game_global_log.sql
if errorlevel 1 goto :err
call :imp 6 nap_card          06-nap_card.sql
if errorlevel 1 goto :err

echo.
echo   HOAN TAT.
echo   Kiem tra: db\mysql\bin\mysql.exe -uroot -p%PASS% -e "show databases;"
echo.
echo   File trong optional\ KHONG duoc nap tu dong - xem db\sql\README.md
echo.
pause
exit /b 0

:imp
echo [%~1/6] %~2  ^<-  %~3
"%MYSQL%" -u%USER% -p%PASS% --default-character-set=utf8 %~2 < "%SQLDIR%\%~3"
exit /b %errorlevel%

:err
echo.
echo   *** LOI khi nap SQL - da dung lai. Xem thong bao phia tren. ***
pause
exit /b 1
