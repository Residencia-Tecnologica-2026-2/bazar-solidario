import { writeFile } from 'node:fs/promises';
import { fileURLToPath, pathToFileURL } from 'node:url';

export function configurar(apiUrl) {
  if (!apiUrl) throw new Error('Informe API_PUBLIC_URL com a URL HTTPS do backend Java publicado.');
  const url = new URL(apiUrl);
  if (url.protocol !== 'https:' || url.username || url.password || url.search || url.hash || url.pathname !== '/') {
    throw new Error('API_PUBLIC_URL deve ser uma origem HTTPS sem credenciais, caminho ou parâmetros.');
  }
  const host = url.hostname.toLowerCase();
  if (host === 'localhost' || host.endsWith('.localhost') || host.includes(':') || /^\d+(\.\d+){3}$/.test(host)) {
    throw new Error('Use o domínio público do backend, não localhost ou endereço IP.');
  }
  return {
    $schema: 'https://openapi.vercel.sh/vercel.json',
    framework: 'vite',
    buildCommand: 'npm run build',
    outputDirectory: 'dist',
    rewrites: [
      { source: '/api/:path*', destination: `${url.origin}/:path*` },
      { source: '/((?!api(?:/|$)|assets/).*)', destination: '/index.html' },
    ],
    headers: [{ source: '/api/:path*', headers: [{ key: 'Cache-Control', value: 'no-store' }] }],
  };
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const config = configurar(process.env.API_PUBLIC_URL);
  const destino = new URL('../vercel.json', import.meta.url);
  await writeFile(destino, `${JSON.stringify(config, null, 2)}\n`);
  console.log(`Configuração gerada: ${fileURLToPath(destino)}. API: ${process.env.API_PUBLIC_URL}`);
}
