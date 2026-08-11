@echo off
title= NapCard (Tomcat)
rem Webapp nap the - Tomcat :80, docBase tro vao src/napcard (conf/Catalina/localhost/ROOT.xml)
set "ROOT=%~dp0.."
set "CATALINA_HOME=%ROOT%\runtime\tomcat8.5"
call "%CATALINA_HOME%\bin\startup.bat"
