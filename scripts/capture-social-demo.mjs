import { chromium } from '@playwright/test';
import { mkdir, rm } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { spawn } from 'node:child_process';

const __dirname = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(__dirname, '..');
const htmlPath = resolve(repoRoot, 'docs/social-demo/index.html');
const outputPath = resolve(repoRoot, 'docs/social-demo/guardrail4j-demo.mp4');
const framesDir = resolve(repoRoot, 'docs/social-demo/.frames');
const durationMs = 9000;
const fps = 30;
const width = 1080;
const height = 1080;
const frameCount = Math.round((durationMs / 1000) * fps);

if (!existsSync(htmlPath)) {
  throw new Error(`Demo page not found: ${htmlPath}`);
}

await mkdir(framesDir, { recursive: true });

const browser = await chromium.launch();
const page = await browser.newPage({
  viewport: { width, height },
  deviceScaleFactor: 1
});

try {
  await page.goto(pathToFileURL(htmlPath).href, { waitUntil: 'networkidle' });

  for (let index = 0; index < frameCount; index += 1) {
    const ms = Math.round((index / fps) * 1000);
    await page.evaluate((time) => window.__setDemoTime(time), ms);
    await page.screenshot({
      path: resolve(framesDir, `frame-${String(index).padStart(4, '0')}.png`)
    });
  }
} finally {
  await browser.close();
}

const ffmpegArgs = [
  '-y',
  '-framerate', String(fps),
  '-i', resolve(framesDir, 'frame-%04d.png'),
  '-c:v', 'libx264',
  '-pix_fmt', 'yuv420p',
  '-movflags', '+faststart',
  outputPath
];

try {
  await run('ffmpeg', ffmpegArgs);
  console.log(`Saved ${outputPath}`);
} catch (error) {
  console.warn('Could not create MP4 because ffmpeg is unavailable or failed.');
  console.warn(error.message);
  process.exitCode = 1;
} finally {
  await removeFramesDir();
}

async function run(command, args) {
  return new Promise((resolveRun, rejectRun) => {
    const child = spawn(command, args, { stdio: 'inherit' });
    child.on('error', rejectRun);
    child.on('close', (code) => {
      if (code === 0) {
        resolveRun();
        return;
      }
      rejectRun(new Error(`${command} exited with code ${code}`));
    });
  });
}

async function removeFramesDir() {
  const resolvedRepo = resolve(repoRoot);
  const resolvedFrames = resolve(framesDir);
  if (!resolvedFrames.startsWith(resolvedRepo)) {
    throw new Error(`Refusing to remove frames outside repository: ${resolvedFrames}`);
  }
  await rm(resolvedFrames, { recursive: true, force: true });
}
