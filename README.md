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
| Backend | Java 17, Spring Boot 3 (REST API) |
| Server database | PostgreSQL |
| Design pattern | Model–View–Controller (MVC) |

## Folder structure

```
android/   Android app (Family Mode + Traveler Mode)   → open this folder in Android Studio
backend/   Spring Boot REST API
docs/      Architecture, API contract, project report
```

## How to run

```bash
# Database (Mac, one time)
brew install postgresql@16
brew services start postgresql@16
createdb safety_companion

# Backend
cd backend
DB_USER=$(whoami) DB_PASSWORD= ./mvnw spring-boot:run
```

Then open the `android/` folder in Android Studio and click **Run** on an emulator.

Demo family login: **demo@cs152.dev / demo1234**

Full steps: [`docs/architecture.md`](docs/architecture.md#how-to-run)

## Team

- Zohreh Ashtarilarki
- Varun Ranjan

## Docs

- [`docs/architecture.md`](docs/architecture.md): MVC design, flows, who owns which files
- [`docs/api.md`](docs/api.md): API contract (endpoints and JSON)
- [`docs/Project_Report_CS152.md`](docs/Project_Report_CS152.md): project report
