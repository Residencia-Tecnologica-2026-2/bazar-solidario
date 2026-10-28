$ErrorActionPreference = "Stop"
if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw "Instale JDK 21 ou superior. Veja GUIA-VSCODE.md." }
Write-Host "API local: http://127.0.0.1:8080/home"
Set-Location (Join-Path $PSScriptRoot "backend")
if (-not $env:JWT_SECRET) {
    $secretsPath = Join-Path (Get-Location) ".local-secrets.properties"
    if (-not (Test-Path -LiteralPath $secretsPath)) {
        $keyBytes = New-Object byte[] 48
        [System.Security.Cryptography.RandomNumberGenerator]::Fill($keyBytes)
        $localKey = [Convert]::ToBase64String($keyBytes)
        [System.IO.File]::WriteAllText($secretsPath, "jwt.secret=$localKey")
    }
}
& .\mvnw.cmd -Plocal "-Dspring-boot.run.profiles=local" spring-boot:run
exit $LASTEXITCODE
