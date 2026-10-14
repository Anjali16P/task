# Feedback Management System

A complete feedback management application with:

- **Frontend:** React (Vite) + HTML/CSS/JavaScript
- **Backend:** Java 17 + Spring Boot 3, Spring Security (JWT), Spring Data JPA
- **Database:** MySQL 8
- **Auth:** JWT bearer tokens, BCrypt password hashing, role-based authorization

Users can register, log in, submit, view and edit their **own** feedback. Administrators
get a separate dashboard to view, add, edit and delete every user's feedback.

> Business rule: **one feedback entry per user.** It is enforced in the service layer, by a
> unique database constraint (`uk_feedback_user_id`) and in the UI (Add is disabled once a
> user already has feedback; Edit is disabled when there is none).

---

## 1. Project structure

```
task/
├─ backend/                      # Spring Boot REST API
│  ├─ pom.xml
│  ├─ mvnw / mvnw.cmd            # Maven wrapper (no local Maven needed)
│  ├─ db/schema.sql              # Documented MySQL schema
│  └─ src/main/java/com/example/feedback/
│     ├─ config/                 # SecurityConfig, AdminInitializer
│     ├─ controller/             # Auth, Feedback, AdminFeedback
│     ├─ dto/                    # Request/response records
│     ├─ entity/                 # User, Feedback, Role
│     ├─ exception/              # Custom exceptions + GlobalExceptionHandler
│     ├─ repository/             # Spring Data repositories
│     ├─ security/               # JWT service, filter, entry points, UserDetails
│     └─ service/                # AuthService, FeedbackService, AdminFeedbackService
├─ frontend/                     # React (Vite) SPA
│  └─ src/
│     ├─ pages/                  # Login, Register, Feedback, AdminDashboard, NotFound
│     ├─ components/             # ProtectedRoute, FeedbackForm, FeedbackTable, FeedbackDetails, Navbar, Alert, Spinner
│     ├─ services/               # api.js, authService.js, feedbackService.js
│     ├─ context/                # AuthContext
│     ├─ styles/                 # global.css, auth.css, feedback.css, admin.css
│     └─ utils/                  # format helpers
└─ postman/                      # Postman collection
```

---

## 2. Prerequisites

- Java 17+
- Node.js 18+ and npm
- MySQL 8 running locally (or reachable)
- (Optional) Postman for API testing. The Maven wrapper is included, so Maven is not required.

---

## 3. Database setup

Create the database (the tables are created automatically by JPA on startup, or run
`backend/db/schema.sql` manually):

```sql
CREATE DATABASE feedback_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The schema created:

- **users**(id PK, username UNIQUE, email UNIQUE, password [BCrypt hash], role [USER|ADMIN], created_at)
- **feedback**(id PK, user_id FK → users.id UNIQUE, message, created_at, updated_at)

Passwords are always stored as BCrypt hashes — never plaintext.

---

## 4. Running the backend

The backend ships with two Spring profiles:

| Profile | Database | When |
|---|---|---|
| `dev` **(default)** | Embedded H2 file DB | Zero-config local run — needs nothing installed |
| `mysql` | MySQL | Production / when you want MySQL |

### Option A — Zero configuration (embedded H2, recommended)

Just run it. The default `dev` profile uses an embedded H2 file database persisted at
`backend/data/feedbackdb.mv.db`. No MySQL installation, database creation, or credentials
are required.

From the `backend/` directory:

**Windows (PowerShell):**
```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**
```bash
./mvnw spring-boot:run
```

The API starts on **http://localhost:8080**. The admin account is created on first run; if
`ADMIN_PASSWORD` is not set, a random password is printed once to the console.

Optional: browse the database at **http://localhost:8080/h2-console**
(JDBC URL `jdbc:h2:file:./data/feedbackdb`, user `sa`, empty password).

To reset all data, stop the backend and delete the `backend/data` folder.

### Option B — MySQL

Set `SPRING_PROFILES_ACTIVE=mysql` and the DB credentials, then run.

