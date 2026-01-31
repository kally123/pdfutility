# PDF Utility Enterprise Platform

A comprehensive enterprise-grade PDF utility platform with microservices architecture, featuring PDF merge, edit, and compress capabilities.

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           Frontend Layer                                 │
├───────────────────────────────┬─────────────────────────────────────────┤
│     Next.js Web Portal        │        React Native Mobile App          │
│     (Port: 3000)              │        (iOS & Android)                  │
└───────────────────────────────┴─────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         API Gateway Service                              │
│                         (Port: 8080)                                    │
│           - Rate Limiting, Authentication, Load Balancing               │
└─────────────────────────────────────────────────────────────────────────┘
                                        │
            ┌───────────────────────────┼───────────────────────────┐
            ▼                           ▼                           ▼
┌─────────────────────┐   ┌─────────────────────┐   ┌─────────────────────┐
│   Auth Service      │   │   PDF Core Service  │   │   Storage Service   │
│   (Port: 8081)      │   │   (Port: 8082)      │   │   (Port: 8083)      │
│                     │   │                     │   │                     │
│   - JWT Auth        │   │   - PDF Merge       │   │   - File Upload     │
│   - OAuth 2.0       │   │   - PDF Edit        │   │   - Azure Blob      │
│   - User Mgmt       │   │   - PDF Compress    │   │   - S3 Compatible   │
│   - RBAC            │   │   - PDF Convert     │   │   - Temp Storage    │
└─────────────────────┘   └─────────────────────┘   └─────────────────────┘
            │                         │                         │
            └─────────────────────────┼─────────────────────────┘
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        Message Broker (Kafka)                            │
│                        Event-Driven Architecture                         │
└─────────────────────────────────────────────────────────────────────────┘
                                      │
            ┌─────────────────────────┼─────────────────────────┐
            ▼                         ▼                         ▼
┌─────────────────────┐   ┌─────────────────────┐   ┌─────────────────────┐
│   PostgreSQL        │   │   Redis Cache       │   │   Azure Blob /      │
│   (R2DBC)           │   │   (Distributed)     │   │   MinIO Storage     │
└─────────────────────┘   └─────────────────────┘   └─────────────────────┘
```

## 🚀 Tech Stack

### Backend Services (Java 21 + Spring Boot 3.3.x)
- **Spring WebFlux** - Reactive, non-blocking REST APIs
- **R2DBC** - Reactive database connectivity (PostgreSQL)
- **Spring Security** - JWT & OAuth 2.0 authentication
- **Apache PDFBox** - PDF manipulation library
- **Apache Kafka** - Event-driven messaging
- **Redis** - Distributed caching
- **Docker** - Containerization

### Frontend (Next.js 14)
- **React 18** - UI library
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **React Query** - Server state management
- **Zustand** - Client state management

### Mobile (React Native)
- **Expo** - Development framework
- **TypeScript** - Type safety
- **React Navigation** - Navigation
- **React Query** - Data fetching

## 📁 Project Structure

```
pdfutility/
├── backend/
│   ├── gateway-service/          # API Gateway
│   ├── auth-service/             # Authentication & Authorization
│   ├── pdf-core-service/         # PDF Operations
│   ├── storage-service/          # File Storage
│   └── common/                   # Shared libraries
├── frontend/
│   └── web-portal/               # Next.js Application
├── mobile/
│   └── pdf-utility-app/          # React Native App
├── docker/
│   └── docker-compose.yml        # Container orchestration
└── docs/
    └── api/                      # API Documentation
```

## 🛠️ Prerequisites

- **Java 21** (LTS)
- **Maven 3.9+**
- **Node.js 20+**
- **Docker & Docker Compose**
- **PostgreSQL 15+**
- **Redis 7+**
- **Apache Kafka 3.5+**

## 🏃‍♂️ Quick Start

### 1. Start Infrastructure
```bash
cd docker
docker-compose up -d
```

### 2. Build Backend Services
```bash
cd backend
mvn clean install -DskipTests
```

### 3. Run Backend Services
```bash
# Start each service in separate terminals
cd backend/gateway-service && mvn spring-boot:run
cd backend/auth-service && mvn spring-boot:run
cd backend/pdf-core-service && mvn spring-boot:run
cd backend/storage-service && mvn spring-boot:run
```

### 4. Start Frontend
```bash
cd frontend/web-portal
npm install
npm run dev
```

### 5. Start Mobile (Development)
```bash
cd mobile/pdf-utility-app
npm install
npx expo start
```

## 📚 API Documentation

API documentation is available via Swagger UI:
- Gateway: http://localhost:8080/swagger-ui.html
- Auth Service: http://localhost:8081/swagger-ui.html
- PDF Service: http://localhost:8082/swagger-ui.html
- Storage Service: http://localhost:8083/swagger-ui.html

## 🔐 Security

- JWT-based authentication with refresh tokens
- OAuth 2.0 integration (Google, Microsoft)
- Role-Based Access Control (RBAC)
- Rate limiting and DDoS protection
- Encrypted file storage
- CORS configuration

## 📊 Features

### PDF Operations
- **Merge** - Combine multiple PDFs into one
- **Split** - Extract pages from PDF
- **Compress** - Reduce PDF file size
- **Convert** - PDF to/from images
- **Edit** - Add text, images, watermarks
- **Rotate** - Rotate PDF pages
- **Password** - Protect/unlock PDFs
- **OCR** - Extract text from scanned PDFs

### Enterprise Features
- Multi-tenant support
- Audit logging
- Usage analytics
- Batch processing
- Webhook integrations
- Custom branding

## 🧪 Testing

```bash
# Backend unit tests
cd backend && mvn test

# Backend integration tests
cd backend && mvn verify -P integration-tests

# Frontend tests
cd frontend/web-portal && npm test

# Mobile tests
cd mobile/pdf-utility-app && npm test
```

## 📦 Deployment

### Docker
```bash
docker-compose -f docker/docker-compose.prod.yml up -d
```

### Kubernetes
```bash
kubectl apply -f k8s/
```

## 📄 License

MIT License - see LICENSE file for details.
