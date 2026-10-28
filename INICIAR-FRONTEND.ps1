$ErrorActionPreference = "Stop"
if (-not (Get-Command node -ErrorAction SilentlyContinue)) { throw "Instale Node.js 22.18 ou superior. Veja GUIA-VSCODE.md." }
Set-Location (Join-Path $PSScriptRoot "frontend")
if (-not (Test-Path -LiteralPath "node_modules")) {
    & npm.cmd ci
    if ($LASTEXITCODE -ne 0) { throw "Falha ao instalar dependências. Confira a conexão." }
}
Write-Host "Aplicativo: http://127.0.0.1:4186/"
& node node_modules/vite/bin/vite.js --host 127.0.0.1 --port 4186 --strictPort
exit $LASTEXITCODE
