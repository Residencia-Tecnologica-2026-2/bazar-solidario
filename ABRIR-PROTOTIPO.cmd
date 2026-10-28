@echo off
title ISAC Brecho - Prototipo local
cd /d "%~dp0frontend"
where node >nul 2>nul
if errorlevel 1 (
  echo Node.js nao encontrado. Instale o Node.js para iniciar o prototipo.
  pause
  exit /b 1
)
if not exist node_modules\vite\bin\vite.js (
  echo Dependencias ausentes. Execute npm install na pasta frontend.
  pause
  exit /b 1
)
echo Prototipo: http://127.0.0.1:4174/
echo Mantenha esta janela aberta durante a apresentacao.
node node_modules\vite\bin\vite.js --host 127.0.0.1 --port 4174 --strictPort --open
echo Se a porta ja estiver em uso, abra http://127.0.0.1:4174/ no navegador.
pause
