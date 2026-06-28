# NutriSafe Android App

NutriSafe is a portrait-first, local-first food safety and biosafety tracker built with Kotlin, Jetpack Compose, MVVM, Room, DataStore, Retrofit/OkHttp, Coil, camera/gallery picking, document upload, WorkManager reminders, and Compose charts.

## Open in Android Studio

1. Open Android Studio.
2. Choose **Open** and select `D:\TANU\priveat`.
3. Let Android Studio sync Gradle.
4. Run the `app` configuration on an emulator or Android phone.

Default local demo login:

- Email: `admin@nutrisafe.com`
- Password: `password123`

Accounts and sessions are stored only on the device. No server is required.

## Gemini Setup

Add the key to the untracked `local.properties` file:

```properties
GEMINI_API_KEY=your_key_here
```

NutriSafe calls Gemini directly for meal images, meal descriptions, prescription extraction, diet plans, and expert chat. If the key is missing, the network fails, or Gemini returns a quota error, the deterministic local food-safety rules remain available.

The key is compiled into the APK and can be extracted by a determined user. This standalone configuration is intended for private or test distribution, not a public store release using a shared developer key.

## Privacy Model

- Meals, safety reports, health records, diet vault choices, chat messages, and leftover timers are stored locally in Room/DataStore with Android backup disabled.
- Android cloud backup is disabled for app databases/preferences through `data_extraction_rules.xml` and manifest backup flags.
- Raw meal/chat images are not stored by default. Users can enable local-only retention.
- Sensitive blur mode hides kcal, macros, and health-adjacent values until tapped or long-pressed.
- Wipe Vault and Delete Account erase Room and DataStore data from the device and return to local sign-in.

## Implemented Screens

- Auth with zero-cloud guarantee
- Dashboard/statistics with weekly overview and calorie chart
- Meal tracker with gallery scan card, storage inputs, and meal detail card
- Security & Privacy vault
- Ask NutriSafe AI diet plan generator
- Expert Chat setup and private chat UI
- Explore drawer with required routes
- Weekly Insights risk report

## Biosafety/Nutrition Features

- Direct Gemini extraction plus a deterministic Kotlin rule engine
- Food recognition with a local calories/macros fallback
- Freshness/spoilage risk categories
- Improper storage detection from user inputs
- Microbial growth estimator
- Allergen alerts from local allergy profile
- Disease suitability warnings
- Ultra-processed classification
- Safety score and final action
- Leftover timers with local notifications
- Weekly food risk report
