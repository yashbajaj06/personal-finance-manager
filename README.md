# Personal Finance Manager API

A REST API built with **Spring Boot 3** and **Java 17** for managing personal finances — track income, expenses, and savings goals.

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security (Session-based) |
| Database | H2 (in-memory) |
| Build Tool | Maven |
| Testing | JUnit 5 + Mockito |

---

## Getting Started

### Prerequisites
- Java 17+ (only requirement — Maven does **not** need to be installed separately, the project ships with the Maven Wrapper)

### Build & Run

The repo includes the Maven Wrapper (`mvnw` / `mvnw.cmd`), so it builds and runs the same way on any machine — Windows, macOS, or Linux — without anyone needing Maven pre-installed globally.

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/personal-finance-manager.git
cd personal-finance-manager

# Build (Linux/macOS)
./mvnw clean install -DskipTests

# Build (Windows)
mvnw.cmd clean install -DskipTests

# Run
./mvnw spring-boot:run      # Linux/macOS
mvnw.cmd spring-boot:run    # Windows
```

The API will start on `http://localhost:8080`.

If you already have Maven installed globally, the plain `mvn` commands work identically — the wrapper is just there so it also works out of the box for anyone who doesn't.

### Run Tests

```bash
./mvnw test        # Linux/macOS
mvnw.cmd test       # Windows
```

### Build Deployable JAR

```bash
./mvnw clean package -DskipTests
java -jar target/personal-finance-manager-1.0.0.jar
```

### Run with Docker (no Java/Maven install needed at all)

The most portable option — only Docker is required on the host machine:

```bash
docker build -t personal-finance-manager .
docker run -p 8080:8080 personal-finance-manager
```

---

## Deployment (Render)

1. Push to GitHub
2. Go to [render.com](https://render.com) → New Web Service
3. Connect your GitHub repo
4. Set:
   - **Environment**: Java
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/personal-finance-manager-1.0.0.jar`
   - **Environment Variable**: `COOKIE_SECURE=true` (Render serves over HTTPS, so the session cookie should be marked secure in production; it defaults to `false` so the app still works over plain HTTP when run locally)
5. Deploy!

Alternatively, `render.yaml` in this repo already defines the above and can be used with Render's Blueprint deploy.

---

## API Documentation

### Authentication

All endpoints except `/api/auth/register` and `/api/auth/login` require authentication via session cookie.

#### Register
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}

Response 201:
{ "message": "User registered successfully", "userId": 1 }
```

#### Login
```
POST /api/auth/login

{ "username": "user@example.com", "password": "password123" }

Response 200:
{ "message": "Login successful" }
```
Cookie `JSESSIONID` is set — include it in all subsequent requests.

#### Logout
```
POST /api/auth/logout

Response 200:
{ "message": "Logout successful" }
```

---

### Transactions

#### Create Transaction
```
POST /api/transactions

{
  "amount": 50000.00,
  "date": "2024-01-15",
  "category": "Salary",
  "description": "January Salary"
}

Response 201:
{ "id": 1, "amount": 50000.00, "date": "2024-01-15", "category": "Salary", "description": "January Salary", "type": "INCOME" }
```

#### Get Transactions
```
GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&categoryId=1&type=INCOME

Response 200:
{ "transactions": [...] }
```
All query parameters are optional and can be combined freely: `startDate`/`endDate` (date range), `categoryId` (from the `id` field returned by `GET /api/categories`), and `type` (`INCOME` or `EXPENSE`).

#### Update Transaction
```
PUT /api/transactions/{id}

{ "amount": 60000.00, "description": "Updated" }
```
> Note: Date cannot be changed.

#### Delete Transaction
```
DELETE /api/transactions/{id}

Response 200:
{ "message": "Transaction deleted successfully" }
```

---

### Categories

#### Get All Categories
```
GET /api/categories

Response 200:
{ "categories": [
    { "id": 1, "name": "Salary", "type": "INCOME", "isCustom": false },
    { "id": 2, "name": "Food", "type": "EXPENSE", "isCustom": false },
    { "id": 8, "name": "MyCategory", "type": "EXPENSE", "isCustom": true }
]}
```
`id` is what you pass as `categoryId` when filtering `GET /api/transactions`.

#### Default Categories
- INCOME: `Salary`
- EXPENSE: `Food`, `Rent`, `Transportation`, `Entertainment`, `Healthcare`, `Utilities`

#### Create Custom Category
```
POST /api/categories

{ "name": "FreelanceWork", "type": "INCOME" }

Response 201:
{ "name": "FreelanceWork", "type": "INCOME", "isCustom": true }
```

#### Delete Custom Category
```
DELETE /api/categories/{name}

Response 200:
{ "message": "Category deleted successfully" }
```

---

### Savings Goals

#### Create Goal
```
POST /api/goals

{
  "goalName": "Emergency Fund",
  "targetAmount": 5000.00,
  "targetDate": "2026-12-01",
  "startDate": "2025-01-01"
}
```

#### Get All Goals
```
GET /api/goals
```

#### Get Goal by ID
```
GET /api/goals/{id}
```

#### Update Goal
```
PUT /api/goals/{id}

{ "targetAmount": 6000.00, "targetDate": "2027-01-01" }
```

#### Delete Goal
```
DELETE /api/goals/{id}
```

---

### Reports

#### Monthly Report
```
GET /api/reports/monthly/{year}/{month}

Response 200:
{
  "month": 1,
  "year": 2024,
  "totalIncome": { "Salary": 3000.00 },
  "totalExpenses": { "Food": 400.00, "Rent": 1200.00 },
  "netSavings": 1400.00
}
```

#### Yearly Report
```
GET /api/reports/yearly/{year}
```

---

## Error Codes

| Code | Meaning |
|------|---------|
| 400 | Bad Request — validation error |
| 401 | Unauthorized — not logged in |
| 403 | Forbidden — accessing another user's data |
| 404 | Not Found |
| 409 | Conflict — duplicate resource |

---

## Design Decisions

- **Session-based auth** using Spring Security with `HttpSession` — no JWT needed, cookie is automatically managed.
- **H2 in-memory database** — zero-config, suitable for assignment; easily swappable to PostgreSQL.
- **Layered architecture**: `Controller → Service → Repository` with strict separation of concerns.
- **DTOs** separate request/response objects from JPA entities, preventing over-posting and serialization issues.
- **Global exception handler** with `@ControllerAdvice` ensures consistent JSON error responses across all endpoints.
- **Default categories** seeded on startup via `CommandLineRunner`.
- **Data isolation** enforced at service layer — all queries filter by the authenticated user.
