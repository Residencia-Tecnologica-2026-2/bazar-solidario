import { spawnSync } from "node:child_process";
const env = { ...process.env, VITE_API_BASE_URL: "https://isac-brecho.vercel.app/api" };
for (const args of [["node_modules/typescript/bin/tsc", "-b"], ["node_modules/vite/bin/vite.js", "build"], ["node_modules/@capacitor/cli/bin/capacitor", "sync", "android"]]) {
  const result = spawnSync(process.execPath, args, { env, stdio: "inherit" });
  if (result.status !== 0) process.exit(result.status ?? 1);
}
