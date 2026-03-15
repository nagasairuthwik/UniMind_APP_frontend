import os
from pathlib import Path

# Load .env from api_server directory or project root
_env_path = Path(__file__).resolve().parent / ".env"
if _env_path.exists():
    from dotenv import load_dotenv
    load_dotenv(_env_path)
else:
    try:
        from dotenv import load_dotenv
        load_dotenv(Path(__file__).resolve().parent.parent / ".env")
    except Exception:
        pass


class Config:
    # Secret key for session security
    SECRET_KEY = os.environ.get("SECRET_KEY") or "supersecretkey"

    # -----------------------------
    # Gemini AI (Google)
    # -----------------------------
    GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY") or os.environ.get("GEMINI_AI_API_KEY") or ""

    # -----------------------------
    # MySQL Configuration
    # -----------------------------
    MYSQL_HOST = os.environ.get("MYSQL_HOST") or "localhost"
    MYSQL_USER = os.environ.get("MYSQL_USER") or "root"
    MYSQL_PASSWORD = os.environ.get("MYSQL_PASSWORD") or ""
    MYSQL_DB = os.environ.get("MYSQL_DB") or "unimind"
    MYSQL_CURSORCLASS = "DictCursor"

    # -----------------------------
    # SMTP / Email for OTP and notifications
    # -----------------------------
    SMTP_HOST = os.environ.get("SMTP_HOST") or "smtp.gmail.com"
    SMTP_PORT = int(os.environ.get("SMTP_PORT") or 587)
    SMTP_USER = os.environ.get("SMTP_USER") or "unimind.fphl@gmail.com"
    # IMPORTANT: this must be your 16‑character Gmail app password with NO spaces
    SMTP_PASSWORD = os.environ.get("SMTP_APP_PASSWORD") or "jylklxhsjsbndmcp"
    SMTP_SENDER = os.environ.get("UNIMIND_SMTP_SENDER") or (
        f"UniMind <{SMTP_USER}>" if SMTP_USER else "UniMind <no-reply@unimind.local>"
    )

    # Optional (recommended for production later)
    DEBUG = True
