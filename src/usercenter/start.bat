@echo off
::color 1f
title= UserCenter
cd /d "%~dp0"
set "JAVA_HOME=%~dp0..\..\runtime\jdk1.8.0_181"
"%JAVA_HOME%\bin\java.exe" -Xms512m -Xmn512m -Xss256k -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./oom.hprof -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:GCPauseIntervalMillis=200 -XX:SurvivorRatio=6 -Xloggc:logs/gc.log -classpath "./*;./UserCenter_lib/*" com.playmore.http.jetty.SpringContextLoader
pause
