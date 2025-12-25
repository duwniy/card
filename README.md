# Card Processing Service

REST API backend service for card processing operations including card creation, fund management, blocking/unblocking, and transaction history tracking.

## 📋 Table of Contents

- [Technologies](#technologies)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Database Schema](#database-schema)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

## 🛠 Technologies

- **Java 17** - Programming language
- **Spring Boot 3.4.1** - Application framework
- **PostgreSQL 15** - Database
- **Liquibase** - Database migration management
- **Maven** - Build tool
- **Docker & Docker Compose** - Containerization
- **JWT** - Authentication
- **Swagger/OpenAPI 3** - API documentation
- **Log4j2** - Logging
- **Hibernate/JPA** - ORM

## ✨ Features

- ✅ **Card Management**: Create, retrieve, block, and unblock cards
- ✅ **Transaction Operations**: Debit and credit operations with currency exchange
- ✅ **Idempotency**: Safe retry mechanism for all write operations
- ✅ **Card Limits**: Maximum 3 active cards per user
- ✅ **Currency Support**: UZS and USD with automatic exchange via CBU API
- ✅ **Transaction History**: Paginated transaction listing with filters
- ✅ **ETag Support**: Optimistic locking for concurrent updates
- ✅ **JWT Authentication**: Secure API access
- ✅ **Health Checks**: Application monitoring endpoints
- ✅ **Comprehensive Error Handling**: Detailed error responses

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Docker**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher

For local development (optional):
- **Java JDK 17**
- **Maven 3.8+**

## 🚀 Installation & Setup

### Quick Start with Docker (Recommended)

1. **Clone the repository**
```bash
   git clone <your-repository-url>
   cd card
```

2. **Start all services**
```bash
   docker-compose up --build -d
```

3. **Verify services are running**
```bash
   docker-compose ps
```

4. **Check application logs**
```bash
   docker-compose logs -f app
```

5. **Access the application**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - Health Check: `http://localhost:8080/actuator/health`

### Local Development Setup

1. **Start PostgreSQL only**
```bash
   docker-compose up postgres -d
```

2. **Run the application**
```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

3. **Build the project**
```bash
   ./mvnw clean package
```

## 📚 API Documentation

### Interactive Documentation

Once the application is running, access the interactive Swagger UI at:
```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI Specification

Raw OpenAPI 3.0 specification available at:
```
http://localhost:8080/v3/api-docs
```

### Main API Endpoints

#### Authentication
All endpoints (except Swagger) require JWT authentication via `Authorization: Bearer <token>` header.

#### Card Operations

| Method | Endpoint | Description | Idempotency |
|--------|----------|-------------|-------------|
| POST | `/api/v1/cards` | Create a new card | ✅ Yes |
| GET | `/api/v1/cards/{cardId}` | Get card details | ❌ No |
| POST | `/api/v1/cards/{cardId}/block` | Block a card | ❌ No |
| POST | `/api/v1/cards/{cardId}/unblock` | Unblock a card | ❌ No |

#### Transaction Operations

| Method | Endpoint | Description | Idempotency |
|--------|----------|-------------|-------------|
| POST | `/api/v1/cards/{cardId}/debit` | Withdraw funds | ✅ Yes |
| POST | `/api/v1/cards/{cardId}/credit` | Top up funds | ✅ Yes |
| GET | `/api/v1/cards/{cardId}/transactions` | Get transaction history | ❌ No |

## ⚙️ Configuration

### Environment Variables

Configure the following environment variables in `docker-compose.yml`:
```yaml
environment:
  # Spring Profile
  SPRING_PROFILES_ACTIVE: prod
  
  # Database Configuration
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/card_db
  SPRING_DATASOURCE_USERNAME: card_db
  SPRING_DATASOURCE_PASSWORD: duwniy00
  
  # JWT Configuration
  JWT_SECRET: your-super-secret-jwt-key-change-this-in-production-min-256-bits
  JWT_EXPIRATION: 86400000  # 24 hours in milliseconds
```

### Application Profiles

- **dev**: Development profile (verbose logging, show SQL)
- **prod**: Production profile (minimal logging, optimized settings)

## 💡 Usage Examples

### 1. Create a Card
```bash
curl -X POST http://localhost:8080/api/v1/cards \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Idempotency-Key: 550e8400-e29b-41d4-a716-446655440001" \
  -d '{
    "user_id": 1234,
    "status": "ACTIVE",
    "initial_amount": 100000,
    "currency": "UZS"
  }'
```

**Response:**
```json
{
  "card_id": "3a922971-55a0-4a86-a82d-dd4e581032eb",
  "user_id": 1234,
  "status": "ACTIVE",
  "balance": 100000,
  "currency": "UZS"
}
```

### 2. Get Card Details
```bash
curl -X GET http://localhost:8080/api/v1/cards/3a922971-55a0-4a86-a82d-dd4e581032eb \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Withdraw Funds (Debit)
```bash
curl -X POST http://localhost:8080/api/v1/cards/3a922971-55a0-4a86-a82d-dd4e581032eb/debit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Idempotency-Key: 660e8400-e29b-41d4-a716-446655440002" \
  -d '{
    "external_id": "tx-001",
    "amount": 50000,
    "currency": "UZS",
    "purpose": "P2P"
  }'
```

### 4. Top Up Funds (Credit)
```bash
curl -X POST http://localhost:8080/api/v1/cards/3a922971-55a0-4a86-a82d-dd4e581032eb/credit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Idempotency-Key: 770e8400-e29b-41d4-a716-446655440003" \
  -d '{
    "external_id": "tx-002",
    "amount": 200000,
    "currency": "UZS"
  }'
