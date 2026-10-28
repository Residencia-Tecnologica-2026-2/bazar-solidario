@echo off
cd /d "%~dp0backend"
call mvnw.cmd -Plocal -Dspring-boot.run.profiles=local spring-boot:run
pause
