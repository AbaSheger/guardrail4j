import { chromium, request as playwrightRequest } from '@playwright/test';
import { mkdir } from 'node:fs/promises';
import { resolve } from 'node:path';

const baseUrl = process.env.GUARDRAIL4J_DEMO_URL ?? 'http://localhost:8080';
const screenshotPath = resolve('docs/demo-summary.png');
const summarizePath = '/api/summarize';
const summaryPath = '/guardrail4j/usage/summary';
const summarizeRequest = {
  text: 'Test document',
  userId: 'alice',
  tenantId: 'acme'
};

const api = await playwrightRequest.newContext({
  baseURL: baseUrl,
  extraHTTPHeaders: {
    'Content-Type': 'application/json'
  }
});

try {
  const summarizeResponse = await api.post(summarizePath, {
    data: summarizeRequest
  });
  const summarizeBody = await responseJson(summarizeResponse, summarizePath);

  const usageSummaryResponse = await api.get(summaryPath);
  const usageSummaryBody = await responseJson(usageSummaryResponse, summaryPath);

  await mkdir('docs', { recursive: true });

  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1200, height: 900 } });

  await page.setContent(renderDemoPage({
    baseUrl,
    summarizePath,
    summaryPath,
    summarizeRequest,
    summarizeBody,
    usageSummaryBody
  }));
  await page.screenshot({ path: screenshotPath, fullPage: true });
  await browser.close();

  console.log(`Saved demo screenshot to ${screenshotPath}`);
} finally {
  await api.dispose();
}

async function responseJson(response, path) {
  const bodyText = await response.text();

  if (!response.ok()) {
    throw new Error(
      `Request to ${path} failed with HTTP ${response.status()}: ${bodyText}`
    );
  }

  try {
    return JSON.parse(bodyText);
  } catch (error) {
    throw new Error(`Request to ${path} did not return JSON: ${bodyText}`, {
      cause: error
    });
  }
}

function renderDemoPage({
  baseUrl,
  summarizePath,
  summaryPath,
  summarizeRequest,
  summarizeBody,
  usageSummaryBody
}) {
  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Guardrail4J Demo Summary</title>
  <style>
    :root {
      color-scheme: light;
      --background: #f7f9fb;
      --surface: #ffffff;
      --border: #d7dee8;
      --text: #17202a;
      --muted: #5c6b7a;
      --accent: #2563eb;
      --success: #047857;
    }

    * {
      box-sizing: border-box;
    }

    body {
      margin: 0;
      background: var(--background);
      color: var(--text);
      font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
    }

    main {
      width: 1100px;
      margin: 0 auto;
      padding: 44px 0 52px;
    }

    header {
      margin-bottom: 28px;
    }

    h1 {
      margin: 0 0 8px;
      font-size: 34px;
      font-weight: 750;
      letter-spacing: 0;
    }

    p {
      margin: 0;
      color: var(--muted);
      font-size: 16px;
      line-height: 1.5;
    }

    .endpoint {
      color: var(--accent);
      font-weight: 700;
    }

    .grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 18px;
      align-items: start;
    }

    .panel {
      border: 1px solid var(--border);
      border-radius: 8px;
      background: var(--surface);
      overflow: hidden;
    }

    .panel.wide {
      grid-column: 1 / -1;
    }

    .panel-title {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      padding: 16px 18px;
      border-bottom: 1px solid var(--border);
      font-size: 14px;
      font-weight: 750;
    }

    .method {
      color: var(--success);
      font-size: 12px;
      font-weight: 800;
      letter-spacing: 0.08em;
      text-transform: uppercase;
    }

    pre {
      margin: 0;
      padding: 18px;
      overflow: auto;
      color: #111827;
      font-family: "JetBrains Mono", "SFMono-Regular", Consolas, monospace;
      font-size: 13px;
      line-height: 1.55;
      white-space: pre-wrap;
      word-break: break-word;
    }
  </style>
</head>
<body>
  <main>
    <header>
      <h1>Guardrail4J Demo Flow</h1>
      <p>
        Captured from <span class="endpoint">${escapeHtml(baseUrl)}</span>:
        one guarded summary call followed by the usage summary endpoint.
      </p>
    </header>

    <section class="grid">
      <article class="panel">
        <div class="panel-title">
          <span>Summarize Request</span>
          <span class="method">POST ${escapeHtml(summarizePath)}</span>
        </div>
        <pre>${escapeHtml(formatJson(summarizeRequest))}</pre>
      </article>

      <article class="panel">
        <div class="panel-title">
          <span>Fake Summary Response</span>
          <span class="method">200 OK</span>
        </div>
        <pre>${escapeHtml(formatJson(summarizeBody))}</pre>
      </article>

      <article class="panel wide">
        <div class="panel-title">
          <span>Usage Summary</span>
          <span class="method">GET ${escapeHtml(summaryPath)}</span>
        </div>
        <pre>${escapeHtml(formatJson(usageSummaryBody))}</pre>
      </article>
    </section>
  </main>
</body>
</html>`;
}

function formatJson(value) {
  return JSON.stringify(value, null, 2);
}

function escapeHtml(value) {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}
