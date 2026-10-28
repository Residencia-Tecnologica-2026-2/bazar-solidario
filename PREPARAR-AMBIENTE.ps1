param([switch]$Reinstalar)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
foreach ($programa in @("java", "node", "npm")) {
    if (-not (Get-Command $programa -ErrorAction SilentlyContinue)) {
        throw "Não encontrei $programa. Consulte GUIA-VSCODE.md e instale os requisitos antes de continuar."
    }
}
$nodeVersion = [version]((& node --version).TrimStart("v"))
if ($nodeVersion -lt [version]"22.18.0") { throw "Use Node.js 22.18 ou superior. Versão atual: $nodeVersion" }
Write-Host "Node: $nodeVersion"
& java -version
if ($LASTEXITCODE -ne 0) { throw "Não foi possível iniciar o Java. Use JDK 21 ou superior." }
Push-Location (Join-Path $PSScriptRoot "frontend")
try {
    if ($Reinstalar -or -not (Test-Path -LiteralPath "node_modules")) {
        & npm.cmd ci
        if ($LASTEXITCODE -ne 0) { throw "A instalação falhou. Confira a conexão e execute novamente." }
    } else { Write-Host "Dependências existentes preservadas. Para reinstalar: ./PREPARAR-AMBIENTE.ps1 -Reinstalar" }
} finally { Pop-Location }
Write-Host "Ambiente preparado. Execute as tarefas de backend e frontend do workspace."
