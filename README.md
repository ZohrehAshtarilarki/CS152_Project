# Offline Safety Companion

An Android app that helps older people travel abroad alone.
A family member sets up the trip remotely, and the traveler gets a very
simple screen that keeps working **without internet**.

CS 152 project · English ↔ Turkish

## Features

- **Family Mode:** log in, create a trip, add flights, contacts, and staff cards
- **Remote pairing:** the traveler's phone shows a 6-digit code; the family types it in
- **Traveler Mode:** four big buttons in Turkish
  - What Should I Do Now?
  - Read a Sign
  - Talk to Airport Staff
  - I Need Help
- **Offline:** the trip is saved on the phone and works in airplane mode
- **Read a Sign:** on-device OCR and translation with Google ML Kit

## Tech stack

| Part | Technology |
| --- | --- |
| Android app | Kotlin, Jetpack Compose |
| Phone database | Room (SQLite) |
| On-device ML | ML Kit Text Recognition, ML Kit Translation |
| Backend | Java, Spring Boot (REST API) |
| Server database | PostgreSQL |

## Folder structure

```
android/   Android app (Family Mode + Traveler Mode)
backend/   Spring Boot REST API
docs/      Project report
```

## How to run

### Backend
1. Install Java (version TBD) and PostgreSQL.
2. Create a database named `safety_companion`.
3. Run:
```bash
   cd backend
   ./mvnw spring-boot:run
```

### Android app
1. Open the `android/` folder in Android Studio.
2. Start an emulator.
3. Click **Run**.

## Team

- Zohreh Ashtarilarki
- Varun Ranjan

## Docs

See [`docs/Project_Report_CS152.md`](docs/Project_Report_CS152.md).
