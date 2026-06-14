# Play Store Release Checklist

## Build

- Create a release signing key and store it outside Git.
- Generate a signed Android App Bundle.
- Verify release build installs on a physical phone.
- Verify R8/minified release does not crash on startup.

## Store Listing

- App name: PrivEat.
- Short description focused on food freshness, spoilage risk, leftovers, allergens, and safety score.
- Add phone screenshots for Auth, Dashboard, Meal Tracker, Safety Details, Insights, Privacy, and AI Planner.
- Add feature graphic and adaptive icon.

## Policy

- Publish privacy policy URL.
- Publish terms URL.
- Add medical and food safety disclaimer.
- Complete Data Safety form accurately.
- Declare camera, notification, and internet use.
- Do not claim medical diagnosis or treatment.

## Manual Test Pass

- Sign in and sign up.
- Delete account.
- Wipe vault.
- Capture meal photo.
- Pick gallery image.
- Import prescription image/PDF.
- Add/delete allergy.
- Generate local plan with cloud disabled.
- Verify cloud fallback when backend is unavailable.
- Verify leftover reminder notification.
- Verify offline mode.
