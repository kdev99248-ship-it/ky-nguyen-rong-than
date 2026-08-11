@echo off
title= MySQL 5.6.49
set "ROOT=%~dp0.."
echo Khoi dong MySQL tren port 3306...
"%ROOT%\db\mysql\bin\mysqld.exe" --defaults-file="%ROOT%\db\mysql\my.ini" --console
pause
