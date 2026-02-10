# Running Services from IntelliJ IDEA

This guide explains how to run the microservices directly from IntelliJ IDEA while keeping the databases and RabbitMQ in Docker containers.

## Prerequisites

1. **Start only the infrastructure** (databases + RabbitMQ):
```bash
docker-compose up -d order-db inventory-db payment-db notification-db rabbitmq
```

2. **Don't start the Spring Boot services in Docker** (we'll run them from IntelliJ)

## Port Mapping for Local Development

When running from IntelliJ, services connect to databases on these ports:

| Service | Database | Host Port |
|---------|----------|-----------|
| order-service | order-db | 5432 |
| inventory-service | inventory-db | 5433 |
| payment-service | payment-db | 5434 |
| notification-service | notification-db | 5435 |

## Setup in IntelliJ IDEA

### Method 1: Using Spring Profiles (Recommended)

Each service now has an `application-local.yml` profile configured with correct local ports.

**Steps for each service**:

1. **Open Run Configuration** (Run → Edit Configurations)
2. **Add New Spring Boot Configuration** (+ → Spring Boot)
3. **Set these values**:
   - **Name**: `OrderServiceApplication` (or appropriate service name)
   - **Main class**: `com.example.order.OrderServiceApplication`
   - **Active profiles**: `local`
   - **Working directory**: `/Users/zahid/Projects/AI/shoping/order-service`
   - **Module**: `order-service` (or appropriate service)

4. **Repeat for all services**:
   - `OrderServiceApplication` → profile: `local`
   - `InventoryServiceApplication` → profile: `local`
   - `PaymentServiceApplication` → profile: `local`
   - `NotificationServiceApplication` → profile: `local`

### Method 2: Using Environment Variables

If you prefer not to use profiles, set these environment variables in each run configuration:

**Order Service**:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/order_db
SPRING_DATASOURCE_USERNAME=orderuser
SPRING_DATASOURCE_PASSWORD=orderpass
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
```

**Inventory Service**:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/inventory_db
SPRING_DATASOURCE_USERNAME=inventoryuser
SPRING_DATASOURCE_PASSWORD=inventorypass
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
```

**Payment Service**:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/payment_db
SPRING_DATASOURCE_USERNAME=paymentuser
SPRING_DATASOURCE_PASSWORD=paymentpass
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
```

**Notification Service**:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5435/notification_db
SPRING_DATASOURCE_USERNAME=notificationuser
SPRING_DATASOURCE_PASSWORD=notificationpass
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
```

## Quick Start Guide

### 1. Start Infrastructure
```bash
cd /Users/zahid/Projects/AI/shoping
docker-compose up -d order-db inventory-db payment-db notification-db rabbitmq
```

### 2. Verify Infrastructure
```bash
docker-compose ps
```

You should see 5 containers running (4 databases + RabbitMQ).

### 3. Run Services from IntelliJ

**Option A - Using Profiles**:
1. Right-click on `OrderServiceApplication.java` → Run
2. Edit the configuration and add active profile: `local`
3. Run the service
4. Repeat for other services

**Option B - Using Maven**:
```bash
# From each service directory
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 4. Verify Services
Check that all services started successfully:
- Order Service: http://localhost:8081/swagger-ui.html
- Inventory Service: Logs show "Sample inventory initialized"
- Payment Service: Logs show successful startup
- Notification Service: Logs show RabbitMQ connection

## Testing

Create a test order to verify the complete flow:
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-TEST",
    "items": [{
      "productId": "PROD-001",
      "productName": "Laptop",
      "quantity": 1,
      "price": 999.99
    }]
  }'
```

Watch the logs in IntelliJ to see the event flow across all services.

## Troubleshooting

### Database Connection Failed

**Error**: `Connection to localhost:5432 refused`

**Solution**: 
- For **inventory-service**, use port 5433
- For **payment-service**, use port 5434
- For **notification-service**, use port 5435
- Make sure you're using the `local` profile or correct environment variables

### RabbitMQ Connection Failed

**Error**: `Connection refused: localhost:5672`

**Solution**: Make sure RabbitMQ container is running:
```bash
docker-compose up -d rabbitmq
```

### Port Already in Use

**Error**: `Port 8081 is already in use`

**Solution**: 
- Stop the Docker container for that service: `docker-compose stop order-service`
- Or change the port in IntelliJ run configuration

## Debugging Tips

1. **Enable Debug Logging**: Already configured in `application-local.yml`

2. **Watch Multiple Services**: Use IntelliJ's "Services" tab to see all running Spring Boot applications

3. **Database Inspection**: Use IntelliJ's Database tool to connect:
   - Order DB: `localhost:5432`
   - Inventory DB: `localhost:5433`
   - Payment DB: `localhost:5434`
   - Notification DB: `localhost:5435`

4. **RabbitMQ Management UI**: http://localhost:15672 (guest/guest)

## Cleanup

Stop infrastructure when done:
```bash
docker-compose down
```

Or keep databases running and just stop/start services from IntelliJ as needed.
