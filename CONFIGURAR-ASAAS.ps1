$ErrorActionPreference = 'Stop'
$destino = Join-Path $PSScriptRoot 'backend/.asaas-local.properties'
$segredo = Read-Host 'Cole a chave do Asaas Sandbox (entrada oculta)' -AsSecureString
$ponteiro = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segredo)
try {
    $chave = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ponteiro).Trim()
    if (-not $chave.StartsWith('$aact_') -or $chave.Contains("`n") -or $chave.Contains("`r")) {
        throw 'Formato inesperado. Use a chave de API copiada do Sandbox.'
    }
    # Nenhum segredo é enviado ao console ou a um serviço externo.
    $conteudoAnterior = if (Test-Path -LiteralPath $destino) { [IO.File]::ReadAllText($destino) } else { '' }
    $conteudoAnterior = [regex]::Replace($conteudoAnterior, '(?m)^app\.asaas\.(base-url|api-key)=[^\r\n]*\r?\n?', '')
    $conteudo = $conteudoAnterior.TrimEnd() + "`napp.asaas.base-url=https://api-sandbox.asaas.com/v3`napp.asaas.api-key=$chave`n"
    [IO.File]::WriteAllText($destino, $conteudo, [Text.UTF8Encoding]::new($false))
    Write-Host 'Chave salva na configuração local ignorada pelo Git. Pagamento ainda não ativado.'
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ponteiro)
    $chave = $null
    $conteudo = $null
    $conteudoAnterior = $null
    $segredo.Dispose()
}
