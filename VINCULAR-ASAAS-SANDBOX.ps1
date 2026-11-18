param([long]$VendedorId = 0, [string]$CarteiraIsac = '')
$ErrorActionPreference = 'Stop'
$arquivoPrivado = Join-Path $PSScriptRoot 'backend/.asaas-local.properties'
if (-not (Test-Path -LiteralPath $arquivoPrivado)) { throw 'Execute CONFIGURAR-ASAAS.ps1 primeiro.' }
if ($VendedorId -le 0) {
    Write-Host 'Use o vendedorId do usuário LOJA no aplicativo, e não o id da loja.'
    $VendedorId = [long](Read-Host 'ID do vendedor local representado pela conta da chave atual')
}
if ($VendedorId -le 0) { throw 'Informe um ID positivo.' }
if ([string]::IsNullOrWhiteSpace($CarteiraIsac)) { $CarteiraIsac = Read-Host 'Wallet ID da SEGUNDA conta Sandbox (ISAC)' }
$uuidCarteira = [Guid]::Empty
if (-not [Guid]::TryParse($CarteiraIsac, [ref]$uuidCarteira) -or $uuidCarteira -eq [Guid]::Empty) { throw 'Wallet ID inválido.' }
$configPrivada = [IO.File]::ReadAllText($arquivoPrivado)
$tokenApi = [regex]::Match($configPrivada, '(?m)^app\.asaas\.api-key=(.+)\r?$').Groups[1].Value.Trim()
try {
    $carteirasEmissoras = Invoke-RestMethod -Uri 'https://api-sandbox.asaas.com/v3/wallets/' -Method Get `
        -Headers @{ access_token=$tokenApi; 'User-Agent'='ISAC-Brecho-Sandbox/1.0' } -TimeoutSec 20
    if (($carteirasEmissoras | ConvertTo-Json -Depth 8).Contains($CarteiraIsac)) {
        throw 'A carteira do ISAC precisa pertencer à segunda conta, diferente da emissora.'
    }
    $configPrivada = [regex]::Replace($configPrivada, '(?m)^app\.asaas\.(seller-id|isac-wallet-id)=[^\r\n]*\r?\n?', '')
    if ($configPrivada -notmatch '(?m)^app\.asaas\.webhook-token=') {
        $bytesWebhook = New-Object byte[] 48
        [Security.Cryptography.RandomNumberGenerator]::Fill($bytesWebhook)
        $configPrivada = $configPrivada.TrimEnd() + "`napp.asaas.webhook-token=" + [Convert]::ToBase64String($bytesWebhook) + "`n"
    }
    $configPrivada = $configPrivada.TrimEnd() + "`napp.asaas.seller-id=$VendedorId`napp.asaas.isac-wallet-id=$CarteiraIsac`n"
    [IO.File]::WriteAllText($arquivoPrivado, $configPrivada, [Text.UTF8Encoding]::new($false))
    Write-Host 'Vinculação salva. Reinicie o backend. A cobrança ainda requer teste completo no Sandbox.'
} finally { $tokenApi=$null; $configPrivada=$null; $carteirasEmissoras=$null }
