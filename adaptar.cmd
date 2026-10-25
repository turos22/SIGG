@echo off
rem Prepara esta maquina (Windows) para desenvolver o SIGG: duplo clique, ou "adaptar.cmd --ajuda".
rem Aqui so garantimos o Node.js (instalando se faltar); o resto da logica esta em adaptar.mjs.
setlocal
cd /d "%~dp0"

where node >/dev/null 2>nul
if not errorlevel 1 goto rodar

set "SIM="
set "VERIFICAR="
for %%A in (%*) do (
  if /i "%%~A"=="--sim" set "SIM=1"
  if /i "%%~A"=="--verificar" set "VERIFICAR=1"
)
if defined VERIFICAR (
  echo [sigg] Node.js nao encontrado. Rode sem --verificar para instalar.
  goto falhou
)
where winget >/dev/null 2>nul
if errorlevel 1 (
  echo [sigg] Node.js nao encontrado e o winget nao esta disponivel.
  echo        Instale o Node.js LTS em https://nodejs.org e rode de novo.
  goto falhou
)
if not defined SIM (
  choice /C SN /M "[sigg] Node.js nao encontrado. Instalar o LTS via winget agora"
  if errorlevel 2 goto falhou
)
winget install -e --id OpenJS.NodeJS.LTS --accept-source-agreements --accept-package-agreements
if errorlevel 1 goto falhou
rem O instalador atualiza o PATH so para janelas novas; nesta, adiciona na mao.
set "PATH=%ProgramFiles%\nodejs;%PATH%"
where node >/dev/null 2>nul
if errorlevel 1 (
  echo [sigg] Node.js instalado, mas nao encontrado. Feche esta janela e rode de novo.
  goto falhou
)

:rodar
node adaptar.mjs %*
if errorlevel 1 goto falhou
rem Pausa para o resumo nao sumir quando aberto com duplo clique
pause
exit /b 0

:falhou
pause
exit /b 1
