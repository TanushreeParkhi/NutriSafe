from pydantic import BaseModel, EmailStr, Field


class AuthRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=8, max_length=128)


class AuthResponse(BaseModel):
    email: EmailStr
    accessToken: str
    tokenType: str = "bearer"


class ApiMessage(BaseModel):
    message: str


class AiProxyRequest(BaseModel):
    task: str
    preferredModel: str
    prompt: str = Field(min_length=1, max_length=12000)


class AiProxyResponse(BaseModel):
    json: str | None = None
    text: str | None = None
    model: str | None = None
    provider: str = "gemini"
    usedFallback: bool = False