**Windows (PowerShell):**
```powershell
$env:SPRING_PROFILES_ACTIVE="mysql"
$env:DB_URL="jdbc:mysql://localhost:3306/feedback_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
$env:JWT_SECRET="<base64-encoded-secret-at-least-32-bytes>"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_EMAIL="admin@example.com"
$env:ADMIN_PASSWORD="ChangeMe@Strong123"
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**
```bash
export SPRING_PROFILES_ACTIVE=mysql
export DB_URL="jdbc:mysql://localhost:3306/feedback_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export JWT_SECRET="<base64-encoded-secret-at-least-32-bytes>"
export ADMIN_USERNAME=admin
export ADMIN_EMAIL=admin@example.com
export ADMIN_PASSWORD="ChangeMe@Strong123"
./mvnw spring-boot:run
```

The API starts on **http://localhost:8080**.

### Environment variables

| Variable | Purpose | Default |
|---|---|---|
| `DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/feedback_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | MySQL user | `root` |
| `DB_PASSWORD` | MySQL password | *(empty)* |
| `JWT_SECRET` | Base64 HMAC secret (≥32 bytes decoded) | insecure dev default |
| `JWT_EXPIRATION_MS` | Token lifetime | `86400000` (24h) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | `http://localhost:5173,http://localhost:3000` |
| `ADMIN_USERNAME` | Bootstrap admin username | `admin` |
| `ADMIN_EMAIL` | Bootstrap admin email | `admin@example.com` |
| `ADMIN_PASSWORD` | Bootstrap admin password | *(random, printed once)* |
| `DDL_AUTO` | Hibernate DDL mode | `update` |
| `SPRING_PROFILES_ACTIVE` | Select `dev` (H2) or `mysql` | `dev` |

> The `DB_*` variables only apply to the `mysql` profile. The `dev` profile ignores them.
> Never commit real credentials. Generate `JWT_SECRET` with e.g.
> `openssl rand -base64 48`.

### Creating the initial admin account

The application creates one administrator on startup **only if no ADMIN exists**:

- If `ADMIN_PASSWORD` is set, that password is used.
- If `ADMIN_PASSWORD` is **not** set, a cryptographically random password is generated and
  printed **once** to the console. Copy it and log in immediately.

Self-registration always creates a `USER`; there is no way to become an admin from the
registration form.

---

## 5. Running the frontend

From the `frontend/` directory:

```bash
npm install
# optionally edit .env to point at a different API host
npm run dev
```

Open **http://localhost:5173**. The API base URL is configured via `VITE_API_BASE_URL`
(see `frontend/.env`). Build for production with `npm run build`.

---

## 6. REST API

Base URL: `http://localhost:8080/api`. Protected/authenticated endpoints require the header
`Authorization: Bearer <token>`.

### Auth
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/auth/register` | public | Register a new USER (201) |
| POST | `/auth/login` | public | Login, returns `{ token, tokenType, user }` |
| POST | `/auth/logout` | public | Stateless logout acknowledgement |
| GET | `/auth/me` | authenticated | Current user's details |

### User feedback
| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/feedback/my` | authenticated | Current user's feedback (204 if none) |
| POST | `/feedback` | authenticated | Create feedback (409 if one already exists) |
| PUT | `/feedback/{id}` | authenticated + owner | Update own feedback (403 otherwise) |

### Admin feedback (ADMIN only)
| Method | Path | Description |
|---|---|---|
| GET | `/admin/feedback` | List all feedback |
| GET | `/admin/feedback/{id}` | Get one feedback |
| POST | `/admin/feedback` | Create feedback for a `userId` |
| PUT | `/admin/feedback/{id}` | Update any feedback |
| DELETE | `/admin/feedback/{id}` | Delete feedback |

Ownership is always derived from the JWT principal — a frontend-supplied user id is never
trusted for user endpoints.

### Status codes
`400` validation/empty input · `401` missing/invalid token or bad credentials ·
`403` forbidden (role or ownership) · `404` not found · `409` duplicate username/email/feedback.

---

## 7. Testing

### Backend automated tests

```powershell
cd backend
.\mvnw.cmd test
```

`FeedbackFlowIntegrationTest` (H2, `test` profile) covers registration validation,
duplicate rejection, login, invalid credentials, feedback create/update, one-per-user
enforcement, ownership protection, admin list/edit/delete and role restrictions.

### Postman

Import `postman/Feedback-Management-System.postman_collection.json`. Set `baseUrl`, run
**Auth → Login** (the token is captured automatically into `{{token}}`), then exercise the
user and admin folders.

### Manual scenarios
1. Register successfully; try duplicate username/email (409) and mismatched passwords (400).
2. Login with valid credentials; try invalid credentials (401).
3. As a user, add feedback; reload the page and confirm it persists.
4. Edit your feedback.
5. Attempt to edit another user's feedback via the API → 403.
6. Log in as admin, view all feedback, view/edit/delete rows.
7. As a regular user, call `/api/admin/feedback` → 403.
8. Log out; confirm `/feedback` and `/admin` redirect to login.

---

## 8. Security notes

- BCrypt password hashing (`PasswordEncoder`).
- Stateless JWT auth; CSRF disabled because no session cookie is used (see `SecurityConfig`).
- `GET/PUT /api/feedback/**` require authentication; `/api/admin/**` requires `ROLE_ADMIN`.
- CORS restricted to configured origins.
- Bean Validation on all request DTOs; errors returned via `GlobalExceptionHandler`.
- No production secrets are hardcoded — all credentials come from environment variables.
