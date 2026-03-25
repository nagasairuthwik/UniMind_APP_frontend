// Simple client-side auth gate for UniMind web.
// Marks a session as "logged in" after signup or signin and protects main pages.

// API base for login/signup/fetch. Override in any HTML before auth.js if needed.
// When pages are served at http://127.0.0.1:5000/web/... (python app.py), use ""
// so requests go to the same origin (avoids CORS and file:// issues).
(function () {
  if (typeof window === "undefined") return;
  var b = window.UNIMIND_API_BASE;
  if (b != null && String(b).trim() !== "") {
    window.UNIMIND_API_BASE = String(b).replace(/\/+$/, "");
    return;
  }
  if (window.location && window.location.protocol !== "file:") {
    var path = window.location.pathname || "";
    if (path === "/web" || path.indexOf("/web/") === 0) {
      window.UNIMIND_API_BASE = "";
      return;
    }
  }
  window.UNIMIND_API_BASE = "http://127.0.0.1:5000";
})();

const UNIMIND_AUTH_KEY = "unimind_is_logged_in";
const UNIMIND_USER_ID_KEY = "unimind_user_id";

function unimindMarkLoggedIn(userId) {
  try {
    localStorage.setItem(UNIMIND_AUTH_KEY, "true");
    if (userId != null) {
      localStorage.setItem(UNIMIND_USER_ID_KEY, String(userId));
    }
  } catch (e) {
    // ignore
  }
}

function unimindClearAllData() {
  try {
    localStorage.removeItem(UNIMIND_AUTH_KEY);
    localStorage.removeItem(UNIMIND_USER_ID_KEY);
  } catch (e) {
    // ignore
  }
}

function unimindIsLoggedIn() {
  try {
    return localStorage.getItem(UNIMIND_AUTH_KEY) === "true";
  } catch (e) {
    return false;
  }
}

function unimindGetUserId() {
  try {
    const raw = localStorage.getItem(UNIMIND_USER_ID_KEY);
    return raw ? parseInt(raw, 10) : null;
  } catch (e) {
    return null;
  }
}

function unimindRequireAuthWithPrompt(redirectTo) {
  var target = redirectTo || "signup.html";
  if (!unimindIsLoggedIn()) {
    alert("Please sign up or log in to continue.");
    window.location.href = target;
    return false;
  }
  return true;
}

function unimindRequireAuthOnLoad(redirectTo) {
  var target = redirectTo || "signup.html";
  if (!unimindIsLoggedIn()) {
    window.location.href = target;
  }
}

