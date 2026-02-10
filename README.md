# Event-Driven Order Fulfillment System

A fully event-driven microservices architecture demonstrating asynchronous communication using **Java 17**, **Spring Boot**, **RabbitMQ**, **PostgreSQL**, and **Docker**. This system implements a complete order fulfillment workflow with zero REST-based inter-service communication.

## 🏗️ System Architecture

```mermaid
graph LR
    subgraph External["External Access"]
        Client[("fa:fa-user Client")] 
    end

    Client -->|HTTP Port 80| LB["fa:fa-network-wired Nginx Gateway<br/>(LoadBalancer)"]

    subgraph Cluster["Kubernetes Cluster"]
        direction LR
        
        subgraph Services["Microservices (2 Replicas each)"]
            OS["fa:fa-shopping-cart Order Service"]
            IS["fa:fa-boxes Inventory Service"]
            PS["fa:fa-credit-card Payment Service"]
            NS["fa:fa-bell Notification Service"]
        end

        subgraph Messaging["Message Broker"]
            RMQ["fa:fa-comments RabbitMQ"]
        end

        subgraph Storage["Persistence Layer"]
            OrderDB[("fa:fa-database Order DB")]
            InvDB[("fa:fa-database Inventory DB")]
            PayDB[("fa:fa-database Payment DB")]
            NotDB[("fa:fa-database Notification DB")]
        end

        %% Routing
        LB --> OS
        LB --> IS
        LB --> PS
        LB --> NS

        %% Event Flow
        OS -.->|OrderCreated| RMQ
        RMQ -.->|OrderCreated| IS
        IS -.->|InventoryReserved/Failed| RMQ
        RMQ -.->|InventoryReserved| PS
        PS -.->|PaymentCompleted/Failed| RMQ
        RMQ -.->|Final Status| NS

        %% DB Connections
        OS --- OrderDB
        IS --- InvDB
        PS --- PayDB
        NS --- NotDB
    end

    %% Black and White Styling
    classDef plain fill:#fff,stroke:#000,stroke-width:2px,color:#000;
    class OS,IS,PS,NS,RMQ,LB,Client,OrderDB,InvDB,PayDB,NotDB plain
    classDef cluster fill:#fff,stroke:#000,stroke-width:1px,stroke-dasharray: 5 5;
    class Cluster,Services,Messaging,Storage,External cluster
```

**Deployment**: Kubernetes with 2 replicas per service for high availability. Kubernetes Services provide automatic load balancing across pods.

