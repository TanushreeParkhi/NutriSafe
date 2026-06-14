from fastapi import Depends, FastAPI, File, Form, HTTPException, UploadFile, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from .database import Base, engine, get_db
from .gemini import generate_text, generate_with_upload
from .models import AiUsage, User
from .rate_limits import enforce_quota
from .schemas import AiProxyRequest, AiProxyResponse, ApiMessage, AuthRequest, AuthResponse
from .security import create_access_token, current_user, hash_password, verify_password

app = FastAPI(title="PrivEat Backend", version="1.0.0")


@app.on_event("startup")
def create_tables() -> None:
    Base.metadata.create_all(bind=engine)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/v1/auth/register", response_model=AuthResponse)
def register(request: AuthRequest, db: Session = Depends(get_db)) -> AuthResponse:
    existing = db.scalar(select(User).where(User.email == request.email.lower()))
    if existing is not None:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Email already registered.")

    user = User(email=request.email.lower(), password_hash=hash_password(request.password))
    db.add(user)
    db.commit()
    db.refresh(user)
    return AuthResponse(email=user.email, accessToken=create_access_token(user))


@app.post("/v1/auth/login", response_model=AuthResponse)
def login(request: AuthRequest, db: Session = Depends(get_db)) -> AuthResponse:
    user = db.scalar(select(User).where(User.email == request.email.lower()))
    if user is None or not verify_password(request.password, user.password_hash):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials.")
    return AuthResponse(email=user.email, accessToken=create_access_token(user))


@app.post("/v1/auth/logout", response_model=ApiMessage)
def logout(_: User = Depends(current_user)) -> ApiMessage:
    return ApiMessage(message="Logged out. Discard token on client.")


@app.delete("/v1/account", response_model=ApiMessage)
def delete_account(user: User = Depends(current_user), db: Session = Depends(get_db)) -> ApiMessage:
    db.query(AiUsage).filter(AiUsage.user_id == user.id).delete()
    db.delete(user)
    db.commit()
    return ApiMessage(message="Account deleted.")


@app.post("/v1/ai/generate", response_model=AiProxyResponse)
async def ai_generate(
    request: AiProxyRequest,
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
) -> AiProxyResponse:
    enforce_quota(db, user, request.task)
    text, model = await generate_text(request.prompt, request.preferredModel, request.task)
    return AiProxyResponse(json=text, model=model)


@app.post("/v1/ai/analyze-meal-image", response_model=AiProxyResponse)
async def analyze_meal_image(
    image: UploadFile = File(...),
    storageContext: str = Form(...),
    preferredModel: str = Form(...),
    prompt: str = Form(...),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
) -> AiProxyResponse:
    enforce_quota(db, user, "food_analysis")
    text, model = await generate_with_upload(
        prompt=prompt,
        preferred_model=preferredModel,
        task="food_analysis",
        upload=image,
        extra_text=storageContext,
    )
    return AiProxyResponse(json=text, model=model)


@app.post("/v1/ai/import-prescription", response_model=AiProxyResponse)
async def import_prescription(
    document: UploadFile = File(...),
    preferredModel: str = Form(...),
    prompt: str = Form(...),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
) -> AiProxyResponse:
    enforce_quota(db, user, "prescription_ocr")
    text, model = await generate_with_upload(
        prompt=prompt,
        preferred_model=preferredModel,
        task="prescription_ocr",
        upload=document,
    )
    return AiProxyResponse(json=text, model=model)
