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
- Java 17+
- Maven 3.8+

### Build & Run

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/personal-finance-manager.git
cd personal-finance-manager

# Build
mvn clean install -DskipTests

# Run
mvn spring-boot:run
```

The API will start on `http://localhost:8080`.

### Run Tests

```bash
mvn test
```

### Build Deployable JAR

```bash
mvn clean package -DskipTests
java -jar target/personal-finance-manager-1.0.0.jar
```

---

## Deployment (Render)

1. Push to GitHub
2. Go to [render.com](https://render.com) → New Web Service
3. Connect your GitHub repo
4. Set:
   - **Environment**: Java
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/personal-finance-manager-1.0.0.jar`
5. Deploy!

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
GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&categoryId=1

Response 200:
{ "transactions": [...] }
```

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
    { "name": "Salary", "type": "INCOME", "isCustom": false },
    { "name": "Food", "type": "EXPENSE", "isCustom": false },
    { "name": "MyCategory", "type": "EXPENSE", "isCustom": true }
]}
```

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
