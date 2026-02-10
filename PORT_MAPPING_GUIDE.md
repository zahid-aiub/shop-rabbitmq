# Docker Port Mapping Reference

## Understanding Docker Port Mapping

When using Docker Compose, there are **two different network contexts**:

### 1. **Docker Internal Network** (Container-to-Container)
Services communicate with each other using:
- **Service names** as hostnames (e.g., `payment-db`, `inventory-db`)
- **Internal container ports** (always 5432 for PostgreSQL)

### 2. **Host Network** (Your Computer to Container)
You access containers from your laptop using:
- **localhost** as hostname
- **Mapped host ports** (5432, 5433, 5434, 5435)

## Port Mapping Configuration

### PostgreSQL Databases

```yaml
# docker-compose.yml
services:
  order-db:
    ports:
      - "5432:5432"  # Host:Container

  inventory-db:
    ports:
      - "5433:5432"  # Host:Container

  payment-db:
    ports:
      - "5434:5432"  # Host:Container

  notification-db:
    ports:
      - "5435:5432"  # Host:Container
```

**Explanation**:
- **Left side (5432, 5433, etc.)**: Port on **your computer** (host)
- **Right side (always 5432)**: Port **inside the container**

### How Services Connect

#### When Running in Docker Compose

```yaml
# application.yml (ALL services use port 5432)
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:payment_db}
```

**Docker Compose sets environment variables**:
```yaml
environment:
  DB_HOST: payment-db      # Service name, NOT localhost
  DB_NAME: payment_db
```

**Actual connection**: `payment-db:5432` ✅

#### When Running Locally (Development)

If you run a service directly on your computer (not in Docker):
- Order Service → `localhost:5432`
- Inventory Service → `localhost:5433` (would need to change application.yml)
- Payment Service → `localhost:5434` (would need to change application.yml)
- Notification Service → `localhost:5435` (would need to change application.yml)

## Summary Table

| Service | DB Service Name | Docker Network | Host Access |
|---------|----------------|----------------|-------------|
| order-service | order-db | `order-db:5432` | `localhost:5432` |
| inventory-service | inventory-db | `inventory-db:5432` | `localhost:5433` |
| payment-service | payment-db | `payment-db:5432` | `localhost:5434` |
| notification-service | notification-db | `notification-db:5432` | `localhost:5435` |

## Correct Configuration Files

### ✅ All `application.yml` Files (Correct)

```yaml
# order-service/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:order_db}

# inventory-service/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:inventory_db}

# payment-service/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:payment_db}

# notification-service/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:notification_db}
```

**Note**: All use port **5432** because `DB_HOST` is overridden by Docker Compose to the service name.

### ❌ Wrong Configuration

```yaml
# WRONG - Don't do this!
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5434/${DB_NAME:payment_db}
    # Port 5434 is for host access only, not for Docker internal network
```

## Common Confusion

**Question**: Why do inventory, payment, and notification databases have different ports (5433, 5434, 5435)?

**Answer**: 
- These ports are **only** for accessing from **your computer** using tools like `psql`, DBeaver, or pgAdmin
- Services running **inside Docker** don't use these ports
- All PostgreSQL containers internally listen on standard port **5432**

## Examples

### Accessing from Your Computer

```bash
# Connect to order database
docker exec -it order-db psql -U orderuser -d order_db
# OR from host
psql -h localhost -p 5432 -U orderuser -d order_db

# Connect to payment database
docker exec -it payment-db psql -U paymentuser -d payment_db
# OR from host
psql -h localhost -p 5434 -U paymentuser -d payment_db
```

### Service-to-Database (Inside Docker)

All services connect to port **5432** with the database service name:
- `payment-service` → `payment-db:5432`
- `inventory-service` → `inventory-db:5432`
- `order-service` → `order-db:5432`
- `notification-service` → `notification-db:5432`

## Key Takeaway

**✅ ALWAYS use port 5432 in `application.yml` files** when using Docker Compose, because the environment variable `DB_HOST` is set to the service name (e.g., `payment-db`), not `localhost`.
