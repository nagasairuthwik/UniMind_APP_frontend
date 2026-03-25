// Warn when pages are opened as file:// — fetch() to the API often fails in that mode.
(function () {
  if (typeof window === "undefined" || window.location.protocol !== "file:") return;
  function showBanner() {
    if (document.getElementById("unimind-file-protocol-banner")) return;
    var d = document.createElement("div");
    d.id = "unimind-file-protocol-banner";
    d.setAttribute("role", "alert");
    d.style.cssText =
      "background:#fbbf24;color:#1f2937;padding:14px 16px;font-size:14px;text-align:center;font-weight:600;z-index:99999;position:relative;";
    d.innerHTML =
      "You opened this page as a <strong>local file</strong> (file://). Sign up / login will usually fail. " +
      "Close this tab, double-click <strong>serve.bat</strong> in this folder, then open " +
      "<strong>http://127.0.0.1:8765/</strong> — see <strong>README_LOCAL.md</strong>.";
    if (document.body) {
      document.body.insertBefore(d, document.body.firstChild);
    }
  }
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", showBanner);
  } else {
    showBanner();
  }
})();
