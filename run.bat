@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
call .\mvnw.cmd clean javafx:run
pause 