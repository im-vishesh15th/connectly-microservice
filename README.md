
**Connectly** is a social networking platform built using microservices architecture. The system manages user connections through a graph database and provides a scalable foundation for social features including connection requests, acceptance/rejection workflows, and event-driven notifications.

### Architecture Patterns
- Microservices Pattern
Each service is an independently deployable Spring Boot application registered with Eureka for dynamic service discovery. Services communicate via:

- Synchronous: REST APIs through the API Gateway
Asynchronous: Kafka event streams
For details, see Service Discovery with Eureka and Event-Driven Communication.

- API Gateway Pattern
The ApiGatewayApplication serves as the single entry point at port 8070, implementing:

Route-based request forwarding using Spring Cloud Gateway
JWT-based authentication via AuthenticationFilter
Path prefix stripping (StripPrefix=2) to translate external URLs to service-internal paths
For implementation details, see Request Routing and JWT Authentication.

**Event-Driven Architecture
The connections service publishes domain events to Kafka topics when connections are created or accepted, enabling loosely coupled integration with other services (notifications, activity feeds, analytics).

### Project Overview

**Connectly** is a microservices application built with Spring Boot that includes:
- **4 Core Services**: User Service, Posts Service, Connections Service, and Notification Service [1](#0-0) 
- **Service Discovery**: Eureka Server for service registration [2](#0-1) 
- **API Gateway**: Single entry point for all services [3](#0-2) 
- **Message Broker**: Kafka (KRaft mode) for event-driven communication [4](#0-3) 
- **Databases**: PostgreSQL for user/posts/notifications, Neo4j for connections graph [5](#0-4) 

### Architecture Diagram

```mermaid
graph TB
    Client["Client Applications"]
    Gateway["API Gateway :8080"]
    Eureka["Discovery Server :8761"]
    
    UserSvc["User Service"]
    PostsSvc["Posts Service"]
    ConnSvc["Connections Service"]
    NotifSvc["Notification Service"]
    
    UserDB[("PostgreSQL<br/>User DB")]
    PostsDB[("PostgreSQL<br/>Posts DB")]
    NotifDB[("PostgreSQL<br/>Notification DB")]
    ConnDB[("Neo4j<br/>Connections DB")]
    
    Kafka["Kafka Broker :9092"]
    KafkaUI["Kafka UI :8090"]
    
    Client --> Gateway
    Gateway --> Eureka
    
    UserSvc --> Eureka
    PostsSvc --> Eureka
    ConnSvc --> Eureka
    NotifSvc --> Eureka
    
    Gateway -.-> UserSvc
    Gateway -.-> PostsSvc
    Gateway -.-> ConnSvc
    Gateway -.-> NotifSvc
    
    UserSvc --> UserDB
    PostsSvc --> PostsDB
    NotifSvc --> NotifDB
    ConnSvc --> ConnDB
    
    UserSvc --> Kafka
    PostsSvc --> Kafka
    ConnSvc --> Kafka
    NotifSvc --> Kafka
    
    Kafka --> KafkaUI
```

### Service Ports

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| Discovery Server | 8762 (Docker) / 8761 (K8s) |
| Kafka UI | 8090 |
| Neo4j Browser | 7474 |
| Neo4j Bolt | 7687 |

<cite/>

## Docker Compose Deployment

### Prerequisites
- Docker and Docker Compose installed
- At least 4GB RAM available

### Steps

1. **Clone the repository**
```bash
git clone <repository-url>
cd connectly-microservice
```

2. **Start all services**
```bash
docker-compose up -d
``` [6](#0-5) 

3. **Verify services are running**
```bash
docker-compose ps
```

4. **Access the application**
    - API Gateway: http://localhost:8080
    - Eureka Dashboard: http://localhost:8762
    - Kafka UI: http://localhost:8090
    - Neo4j Browser: http://localhost:7474 (credentials: neo4j/password)

5. **View logs**
```bash
docker-compose logs -f [service-name]
```

6. **Stop all services**
```bash
docker-compose down
```

7. **Clean up volumes** (removes all data)
```bash
docker-compose down -v
```

<cite/>

## Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (minikube, kind, or cloud provider)
- kubectl configured
- At least 8GB RAM available for the cluster

### Steps

1. **Apply database deployments first**
```bash
kubectl apply -f k8s/notification-db.yml
kubectl apply -f k8s/posts-db.yml
kubectl apply -f k8s/user-db.yml
kubectl apply -f k8s/connections-db.yml
``` [7](#0-6) [8](#0-7) 

2. **Deploy Kafka**
```bash
kubectl apply -f k8s/kafka.yml
``` [9](#0-8) 

3. **Deploy Kafka UI (optional)**
```bash
kubectl apply -f k8s/kafkaui.yml
``` [10](#0-9) 

4. **Deploy Discovery Server**
```bash
kubectl apply -f k8s/discovery-server.yml
``` [11](#0-10) 

5. **Wait for Discovery Server to be ready**
```bash
kubectl wait --for=condition=ready pod -l app=discovery-server --timeout=300s
```

6. **Deploy microservices**
```bash
kubectl apply -f k8s/user-service.yml
kubectl apply -f k8s/notification-service.yml
kubectl apply -f k8s/connections-service.yml
kubectl apply -f k8s/posts-service.yml
``` [12](#0-11) 

7. **Deploy API Gateway**
```bash
kubectl apply -f k8s/api-gateway.yml
``` [13](#0-12) 

8. **Verify all pods are running**
```bash
kubectl get pods
kubectl get services
```

9. **Access the application**

For Minikube:
```bash
minikube service api-gateway --url
```

For port-forwarding:
```bash
kubectl port-forward service/api-gateway 8080:80
```

10. **View logs**
```bash
kubectl logs -f deployment/[service-name]
```

11. **Scale services** (if needed)
```bash
kubectl scale deployment user-service --replicas=3
```

12. **Clean up**
```bash
kubectl delete -f k8s/
```

### Resource Requirements

| Component | CPU Request | CPU Limit | Memory Request | Memory Limit |
|-----------|-------------|-----------|----------------|--------------|
| API Gateway | 100m | 200m | 100Mi | 500Mi |
| Discovery Server | 100m | 200m | 100Mi | 500Mi |
| User Service | 100m | 200m | 200Mi | 400Mi |
| Posts Service | 100m | 200m | 200Mi | 400Mi |
| Connections Service | 100m | 200m | 200Mi | 400Mi |
| Notification Service | 100m | 200m | 200Mi | 400Mi |
| Kafka (per pod) | 100m | 200m | 512Mi | 1Gi |
| Neo4j | 100m | 200m | 600Mi | 4Gi |
| PostgreSQL DBs | 100m | 200m | 100Mi | 200Mi | [14](#0-13) [15](#0-14) [16](#0-15) 

## Notes

- The Docker Compose setup uses a shared network `connectly-network` for service communication [17](#0-16) 
- All PostgreSQL databases use the same credentials (vishesh/vishesh15th) in both Docker and K8s deployments [18](#0-17) 
- Kafka runs in KRaft mode (no Zookeeper) with 2 replicas in Kubernetes [19](#0-18) 
- Services use Spring profiles: default for Docker Compose, `k8s` for Kubernetes [20](#0-19) 
- The Kubernetes deployment uses StatefulSets for databases to ensure data persistence [7](#0-6) 
- Neo4j credentials are set to neo4j/password in both environments [21](#0-20)

### Citations

**File:** docker-compose.yml (L1-3)
```yaml
version: "3.8"

services:
```

**File:** docker-compose.yml (L5-21)
```yaml
  kafka:
    image: confluentinc/cp-kafka:7.7.0
    container_name: kafka
    networks:
      - connectly-network
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_CONTROLLER_QUORUM_VOTERS: "1@kafka:29093"
      KAFKA_LISTENERS: "PLAINTEXT://:9092,CONTROLLER://:29093"
      KAFKA_ADVERTISED_LISTENERS: "PLAINTEXT://kafka:9092"
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT"
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      CLUSTER_ID: "LZ9zT1d9T3m7Jr3vWg=="
    volumes:
      - kafka_data:/var/lib/kafka/data
```

**File:** docker-compose.yml (L38-86)
```yaml
  notification-db:
    image: postgres
    container_name: notification-db
    environment:
      - POSTGRES_DB=notificationDB
      - POSTGRES_USER=vishesh
      - POSTGRES_PASSWORD=vishesh15th
    networks:
      - connectly-network
    volumes:
      - notification-db-data:/var/lib/postgresql/data

  posts-db:
    image: postgres
    container_name: posts-db
    environment:
      - POSTGRES_DB=postsDB
      - POSTGRES_USER=vishesh
      - POSTGRES_PASSWORD=vishesh15th
    networks:
      - connectly-network
    volumes:
      - posts-db-data:/var/lib/postgresql/data

  user-db:
    image: postgres
    container_name: user-db
    environment:
      - POSTGRES_DB=userDB
      - POSTGRES_USER=vishesh
      - POSTGRES_PASSWORD=vishesh15th
    networks:
      - connectly-network
    volumes:
      - user-db-data:/var/lib/postgresql/data

  # 🕸 Neo4j for connections
  connections-db:
    image: neo4j
    container_name: connections-db
    environment:
      - NEO4J_AUTH=neo4j/password
    ports:
      - "7474:7474"
      - "7687:7687"
    volumes:
      - connections-db-data:/data
    networks:
      - connectly-network
```

**File:** docker-compose.yml (L89-95)
```yaml
  discovery-server:
    image: imvishesh15/connectly-app/discovery-server
    container_name: discovery-server
    networks:
      - connectly-network
    ports:
      - "8762:8761" # Changed to 8762 to avoid conflict with localhost 8761
```

**File:** docker-compose.yml (L98-136)
```yaml
  posts-service:
    image: imvishesh15/connectly-app/posts-service
    container_name: posts-service
    networks:
      - connectly-network
    depends_on:
      - discovery-server
      - posts-db
      - kafka

  user-service:
    image: imvishesh15/connectly-app/user-service
    container_name: user-service
    networks:
      - connectly-network
    depends_on:
      - discovery-server
      - user-db
      - kafka

  notification-service:
    image: imvishesh15/connectly-app/notification-service
    container_name: notification-service
    networks:
      - connectly-network
    depends_on:
      - discovery-server
      - notification-db
      - kafka

  connections-service:
    image: imvishesh15/connectly-app/connections-service
    container_name: connections-service
    networks:
      - connectly-network
    depends_on:
      - discovery-server
      - connections-db
      - kafka
```

**File:** docker-compose.yml (L138-146)
```yaml
  api-gateway:
    image: imvishesh15/connectly-app/api-gateway
    container_name: api-gateway
    ports:
      - "8080:8080"
    networks:
      - connectly-network
    depends_on:
      - discovery-server
```

**File:** docker-compose.yml (L149-150)
```yaml
networks:
  connectly-network:
```

**File:** k8s/notification-db.yml (L1-10)
```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: notification-db
spec:
  selector:
    matchLabels:
      app: notification-db
  serviceName: notification-db
  replicas: 1
```

**File:** k8s/connections-db.yml (L1-12)
```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: neo4j
  labels:
    app: neo4j
spec:
  replicas: 1
  selector:
    matchLabels:
      app: neo4j
  serviceName: "neo4j"
```

**File:** k8s/kafka.yml (L21-33)
```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  labels:
    app: kafka
  name: kafka
spec:
  podManagementPolicy: Parallel
  replicas: 2
  selector:
    matchLabels:
      app: kafka
  serviceName: kafka
```

**File:** k8s/kafka.yml (L69-75)
```yaml
        resources:
          limits:
            cpu: "200m"
            memory: 1Gi
          requests:
            cpu: 100m
            memory: 512Mi
```

**File:** k8s/kafkaui.yml (L1-11)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka-ui
  labels:
    app: kafka-ui
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka-ui
```

**File:** k8s/discovery-server.yml (L1-10)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: discovery-server
spec:
  selector:
    matchLabels:
      app: discovery-server
  template:
    metadata:
```

**File:** k8s/user-service.yml (L1-10)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
```

**File:** k8s/user-service.yml (L18-24)
```yaml
        resources:
          limits:
            memory: "400Mi"
            cpu: "200m"
          requests:
            memory: "200Mi"
            cpu: "100m"
```

**File:** k8s/api-gateway.yml (L1-10)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
```

**File:** k8s/api-gateway.yml (L17-23)
```yaml
        resources:
          limits:
              memory: "500Mi"
              cpu: "200m"
          requests:
            memory: "100Mi"
            cpu: "100m"
```

**File:** k8s/api-gateway.yml (L27-28)
```yaml
        - name: SPRING_PROFILES_ACTIVE
          value: k8s  
```
