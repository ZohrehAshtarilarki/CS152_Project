# API Contract

Base URL: `http://localhost:8080` (from the Android emulator: `http://10.0.2.2:8080`).

- All bodies are JSON.
- All times are **UTC epoch milliseconds**.
- Errors look like `{"error": "message"}`.

| Who | Method | Path | Auth header | Body → Response |
|---|---|---|---|---|
| Family | POST | `/api/auth/register` | — | `RegisterRequest` → `AuthResponse` |
| Family | POST | `/api/auth/login` | — | `LoginRequest` → `AuthResponse` |
| Family | GET | `/api/trips` | `Bearer <familyToken>` | → `TravelPackage[]` |
| Family | POST | `/api/trips` | `Bearer <familyToken>` | `TripRequest` → `TravelPackage` |
| Family | GET | `/api/trips/{tripId}` | `Bearer <familyToken>` | → `TravelPackage` |
| Family | PUT | `/api/trips/{tripId}` | `Bearer <familyToken>` | `TripRequest` → `TravelPackage` (replaces the whole trip) |
| Family | POST | `/api/trips/{tripId}/pair` | `Bearer <familyToken>` | `{ "code": "482913" }` → `PairingStatus` |
| Traveler | POST | `/api/pairing/start` | — | → `PairingStart` |
| Traveler | GET | `/api/pairing/{pairingId}` | — | → `PairingStatus` |
| Traveler | GET | `/api/traveler/package` | `Bearer <deviceToken>` | → `TravelPackage` |

## Shapes

**AuthResponse**
```json
{ "token": "x8Fk...", "familyId": "6f1c...", "name": "Demo Family" }
```

**TravelPackage** (the shared contract: the traveler phone saves exactly this)
```json
{
  "tripId": "a1b2...",
  "travelerName": "Ayşe Yılmaz",
  "targetLanguage": "tr",
  "lastUpdatedUtc": 1793521800000,
  "legs": [
    {
      "sequenceOrder": 1, "flightNumber": "TK008",
      "originAirport": "SFO", "destinationAirport": "IST",
      "departureTimeUtc": 1793556600000, "arrivalTimeUtc": 1793625900000,
      "terminal": "I", "gate": "G101", "status": "SCHEDULED"
    }
  ],
  "emergencyContacts": [
    { "name": "Demo Caregiver", "relation": "Family / Caregiver", "phoneNumber": "+1-408-555-0199" }
  ],
  "staffCards": [
    {
      "id": "c3d4...", "category": "TRANSFER",
      "titleEn": "Help Finding Connection Gate", "titleTr": "Aktarma Kapısını Bulma Yardımı",
      "messageEn": "I do not speak English well. Please guide me to Gate {GATE} for flight {FLIGHT}.",
      "messageTr": "İngilizce iyi konuşamıyorum. Lütfen {FLIGHT} sefer sayılı uçuşum için {GATE} numaralı kapıya gitmeme yardım edin."
    }
  ]
}
```

**TripRequest**: same as TravelPackage, but without `tripId` and `lastUpdatedUtc`.

**PairingStart**
```json
{ "pairingId": "secret-uuid", "code": "482913", "expiresAtUtc": 1793522400000 }
```

**PairingStatus**: `status` is `WAITING`, `CONFIRMED` or `EXPIRED`. `deviceToken` is sent **only** to the traveler phone.
```json
{ "status": "CONFIRMED", "deviceToken": "Qm9...", "tripId": "a1b2..." }
```

## Try it with curl (Family side, when you only have one emulator)

```bash
# 1. Sign in and copy the "token"
curl -s -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@cs152.dev","password":"demo1234"}'

# 2. List trips and copy the "tripId"
curl -s localhost:8080/api/trips -H "Authorization: Bearer PASTE_TOKEN"

# 3. Pair with the code shown on the traveler emulator
curl -s -X POST localhost:8080/api/trips/PASTE_TRIP_ID/pair \
  -H "Authorization: Bearer PASTE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"482913"}'
```

## Changing the contract

1. Agree with your teammate first.
2. Change **both** `backend/.../view/TripViews.java` and `android/.../model/TravelPackage.kt` (and `ApiService.kt` / `ApiModels.kt` if needed).
3. Update this file in the same pull request.
