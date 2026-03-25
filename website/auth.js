// Simple client-side auth gate for UniMind web.
// Marks a session as "logged in" after signup or signin and protects main pages.

// Default API URL (same PC as browser). Override in any HTML before auth.js:
//   <script>window.UNIMIND_API_BASE = "http://192.168.x.x:5000";</script>
(function () {
  if (typeof window === "undefined") return;
  var b = window.UNIMIND_API_BASE;
  if (!b || String(b).trim() === "") {
    window.UNIMIND_API_BASE = "http://127.0.0.1:5000";
  } else {
    window.UNIMIND_API_BASE = String(b).replace(/\/+$/, "");
  }
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

