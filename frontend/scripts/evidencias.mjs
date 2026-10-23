import { spawnSync } from "node:child_process";
import { mkdirSync, writeFileSync, readdirSync } from "node:fs";
import { fileURLToPath } from "node:url";
import path from "node:path";

const frontend = fileURLToPath(new URL("../", import.meta.url));
const output = path.resolve(frontend, "docs/evidencias");
mkdirSync(output, { recursive: true });
const files = readdirSync(path.join(frontend, "tests")).filter(f => f.endsWith(".test.ts")).map(f => `tests/${f}`);
const result = spawnSync(process.execPath, ["--experimental-strip-types", "--test", "--test-reporter=tap", ...files], { cwd: frontend, encoding: "utf8" });
if (result.error) throw result.error;
const log = (result.stdout ?? "") + (result.stderr ?? "");
writeFileSync(path.join(output, "testes.tap"), log);
const summary = { executedAt: new Date().toISOString(), node: process.version, exitCode: result.status,
  tests: Number(log.match(/# tests (\d+)/)?.[1] ?? 0),
  passed: Number(log.match(/# pass (\d+)/)?.[1] ?? 0),
  failed: Number(log.match(/# fail (\d+)/)?.[1] ?? 0),
  suites: Number(log.match(/# suites (\d+)/)?.[1] ?? 0) };
writeFileSync(path.join(output, "resultado.json"), JSON.stringify(summary, null, 2));
const escape = value => String(value).replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c]));
writeFileSync(path.join(output, "relatorio.html"), `<!doctype html><html lang="pt-BR"><meta charset="utf-8"><title>PI3 — Evidências dos testes</title>
<style>body{font:17px/1.6 system-ui;margin:48px auto;max-width:1050px;padding:0 32px;color:#183e39;background:#fafaf7}h1{font-size:38px;line-height:1.2}.cards{display:flex;gap:24px;margin:30px 0}.cards div{background:#e7f1ec;padding:24px;flex:1;border:1px solid #b9d2c6}strong{display:block;font-size:40px}pre{background:#fff;border:1px solid #ccd8d1;padding:24px;font:13px/1.5 monospace;white-space:pre-wrap}small{color:#4e615b}</style>
<p>PROJETO INTEGRADOR 3 / BAZAR SOLIDÁRIO</p><h1>Testes unitários do website</h1><p>Execução real do Node.js Test Runner. Cadastro, catálogo e sacola integrados à interface React.</p>
<div class="cards"><div><strong>${summary.tests}</strong>testes executados</div><div><strong>${summary.passed}</strong>aprovados</div><div><strong>${summary.failed}</strong>falhas</div><div><strong>${summary.suites}</strong>suítes de classes</div></div>
<p>UsuarioService · CatalogoService · SacolaService</p><p><small>${escape(summary.executedAt)} · Node ${escape(summary.node)} · código de saída ${summary.exitCode}</small></p>
<p>API de cadastro simulada nos testes unitários. Este relatório não comprova persistência no PostgreSQL nem autenticação.</p><h2>Saída completa da execução</h2><pre>${escape(log)}</pre></html>`);
console.log(JSON.stringify(summary, null, 2));
process.exitCode = summary.tests > 0 ? result.status ?? 1 : 1;
