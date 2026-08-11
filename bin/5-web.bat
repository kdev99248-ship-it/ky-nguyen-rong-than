@echo off
title= Apache + PHP 5.4.45
rem Web GM / login / CDN client  (Apache :81 -> cdn/www)
set "ROOT=%~dp0.."
"%ROOT%\cdn\apache\bin\httpd.exe" -d "%ROOT%\cdn\apache"
pause
