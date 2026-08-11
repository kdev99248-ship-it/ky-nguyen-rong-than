@echo off
title= Dung toan bo server
set "ROOT=%~dp0.."

echo Dung Apache...
taskkill /F /IM httpd.exe >nul 2>&1

echo Dung Tomcat + GameServer + UserCenter (java.exe)...
taskkill /F /IM java.exe >nul 2>&1

echo Dung MySQL (shutdown sach de khong hong InnoDB)...
"%ROOT%\db\mysql\bin\mysqladmin.exe" -uroot -pxpymw.com shutdown 2>nul
timeout /t 5 /nobreak >nul
taskkill /F /IM mysqld.exe >nul 2>&1

echo Da dung toan bo.
pause
