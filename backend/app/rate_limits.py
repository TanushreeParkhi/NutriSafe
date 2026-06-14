from datetime import datetime, timezone

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from .config import settings
from .models import AiUsage, User, now_utc


LIMITS_BY_TASK = {
    "food_analysis": lambda: settings.daily_food_analysis_limit,
    "prescription_ocr": lambda: settings.daily_prescription_ocr_limit,
    "diet_plan": lambda: settings.daily_diet_plan_limit,
    "expert_chat": lambda: settings.daily_expert_chat_limit,
}


def enforce_quota(db: Session, user: User, task: str) -> None:
    limit_factory = LIMITS_BY_TASK.get(task)
    if limit_factory is None:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Unknown AI task.")

    limit = limit_factory()
    day = datetime.now(timezone.utc).date().isoformat()
    usage = db.scalar(
        select(AiUsage).where(
            AiUsage.user_id == user.id,
            AiUsage.task == task,
            AiUsage.day == day,
        )
    )
    if usage is None:
        usage = AiUsage(user_id=user.id, task=task, day=day, count=0)
        db.add(usage)

    if usage.count >= limit:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail=f"Daily {task} quota reached. PrivEat should use local fallback.",
        )

    usage.count += 1
    usage.updated_at = now_utc()
    db.commit()
