# FARM EASY

A Unified Digital Platform for Farmer Support, Equipment Sharing & Land Rental.

## Tech Stack

- **Frontend:** React, Bootstrap, react-i18next, Axios, Vite
- **Backend:** Spring Boot 3, Spring Security, JWT, WebSockets, iText 7, Java Mail
- **Database:** MySQL

## Prerequisites

- Node.js 18+
- Java 17+
- Maven 3.8+
- MySQL 8+

## Quick Start

### 1. Database

Create database and run schema (optional; JPA can create tables):

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

Or set in `backend/src/main/resources/application.yml`:

- `spring.datasource.url`: `jdbc:mysql://localhost:3306/frameasy?createDatabaseIfNotExist=true&...`
- `spring.datasource.username` / `password`

Seed users (if you run seed.sql): password for admin and farmer is **password** (BCrypt hash in seed). Change after first login in production.

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

Runs on **http://localhost:8080**. API base path: **/api**.

Configure SMTP in `application.yml` or env for OTP emails:

- `MAIL_USERNAME`, `MAIL_PASSWORD` (e.g. Gmail app password)

Optional: set `JWT_SECRET` in env for production.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on **http://localhost:3000**. Proxy forwards `/api` and `/ws` to the backend.

## Features

- **Landing:** Hero, counters, feature sections, nav (Home, Schemes, Products, Trade, Login/Profile).
- **Auth:** Register (Farmer/Customer), email OTP verification, login with OTP, JWT, role-based redirect.
- **Equipment / Land / Trade:** List, browse with location filter (HTML5 Geolocation), search, category/radius. Contact via in-app chat; Confirm Deal with OTP and legal PDF agreement (iText 7), email to both parties.
- **Government Schemes:** Filter by state, search, cache (24h), scheduler refresh, offline last-fetched. data.gov.in integration configurable via `schemes.api-url` and `schemes.api-key`.
- **Multilingual:** English, Hindi, Marathi (react-i18next); language stored in user profile.
- **AI Farming Assistant:** Floating chat; farming-only responses (soil, crops, weather, etc.).
- **Admin:** Manage users, approve/delete listings, view agreements, refresh scheme cache, stats.

## API Overview

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/auth/register | Register |
| POST | /api/auth/verify-registration | Verify registration OTP |
| POST | /api/auth/login | Login (sends OTP) |
| POST | /api/auth/verify-login | Verify login OTP |
| GET | /api/auth/me | Current user (JWT) |
| GET | /api/equipment/public | List equipment (lat, lon, radiusKm, category) |
| GET | /api/land/public | List land |
| GET | /api/trade/public | List trade/crops |
| GET | /api/schemes/public/list | List schemes (state, search) |
| POST | /api/agreements/send-otp | Send OTP for agreement |
| POST | /api/agreements | Create agreement (OTP + PDF) |
| GET | /api/chat/conversation | Get chat with user |
| POST | /api/chat/send | Send message |
| POST | /api/ai/chat | AI farming assistant |
| GET/PUT | /api/users/profile | Get/update profile |
| GET | /api/admin/* | Admin endpoints (ROLE_ADMIN) |

WebSocket: connect to `/ws` (STOMP), send to `/app/chat/{receiverId}` with `senderId`, `content`, `relatedType`, `relatedId`.

## Project Structure

```
Krushi/
├── backend/           # Spring Boot
│   └── src/main/java/com/frameasy/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── model/
│       ├── dto/
│       ├── security/
│       ├── config/
│       ├── scheduler/
│       ├── websocket/
│       └── util/
├── frontend/          # React + Vite
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/
│       ├── context/
│       ├── hooks/
│       ├── i18n/
│       └── App.jsx
├── database/
│   ├── schema.sql
│   └── seed.sql
└── README.md
```

## Security

- JWT in `Authorization: Bearer <token>`
- BCrypt for passwords
- OTP for registration, login, and agreement signing
- Role-based access (ROLE_FARMER, ROLE_CUSTOMER, ROLE_ADMIN)
- CORS configured for frontend origin

## Deployment

- **Backend:** Build `mvn -DskipTests package`, run `java -jar target/frameasy-backend-1.0.0.jar`. Set env: `SPRING_DATASOURCE_URL`, `JWT_SECRET`, `MAIL_*`.
- **Frontend:** `npm run build`, serve `dist/` and proxy `/api` and `/ws` to backend.
- **Database:** Run schema and optionally seed; ensure `ddl-auto` is `update` or `validate` in production.

---

FARM EASY — Farming Made Easy for Everyone.
