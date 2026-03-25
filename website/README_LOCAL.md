# Run the UniMind website (login / signup work)

## Why login works in the app but not when you “open the HTML file”

If you double‑click `signup.html` or `signin.html`, the address bar shows **`file:///C:/...`**.  
Many browsers **block or fail** `fetch()` from `file://` to `http://localhost:5000`, so you see *Unable to reach UniMind server* even when the API is running.

## Fix: open the site over HTTP

1. **Start MySQL** (XAMPP → Start MySQL).

2. **Start the API** (same as for the Android app):

   ```bat
   cd api_server
   python app.py
   ```

3. **Start the website server** — in the **`website`** folder double‑click:

   **`serve.bat`**

   Or manually:

   ```bat
   cd website
   python -m http.server 8765
   ```

4. In the browser open:

   - **http://127.0.0.1:8765/signup.html**
   - or **http://127.0.0.1:8765/signin.html**

   The address must start with **`http://`**, not **`file://`**.

5. The API URL is **`http://127.0.0.1:5000`** by default (see `auth.js` → `UNIMIND_API_BASE`).  
   If your API runs on another PC/IP, set before `auth.js`:

   ```html
   <script>window.UNIMIND_API_BASE = "http://YOUR_PC_IP:5000";</script>
   ```

## Quick check

Open **http://127.0.0.1:5000/health** — if you see `{"status":"ok"}`, the backend is running.
