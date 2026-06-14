# PrivEat Android App

PrivEat is a portrait-first, local-first nutrition and biosafety tracker built with Kotlin, Jetpack Compose, MVVM, Room, DataStore, Retrofit/OkHttp, Coil, gallery image picking, WorkManager reminders, and a Compose chart.

## Open in Android Studio

1. Open Android Studio.
2. Choose **Open** and select `D:\TANU\priveat`.
3. Let Android Studio sync Gradle.
4. Run the `app` configuration on an emulator or Android phone.

Default local demo login:

- Email: `admin@priveat.com`
- Password: `password123`

## Gemini Setup

The app works without a Gemini key by using `GeminiRepository` mock fallback responses.

For a demo key, set this Gradle property before syncing:

```properties
GEMINI_API_KEY=your_key_here
GEMINI_MODEL=gemini-1.5-flash
```

The API key is read through `BuildConfig` and isolated inside `GeminiRepository`. The source includes the required TODO: production must call a backend proxy. Do not ship production Android clients with a direct Gemini API key.

## Privacy Model

- Meals, safety reports, health records, diet vault choices, chat messages, and leftover timers are stored locally in Room/DataStore.
- Android cloud backup is disabled for app databases/preferences through `data_extraction_rules.xml` and manifest backup flags.
- Raw meal/chat images are not stored by default. Users can enable local-only retention.
- Sensitive blur mode hides kcal, macros, and health-adjacent values until tapped or long-pressed.
- Wipe Vault deletes Room and DataStore data locally.

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

- Hybrid Gemini extraction plus deterministic Kotlin rule engine
- Food recognition mock/Gemini service layer for calories and macros
- Freshness/spoilage risk categories
- Improper storage detection from user inputs
- Microbial growth estimator
- Allergen alerts from local allergy profile
- Disease suitability warnings
- Ultra-processed classification
- Safety score and final action
- Leftover timers with local notifications
- Weekly food risk report
