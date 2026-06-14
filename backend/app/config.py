from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    database_url: str = "sqlite:///./priveat_backend.db"
    jwt_secret: str = "replace-this-before-production"
    jwt_algorithm: str = "HS256"
    access_token_minutes: int = 60 * 24 * 14
    gemini_api_key: str = ""
    gemini_flash_model: str = "gemini-3-flash-preview"
    gemini_pro_model: str = "gemini-3.1-pro-preview"
    daily_food_analysis_limit: int = 3
    daily_prescription_ocr_limit: int = 1
    daily_diet_plan_limit: int = 1
    daily_expert_chat_limit: int = 20
    max_upload_bytes: int = 8 * 1024 * 1024

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")


settings = Settings()
