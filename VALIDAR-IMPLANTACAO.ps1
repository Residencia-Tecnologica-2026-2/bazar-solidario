param([Parameter(Mandatory=$true)][string]$SiteUrl)
$ErrorActionPreference = 'Stop'
$site = [Uri]$SiteUrl
if ($site.Scheme -ne 'https') { throw 'Informe o endereço HTTPS do site publicado.' }
foreach ($path in @('/api/health', '/api/anuncios?page=0&size=2', '/api/lojas?page=0&size=2', '/api/encontros')) {
    $response = Invoke-WebRequest -Uri ($site.GetLeftPart([UriPartial]::Authority) + $path) -SkipHttpErrorCheck -TimeoutSec 40
    if ($response.StatusCode -ne 200 -or $response.Headers['Content-Type'] -notmatch 'application/json') {
        throw "Falha em $path`: HTTP $($response.StatusCode), tipo $($response.Headers['Content-Type']). Verifique API e rewrite."
    }
    $null = $response.Content | ConvertFrom-Json
    Write-Host "OK $path — JSON HTTP 200"
}
Write-Host 'Leitura pública validada. Teste também cadastro, login, upload e compra com contas de teste.'
