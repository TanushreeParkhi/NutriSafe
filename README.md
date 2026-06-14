# PrivEat Android App

PrivEat is a portrait-first, local-first food safety and biosafety tracker built with Kotlin, Jetpack Compose, MVVM, encrypted Room, DataStore, Retrofit/OkHttp, Coil, camera/gallery picking, document upload, WorkManager reminders, and Compose charts.

## Open in Android Studio

1. Open Android Studio.
2. Choose **Open** and select `D:\TANU\priveat`.
3. Let Android Studio sync Gradle.
4. Run the `app` configuration on an emulator or Android phone.

Default local demo login:

- Email: `admin@priveat.com`
- Password: `password123`

For backend auth, run the backend in `backend/` and set:

```properties
PRIVEAT_BACKEND_BASE_URL=https://api.yourdomain.com/
PRIVEAT_BACKEND_AUTH_ENABLED=true
```

## Production AI Setup

PrivEat is production-configured so Gemini keys never live in the Android app.
Cloud AI is optional and disabled by default; local safety rules keep the app usable offline.

For production, deploy a backend proxy and set:

```properties
PRIVEAT_BACKEND_BASE_URL=https://api.yourdomain.com/
PRIVEAT_BACKEND_AUTH_ENABLED=true
PRIVEAT_CLOUD_AI_ENABLED=true
GEMINI_FLASH_MODEL=gemini-3-flash-preview
GEMINI_PRO_MODEL=gemini-3.1-pro-preview
```

The backend stores `GEMINI_API_KEY`, applies per-user quotas, calls Gemini, and returns strict JSON to the app.
Do not ship production Android clients with a direct Gemini API key.

See `backend/README.md` and `docs/PRODUCTION_READINESS.md` for the backend contract and release checklist.

## Privacy Model

- Meals, safety reports, health records, diet vault choices, chat messages, and leftover timers are stored locally in encrypted Room/DataStore.
- Android cloud backup is disabled for app databases/preferences through `data_extraction_rules.xml` and manifest backup flags.
- Raw meal/chat images are not stored by default. Users can enable local-only retention.
- Sensitive blur mode hides kcal, macros, and health-adjacent values until tapped or long-pressed.
- Wipe Vault deletes Room and DataStore data locally. When backend auth is enabled, Delete Account also calls the backend account deletion endpoint.

## Implemented Screens

- Auth with zero-cloud guarantee
- Dashboard/statistics with weekly overview and calorie chart
- Meal tracker with gallery scan card, storage inputs, and meal detail card
- Security & Privacy vault
- Ask PrivEat AI diet plan generator
- Expert Chat setup and private chat UI
- Explore drawer with required routes
- Weekly Insights risk report

## Biosafety/Nutrition Features

- Hybrid backend Gemini extraction plus deterministic Kotlin rule engine
- Food recognition fallback/backend service layer for calories and macros
- Freshness/spoilage risk categories
- Improper storage detection from user inputs
- Microbial growth estimator
- Allergen alerts from local allergy profile
- Disease suitability warnings
- Ultra-processed classification
- Safety score and final action
- Leftover timers with local notifications
- Weekly food risk report
