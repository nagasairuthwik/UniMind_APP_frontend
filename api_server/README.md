# UniMind API Server

Backend for the UniMind Android app. Use your machine IP in the app's `ApiClient.kt` (e.g. `http://10.13.245.106:5000/`).

## Setup

```bash
cd api_server
pip install -r requirements.txt
```

### Gemini AI (optional)

For step insights, domain recommendations, and progress analysis:

1. Copy `.env.example` to `.env` in `api_server/`.
2. Set your Google Gemini API key in `.env`:  
   `GEMINI_API_KEY=your_key_here`  
   (Get a key at [Google AI Studio](https://aistudio.google.com/apikey).)
3. Restart the server. If the key is missing, AI endpoints return fallback messages.

## Run

```bash
python app.py
```

Or with Flask CLI:

```bash
export FLASK_APP=app.py
flask run --host=0.0.0.0 --port=5000
```

Server will be at `http://0.0.0.0:5000` (reachable from your phone at `http://<your-ip>:5000`).

## Endpoints

| Method | Path    | Body / params         | Description        |
|--------|---------|------------------------|--------------------|
| POST   | /signup | JSON: full_name, email, password | Create account (saved to DB) |
| POST   | /login  | JSON: email, password  | Sign in            |
| GET    | /users  | -                      | List all users (from DB)     |
| GET    | /health | -                      | Health check       |

### AI (Gemini)

| Method | Path | Body / params | Description |
|--------|------|----------------|-------------|
| POST   | /ai/steps/insight | user_id (optional), steps_today, steps_goal, last_7_days (optional) | Get short AI tip for step count |
| POST   | /ai/recommendations | user_id, domain_indices (0–3), goals/full_name optional | Recommendations per domain (Health, Productivity, Finance, Lifestyle) |
| POST   | /ai/progress/insight | domain_index, summary, recent_metrics optional | Progress insight for one domain |
| POST   | /steps | user_id, date (optional), steps, goal (optional) | Save daily steps |
| GET    | /steps | user_id, limit (optional) | List user's steps |
| POST   | /progress | user_id, domain_index, date (optional), summary, metrics (optional) | Save domain progress entry |
| GET    | /progress | user_id, domain_index (optional), limit (optional) | List progress entries |

## Database

Signup data is stored in **SQLite**: `api_server/unimind.db`

- Table: `users` (id, full_name, email, password, created_at)
- Created automatically on first run.
- To view data: open `unimind.db` with [DB Browser for SQLite](https://sqlitebrowser.org/) or call `GET http://localhost:5000/users`.
