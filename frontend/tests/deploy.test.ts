import test from 'node:test';
import assert from 'node:assert/strict';
import { configurar } from '../scripts/configurar-vercel.mjs';

test('API precede fallback SPA e preserva o prefixo apenas no domínio do frontend', () => {
  const config = configurar('https://api.isac.example/');
  assert.deepEqual(config.rewrites[0], { source: '/api/:path*', destination: 'https://api.isac.example/:path*' });
  assert.match(config.rewrites[1].source, /\?!api/);
});

test('recusa configuração ausente, privada, com credenciais ou caminho', () => {
  for (const url of [undefined, 'http://api.example', 'https://localhost', 'https://127.0.0.1', 'https://u:p@api.example', 'https://api.example/api', 'https://api.example/?token=x']) {
    assert.throws(() => configurar(url));
  }
});