## 📋 Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Microservices Overview](#microservices-overview)
- [Event Flow](#event-flow)
- [RabbitMQ Topology](#rabbitmq-topology)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Testing Scenarios](#testing-scenarios)
- [Monitoring](#monitoring)

## ✨ Features

- **Event-Driven Architecture**: Pure asynchronous communication via RabbitMQ
- **Microservices Pattern**: Four independent, loosely coupled services
- **Kubernetes Deployment**: Container orchestration with auto-scaling and self-healing
- **High Availability**: 2 replicas per service with automatic load balancing
- **Database Per Service**: Complete data isolation with PostgreSQL
- **Message-Driven Workflows**: Topic exchanges with routing keys
- **Saga Pattern**: Distributed transaction handling with compensating actions
- **JSON Events**: Structured event payloads with metadata
- **High-Volume CSV Data Upload**: Asynchronous processing of large CSV files (2M+ records) using RabbitMQ for chunking and flow control
- **API Gateway**: Nginx configured as a reverse proxy and load balancer
- **CI/CD Pipeline**: Automated Docker image builds and deployments via GitHub Actions
- **Production-Ready**: Health checks, logging, and error handling

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.1 |
| Database | PostgreSQL | 16 |
| Message Broker | RabbitMQ | 3.12 |
| ORM | Hibernate/JPA | - |
| Build Tool | Maven | 3.9.5 |
| Container | Docker | - |
| Orchestration | Kubernetes | 1.34.1 |
| Gateway | Nginx | 1.25 |
| CI/CD | GitHub Actions | - |

## 🎯 Microservices Overview

### 1. Order Service (Port 8081)
- **Responsibility**: REST API for order creation
- **Database**: `order_db`
- **Publishes**: `OrderCreatedEvent`
- **REST Endpoints**:
  - `POST /api/orders` - Create new order
  - `GET /api/orders` - List all orders
  - `GET /api/orders/{id}` - Get order by ID

### 2. Inventory Service (Port 8082)
- **Responsibility**: Inventory availability checking and reservation
- **Database**: `inventory_db`
- **Consumes**: `OrderCreatedEvent`
- **Publishes**: `InventoryReservedEvent`, `InventoryFailedEvent`
- **Features**:
  - Pre-initialized with sample inventory (PROD-001, PROD-002, PROD-003)
  - Stock reservation mechanism
  - Availability validation

### 3. Payment Service (Port 8083)
- **Responsibility**: Payment processing simulation
- **Database**: `payment_db`
- **Consumes**: `InventoryReservedEvent`
- **Publishes**: `PaymentCompletedEvent`, `PaymentFailedEvent`
- **Features**:
  - 80% success rate for demo purposes
  - Transaction tracking
  - Payment status persistence

### 4. Notification Service (Port 8084)
- **Responsibility**: Customer notifications
- **Database**: `notification_db`
- **Consumes**: `InventoryFailedEvent`, `PaymentCompletedEvent`, `PaymentFailedEvent`
- **Features**:
  - Email simulation (logged)
  - SMS simulation (logged)
  - Notification history persistence

## 🔄 Event Flow

### Success Scenario

```
1. Client → POST /api/orders → Order Service
2. Order Service → OrderCreatedEvent → RabbitMQ
3. Inventory Service ← OrderCreatedEvent ← RabbitMQ
4. Inventory Service → InventoryReservedEvent → RabbitMQ
5. Payment Service ← InventoryReservedEvent ← RabbitMQ
6. Payment Service → PaymentCompletedEvent → RabbitMQ
7. Notification Service ← PaymentCompletedEvent ← RabbitMQ
8. Notification Service → Sends Email + SMS
```

### Failure Scenarios

#### Inventory Failure
```
1-3. (Same as success)
4. Inventory Service → InventoryFailedEvent → RabbitMQ
5. Notification Service ← InventoryFailedEvent ← RabbitMQ
6. Notification Service → Sends failure notification
```

#### Payment Failure
```
1-5. (Same as success)
6. Payment Service → PaymentFailedEvent → RabbitMQ
7. Notification Service ← PaymentFailedEvent ← RabbitMQ
8. Notification Service → Sends failure notification
```

## 🐰 RabbitMQ Topology

### Exchanges

| Exchange | Type | Description |
|----------|------|-------------|
| `order.events` | Topic | Order-related events |
| `inventory.events` | Topic | Inventory-related events |
| `payment.events` | Topic | Payment-related events |

### Queues and Bindings

| Queue | Exchange | Routing Key | Consumer |
|-------|----------|-------------|----------|
| `order-created-queue` | `order.events` | `order.created` | Inventory Service |
| `inventory-reserved-queue` | `inventory.events` | `inventory.reserved` | Payment Service |
| `inventory-failed-queue` | `inventory.events` | `inventory.failed` | Notification Service |
| `payment-completed-queue` | `payment.events` | `payment.completed` | Notification Service |
| `payment-failed-queue` | `payment.events` | `payment.failed` | Notification Service |

### Event Schema

All events extend `BaseEvent` with:
```json
{
  "eventId": "uuid",
  "eventType": "EVENT_TYPE",
  "timestamp": "2026-01-15T22:43:06",
  "sourceService": "service-name",
  // event-specific fields
}
```

## 📦 Prerequisites

- **Docker Desktop**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher
- **Memory**: At least 4GB available for containers
- **Port Availability**: 5432-5435, 5672, 8081-8084, 15672

## 🚀 Quick Start

### 1. Clone and Navigate
```bash
cd /Users/zahid/Projects/AI/shoping
```

### 2. Build and Start All Services
```bash
docker-compose up --build
```

This will:
- Build Docker images for all 4 microservices
- Start RabbitMQ with management plugin
- Start 4 PostgreSQL databases
- Start all microservices with proper dependencies

### 3. Wait for Services to be Ready

Monitor logs until you see:
```
order-service       | Started OrderServiceApplication
inventory-service   | Started InventoryServiceApplication
payment-service     | Started PaymentServiceApplication
notification-service| Started NotificationServiceApplication
```

## 🚢 Kubernetes Deployment

### Prerequisites

- **Docker Desktop** with Kubernetes enabled
- **kubectl** configured for local cluster
- **GitHub Account** with Docker Hub credentials set as secrets

### Deploy to Kubernetes

1. **Enable Kubernetes in Docker Desktop:**
   - Open Docker Desktop → Settings → Kubernetes
   - Check "Enable Kubernetes"
   - Apply & Restart

2. **Verify kubectl context:**
   ```bash
   kubectl config current-context
   # Should show: docker-desktop
   ```

3. **Deploy all services:**
   ```bash
   kubectl apply -f k8s/
   ```

4. **Check deployment status:**
   ```bash
   kubectl get pods
   kubectl get services
   ```

5. **Access the application:**
   ```bash
   # Get nginx-gateway service details
   kubectl get svc nginx-gateway
   
   # Access via NodePort (if LoadBalancer is pending)
   curl http://localhost:<NodePort>/api/orders
   
   # Or use port-forward
   kubectl port-forward svc/nginx-gateway 8080:80
   curl http://localhost:8080/api/orders
   ```

### CI/CD with GitHub Actions

The project includes automated CI/CD pipeline:

1. **Set GitHub Secrets:**
   - `DOCKER_USERNAME`: Your Docker Hub username
   - `DOCKER_PASSWORD`: Your Docker Hub password/token

2. **Set up Self-Hosted Runner:**
   ```bash
   # On your local machine
   mkdir actions-runner && cd actions-runner
   # Follow instructions from: GitHub Repo → Settings → Actions → Runners → New self-hosted runner
   ```

3. **Automatic Deployment:**
   - Push code to `main` branch
   - GitHub Actions builds Docker images
   - Images pushed to Docker Hub
   - Self-hosted runner deploys to local Kubernetes cluster

### Kubernetes Features

- **High Availability**: 2 replicas per service
- **Auto-healing**: Failed pods automatically restart
- **Load Balancing**: Kubernetes Services distribute traffic
- **Rolling Updates**: Zero-downtime deployments
- **Self-Healing**: Automatic pod replacement on failure

### Scale Services

```bash
# Scale order-service to 3 replicas
kubectl scale deployment order-service --replicas=3

# Verify
kubectl get pods -l app=order-service
```

### View Logs

```bash
# All pods of a service
kubectl logs -l app=order-service

# Specific pod
kubectl logs <pod-name>

# Follow logs
kubectl logs -f -l app=order-service
```

### Delete Deployment

```bash
# Delete all resources
kubectl delete -f k8s/

# Or delete specific resources
kubectl delete deployment order-service
kubectl delete service order-service
```


## 🧪 Testing Scenarios

### Scenario 1: Successful Order Flow
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "items": [
      {
        "productId": "PROD-002",
        "productName": "Mouse",
        "quantity": 1,
        "price": 29.99
      }
    ]
  }'
```

**Expected Flow:**
1. ✅ Order created in Order Service
2. ✅ Inventory reserved (50 available)
3. ✅ Payment processed (80% chance)
4. ✅ Email + SMS notifications sent

**Check Logs:**
```bash
docker-compose logs -f notification-service
```

You should see:
```
📧 EMAIL NOTIFICATION SENT:
Subject: Order #1 - Payment Successful!
📱 SMS NOTIFICATION SENT:
```

### Scenario 2: Insufficient Inventory
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-002",
    "items": [
      {
        "productId": "PROD-001",
        "productName": "Laptop",
        "quantity": 100,
        "price": 999.99
      }
    ]
  }'
```

**Expected Flow:**
1. ✅ Order created
2. ❌ Inventory check fails (only 10 available)
3. ✅ Inventory failed notification sent

### Scenario 3: Payment Failure
Create multiple orders - approximately 20% will fail payment processing due to the simulated failure rate.

```bash
for i in {1..5}; do
  curl -X POST http://localhost:8081/api/orders \
    -H "Content-Type: application/json" \
    -d '{
      "customerId": "CUST-003",
      "items": [
        {
          "productId": "PROD-003",
          "productName": "Keyboard",
          "quantity": 1,
          "price": 49.99
        }
      ]
    }'
  sleep 1
done
```

**Expected**: ~1 order will fail payment and trigger failure notification.

## 📊 Monitoring

### RabbitMQ Management UI

**Access**: http://localhost:15672

**Features**:
- View exchanges and bindings
- Monitor queue depths
- Track message rates
- View message flow

**Navigate to**:
- **Exchanges**: See `order.events`, `inventory.events`, `payment.events`
- **Queues**: See all 5 queues with message counts
- **Connections**: See active connections from services

### Database Inspection

**Connect to Order Database:**
```bash
docker exec -it order-db psql -U orderuser -d order_db

# View tables
\dt

# View orders
SELECT * FROM orders;
SELECT * FROM order_items;
```

**Connect to Inventory Database:**
```bash
docker exec -it inventory-db psql -U inventoryuser -d inventory_db

# View inventory
SELECT * FROM inventory_items;
```

**Connect to Payment Database:**
```bash
docker exec -it payment-db psql -U paymentuser -d payment_db

# View payments
SELECT * FROM payments;
SELECT * FROM payment_transactions;
```

**Connect to Notification Database:**
```bash
docker exec -it notification-db psql -U notificationuser -d notification_db

# View notifications
SELECT * FROM notification_logs ORDER BY sent_at DESC;
```

### Service Logs

**View All Logs:**
```bash
docker-compose logs -f
```

**View Specific Service:**
```bash
docker-compose logs -f order-service
docker-compose logs -f inventory-service
docker-compose logs -f payment-service
docker-compose logs -f notification-service
```

**View RabbitMQ Logs:**
```bash
docker-compose logs -f rabbitmq
```

## 🐛 Troubleshooting

### Services Won't Start

**Issue**: Service fails to connect to RabbitMQ
```bash
# Check RabbitMQ status
docker-compose logs rabbitmq

# Restart RabbitMQ
docker-compose restart rabbitmq
```

**Issue**: Service fails to connect to database
```bash
# Check database logs
docker-compose logs order-db

# Verify database is healthy
docker exec -it order-db pg_isready -U orderuser -d order_db
```

### RabbitMQ Queue Issues

**Issue**: Messages not being consumed
```bash
# Check if queues are bound correctly
# Visit: http://localhost:15672/#/queues
# Verify bindings in "Bindings" section
```

### Nginx 502 Bad Gateway

**Issue**: Nginx returns 502 Bad Gateway because it holds onto stale IP addresses after backend services restart.
**Solution**: Configure Nginx to use Docker's embedded DNS resolver (`127.0.0.11`) and variables for upstreams to force dynamic resolution.

```nginx
resolver 127.0.0.11 valid=10s;
set $backend_host service-name;
proxy_pass http://$backend_host:port;
```

### Port Conflicts

**Issue**: Port already in use
```bash
# Check what's using the port
lsof -i :8081

# Modify port mapping in docker-compose.yml if needed
```

## 🎓 Learning Outcomes

This project demonstrates:

1. **Event-Driven Architecture**: Asynchronous, loosely coupled microservices
2. **Message-Driven Communication**: RabbitMQ topic exchanges and routing
3. **Database Per Service**: Complete data isolation
4. **Event Sourcing Patterns**: Event metadata and structured payloads
5. **Saga Pattern**: Distributed transactions with compensating actions
6. **Container Orchestration**: Multi-container application deployment
7. **Service Dependencies**: Proper startup ordering and health checks
8. **Error Handling**: Graceful failure handling and notifications

## 🔐 Production Considerations

For production deployment, consider:

1. **Security**:
   - Use secure credentials (not defaults)
   - Implement authentication/authorization
   - Enable TLS for RabbitMQ and databases

2. **Scalability**:
   - Add load balancers
   - Enable horizontal scaling for services
   - Use RabbitMQ clustering

3. **Resilience**:
   - Implement circuit breakers
   - Add retry mechanisms with exponential backoff
   - Use dead-letter queues

4. **Monitoring**:
   - Add Prometheus + Grafana
   - Implement distributed tracing (Zipkin/Jaeger)
   - Set up centralized logging (ELK stack)

5. **High Availability**:
   - Deploy PostgreSQL with replication
   - Use RabbitMQ quorum queues
   - Implement service mesh (Istio)

---

**Built with ❤️ using Event-Driven Architecture**
