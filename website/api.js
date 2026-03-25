/**
 * UniMind web → Flask API (same backend as the Android app for login, etc.)
 *
 * Website AI (/ai/chat, /ai/finance_suggestions, …) runs on the server only.
 * Keys live on the server (GEMINI_API_KEY / GEMINI_API_KEYS): api_server/.env is tried first,
 * then local.properties, gemini_keys.properties, gradle.properties — restart python app.py.
 * Open the site at http://127.0.0.1:5000/web/… (not file://). Never put API keys in this file.
 */
function unimindApiBase() {
  if (typeof window !== "undefined" && window.UNIMIND_API_BASE) {
    return String(window.UNIMIND_API_BASE).replace(/\/+$/, "");
  }
  // Pages served from Flask at /web/... → same origin; use "" so fetch("/ai/...") works.
  if (typeof window !== "undefined" && window.location) {
    var proto = window.location.protocol || "";
    if (proto !== "file:") {
      var path = window.location.pathname || "";
      if (path === "/web" || path.indexOf("/web/") === 0) {
        return "";
      }
    }
  }
  return "http://127.0.0.1:5000";
}
const UNIMIND_FINANCE_KEY = "unimind_finance_state";
const UNIMIND_PRODUCTIVITY_KEY = "unimind_productivity_state";
const UNIMIND_LIFESTYLE_KEY = "unimind_lifestyle_state";

function unimindGetFinanceState() {
  try {
    const raw = localStorage.getItem(UNIMIND_FINANCE_KEY);
    return raw ? JSON.parse(raw) : { monthlySalary: 0, spentToday: 0, spentMonth: 0 };
  } catch {
    return { monthlySalary: 0, spentToday: 0, spentMonth: 0 };
  }
}

function unimindSetFinanceState(state) {
  try {
    localStorage.setItem(UNIMIND_FINANCE_KEY, JSON.stringify(state));
  } catch {
    // ignore
  }
}

async function unimindCallApi(path, body) {
  const base = unimindApiBase();
  let res;
  try {
    res = await fetch(base + path, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body || {}),
    });
  } catch (e) {
    throw new Error(
      "Cannot reach API at " +
        base +
        ". Start the server (python app.py) and set window.UNIMIND_API_BASE if not on this PC."
    );
  }
  const data = await res.json().catch(() => ({}));
  if (!res.ok || !data.success) {
    throw new Error(data.message || "Request failed (" + res.status + ")");
  }
  return data;
}

async function unimindAiChat(prompt, history) {
  const data = await unimindCallApi("/ai/chat", { prompt, history: history || [] });
  return data.reply || "";
}

async function unimindAiFinanceInsight(state) {
  const s = state || unimindGetFinanceState();
  const data = await unimindCallApi("/ai/finance_suggestions", {
    monthly_salary: s.monthlySalary || 0,
    total_spent_today: s.spentToday || 0,
    total_spent_month: s.spentMonth || 0,
  });
  return data.reply || "";
}

function unimindGetProductivityState() {
  try {
    const raw = localStorage.getItem(UNIMIND_PRODUCTIVITY_KEY);
    return raw ? JSON.parse(raw) : { totalTasks: 0, completedToday: 0, upcomingTitles: [] };
  } catch {
    return { totalTasks: 0, completedToday: 0, upcomingTitles: [] };
  }
}

function unimindSetProductivityState(state) {
  try {
    localStorage.setItem(UNIMIND_PRODUCTIVITY_KEY, JSON.stringify(state));
  } catch {
    // ignore
  }
}

async function unimindAiProductivityInsight(state) {
  const s = state || unimindGetProductivityState();
  const data = await unimindCallApi("/ai/productivity_suggestions", {
    total_tasks: s.totalTasks || 0,
    completed_today: s.completedToday || 0,
    upcoming_titles: s.upcomingTitles || [],
  });
  return data.reply || "";
}

function unimindGetLifestyleState() {
  try {
    const raw = localStorage.getItem(UNIMIND_LIFESTYLE_KEY);
    return raw ? JSON.parse(raw) : { sleepHours: 0, stressLevel: 5 };
  } catch {
    return { sleepHours: 0, stressLevel: 5 };
  }
}

function unimindSetLifestyleState(state) {
  try {
    localStorage.setItem(UNIMIND_LIFESTYLE_KEY, JSON.stringify(state));
  } catch {
    // ignore
  }
}

async function unimindAiLifestyleInsight(state) {
  const s = state || unimindGetLifestyleState();
  const data = await unimindCallApi("/ai/lifestyle_suggestions", {
    sleep_hours: s.sleepHours || 0,
    stress_level: s.stressLevel || 5,
  });
  return data.reply || "";
}

