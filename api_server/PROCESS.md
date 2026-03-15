# Process to store signup data

## Why "Site can't be reached" on mobile with 127.0.0.1

**127.0.0.1** (or **localhost**) means "this device itself". So:

- On your **PC**: `http://127.0.0.1:5000` → your PC talks to the server on the same PC. Works.
- On your **phone**: `http://127.0.0.1:5000` → your phone looks for a server **on the phone**. There is no server there, so "site can't be reached".

**Fix:** From the phone (and in the Android app), use your **computer’s IP address** instead of 127.0.0.1, e.g. **http://10.13.245.106:5000** (see step 2 below).

---

## 1. Start the server (on your computer)

From a terminal, in the **api_server** folder:

```bash
cd api_server
python app.py
```

You should see:

- `[UniMind] Database: ...\api_server\unimind.db`
- `[UniMind] Server: http://0.0.0.0:5000`

Leave this terminal open. The app will send signup/login to this server.

## 2. Find your computer’s IP and use it on the phone

- **On Windows (PC):** Open Command Prompt or PowerShell and run:
  ```bash
  ipconfig
  ```
  Find **IPv4 Address** under your Wi‑Fi adapter (e.g. `10.13.245.106`).
- **On the phone:** Use that IP in the browser and in the app, e.g.:
  - In **mobile browser:** `http://10.13.245.106:5000/users` (not 127.0.0.1).
  - In **Android app:** `ApiClient.kt` must have `BASE_URL = "http://10.13.245.106:5000/"` (same IP).

**Phone and computer must be on the same Wi‑Fi.** If the phone still can’t reach the server, allow port 5000 through Windows Firewall (see below).

## 3. Point the app at this server

In the Android project, `ApiClient.kt` must use your computer’s IP and port 5000, for example:

- `http://10.13.245.106:5000/`

So the app sends:

- **Signup:** `POST http://10.13.245.106:5000/signup`  
  Body (JSON): `{"full_name":"...", "email":"...", "password":"..."}`

- **Login:** `POST http://10.13.245.106:5000/login`  
  Body (JSON): `{"email":"...", "password":"..."}`

## 4. Sign up from the app

- Open the app → Create Account.
- Fill Full Name, Email, Password, Confirm Password → Continue.

On the server terminal you should see:

- `[UniMind] Signup saved: id=1 email=...`

That line means the row was **written and committed** to the database.

## 5. Confirm data is stored

**Option A – Browser**

- On the same machine as the server (or from the phone if you use your IP):
  - `http://10.13.245.106:5000/users`
- You should see JSON with a `users` array containing the signup you just did.

**Option B – SQLite file**

- Open the file path printed at startup:  
  `...\UniMind\api_server\unimind.db`
- Use [DB Browser for SQLite](https://sqlitebrowser.org/) or any SQLite client.
- Open the `users` table and run: `SELECT * FROM users;`

## If data still doesn’t appear

1. **Only one server**  
   Stop any other process using port 5000. Only run `python app.py` from `api_server`.

2. **Same server the app uses**  
   The app must call the same machine where you ran `python app.py`. Check `ApiClient.kt` → `BASE_URL` (e.g. `http://10.13.245.106:5000/`).

3. **Check server log**  
   After tapping Continue on signup, you must see:  
   `[UniMind] Signup saved: id=... email=...`  
   If you don’t, the request is not reaching this server (wrong IP/port or another server is running).

4. **Check `/users`**  
   Use your PC's IP (e.g. `http://10.13.245.106:5000/users`), not 127.0.0.1, when opening from the phone. If the list is empty after signup, the app may be hitting a different server.

---

## Allow port 5000 through Windows Firewall (if phone can't reach server)

1. Open **Windows Defender Firewall** → **Advanced settings**.
2. **Inbound Rules** → **New Rule** → **Port** → Next → **TCP**, **Specific local ports:** `5000` → Next.
3. Allow the connection → Next → Name e.g. "Flask 5000" → Finish.

Then on the phone try: `http://<your-PC-IP>:5000/users` (IP from `ipconfig`, not 127.0.0.1).

---

4. **Check `/users` (original)**  
   Open `http://10.13.245.106:5000/users` in a browser right after signup. If the list is empty, the request that returned “Signup successful” in the app is going to a different backend.
