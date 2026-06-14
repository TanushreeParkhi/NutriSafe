# PrivEat Backend

Production backend proxy for PrivEat Android.

## Responsibilities

- Register and authenticate users.
- Store password hashes, not raw passwords.
- Keep `GEMINI_API_KEY` server-side only.
- Apply per-user AI quotas.
- Proxy Gemini Flash/Pro calls.
- Accept meal image and prescription document uploads.
- Delete user backend data on account deletion.

## Local Run

```powershell
cd D:\TANU\priveat\backend
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Then set Android `gradle.properties`:

```properties
PRIVEAT_BACKEND_BASE_URL=http://10.0.2.2:8000/
PRIVEAT_BACKEND_AUTH_ENABLED=true
PRIVEAT_CLOUD_AI_ENABLED=true
```

Use HTTPS in production. The Android release manifest blocks cleartext traffic.