```

### 5. Block Card
```bash
# First, get the ETag
curl -I http://localhost:8080/api/v1/cards/3a922971-55a0-4a86-a82d-dd4e581032eb \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Then block with ETag
curl -X POST http://localhost:8080/api/v1/cards/3a922971-55a0-4a86-a82d-dd4e581032eb/block \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "If-Match: \"0\""
```

### 6. Get Transaction History
```bash
curl -X GET "http://localhost:8080/api/v1/cards/3a922971-55a0-4a86-a82d-dd4e581032eb/transactions?page=0&size=10&type=DEBIT" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🗄️ Database Schema

The application uses Liquibase for database migrations. Key tables:

### Tables

1. **cards**
   - `card_id` (UUID, PK)
   - `user_id` (BIGINT)
   - `status` (VARCHAR) - ACTIVE, BLOCKED, CLOSED
   - `balance` (BIGINT) - Amount in tiyin (1/100 of currency unit)
   - `currency` (VARCHAR) - UZS, USD
   - `version` (BIGINT) - For optimistic locking
   - `created_at`, `updated_at` (TIMESTAMP)

2. **transactions**
   - `transaction_id` (UUID, PK)
   - `card_id` (UUID, FK)
   - `external_id` (VARCHAR)
   - `type` (VARCHAR) - DEBIT, CREDIT
   - `amount` (BIGINT)
   - `after_balance` (BIGINT)
   - `currency` (VARCHAR)
   - `purpose` (VARCHAR) - For debit: P2P, PAYMENT
   - `exchange_rate` (BIGINT)
   - `created_at` (TIMESTAMP)

3. **idempotency_records**
   - `id` (BIGINT, PK)
   - `idempotency_key` (VARCHAR, UNIQUE)
   - `endpoint` (VARCHAR)
   - `resource_id` (VARCHAR)
   - `resource_type` (VARCHAR)
   - `response_status` (INTEGER)
   - `response_body` (TEXT)
   - `created_at`, `expires_at` (TIMESTAMP)

### Access Database
```bash
# Connect to PostgreSQL
docker exec -it card-db-postgres psql -U card_db -d card_db

# View tables
\dt

# Query cards
SELECT * FROM cards;

# Query transactions
SELECT * FROM transactions;

# Exit
\q
```

## 🔧 Development

### Project Structure
```
card/
├── src/
│   ├── main/
│   │   ├── java/org/example/card/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── enums/           # Enumerations
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── mapper/          # MapStruct mappers
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Spring Data repositories
│   │   │   ├── security/        # Security configuration
│   │   │   ├── service/         # Business logic
│   │   │   └── CardApplication.java
│   │   └── resources/
│   │       ├── db/changelog/    # Liquibase migrations
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/                    # Unit tests
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

### Adding New Features

1. Create entity in `model/`
2. Create repository in `repository/`
3. Create service in `service/`
4. Create DTO in `dto/`
5. Create mapper in `mapper/`
6. Create controller in `controller/`
7. Add Liquibase changelog in `resources/db/changelog/`

## 🧪 Testing

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=CardServiceTest
```

### Generate Test Coverage Report
```bash
./mvnw clean test jacoco:report
```

## 🚢 Deployment

### Production Deployment

1. **Update environment variables** in `docker-compose.yml`:
   - Change `JWT_SECRET` to a strong secret key
   - Update database credentials
   - Set `SPRING_PROFILES_ACTIVE: prod`

2. **Build and deploy**:
```bash
   docker-compose -f docker-compose.yml up --build -d
```

3. **Monitor logs**:
```bash
   docker-compose logs -f
```

### CI/CD Pipeline

Example GitLab CI configuration (`.gitlab-ci.yml`):
```yaml
stages:
  - build
  - test
  - deploy

build:
  stage: build
  image: maven:3.8-openjdk-17
  script:
    - mvn clean package -DskipTests
  artifacts:
    paths:
      - target/*.jar

test:
  stage: test
  image: maven:3.8-openjdk-17
  script:
    - mvn test

deploy:
  stage: deploy
  script:
    - docker-compose up --build -d
  only:
    - main
```

## 🔍 Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Check what's using port 8080
sudo lsof -i :8080

# Or change port in docker-compose.yml
ports:
  - "8081:8080"  # Change external port
```

#### Database Connection Failed
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check logs
docker-compose logs postgres

# Restart services
docker-compose restart
```

#### Application Won't Start
```bash
# Check logs
docker-compose logs app

# Rebuild from scratch
docker-compose down -v
docker-compose up --build
```

### Logs Location

- **Application logs**: `docker-compose logs app`
- **Database logs**: `docker-compose logs postgres`
- **All logs**: `docker-compose logs -f`

### Health Monitoring
```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check detailed metrics
curl http://localhost:8080/actuator/metrics
```

## 📝 API Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `missing_field` | 400 | Required field is missing |
| `invalid_data` | 400 | Data validation failed |
| `limit_exceeded` | 400 | Card limit reached (max 3) |
| `insufficient_funds` | 400 | Not enough balance |
| `incompatible_status` | 400 | Invalid card status for operation |
| `unauthorized` | 401 | Missing or invalid JWT token |
| `forbidden` | 403 | No permission for this resource |
| `not_found` | 404 | Resource not found |

## 👥 Authors

[duwniy]

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
