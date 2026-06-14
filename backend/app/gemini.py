import base64

import httpx
from fastapi import HTTPException, UploadFile, status

from .config import settings


ALLOWED_MODELS = {
    "gemini-3-flash-preview",
    "gemini-3.1-pro-preview",
    "gemini-3.5-flash",
}


def safe_model(preferred_model: str, task: str) -> str:
    if preferred_model in ALLOWED_MODELS:
        return preferred_model
    if task == "diet_plan":
        return settings.gemini_pro_model
    return settings.gemini_flash_model


async def generate_text(prompt: str, preferred_model: str, task: str) -> tuple[str, str]:
    model = safe_model(preferred_model, task)
    body = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {
            "temperature": 0.2,
            "responseFormat": {"text": {"mimeType": "APPLICATION_JSON"}},
        },
    }
    return await _call_gemini(model, body)


async def generate_with_upload(
    prompt: str,
    preferred_model: str,
    task: str,
    upload: UploadFile,
    extra_text: str = "",
) -> tuple[str, str]:
    content = await upload.read()
    if len(content) > settings.max_upload_bytes:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail="Upload exceeds 8 MB.",
        )

    mime_type = upload.content_type or "application/octet-stream"
    if not (mime_type.startswith("image/") or mime_type == "application/pdf"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only images and PDF documents are supported.",
        )

    model = safe_model(preferred_model, task)
    encoded = base64.b64encode(content).decode("ascii")
    full_prompt = f"{prompt}\n\nContext:\n{extra_text}".strip()
    body = {
        "contents": [
            {
                "parts": [
                    {"text": full_prompt},
                    {"inlineData": {"mimeType": mime_type, "data": encoded}},
                ]
            }
        ],
        "generationConfig": {
            "temperature": 0.2,
            "responseFormat": {"text": {"mimeType": "APPLICATION_JSON"}},
        },
    }
    return await _call_gemini(model, body)


async def _call_gemini(model: str, body: dict) -> tuple[str, str]:
    if not settings.gemini_api_key:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Gemini API key is not configured.",
        )

    url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent"
    async with httpx.AsyncClient(timeout=45) as client:
        response = await client.post(
            url,
            headers={"x-goog-api-key": settings.gemini_api_key},
            json=body,
        )
    if response.status_code >= 400:
        raise HTTPException(status_code=response.status_code, detail=response.text[:500])

    payload = response.json()
    text = (
        payload.get("candidates", [{}])[0]
        .get("content", {})
        .get("parts", [{}])[0]
        .get("text", "")
    )
    if not text:
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY, detail="Gemini returned an empty response.")
    return text, model
