# 🎬 Cinema System — Microservices Architecture

Hệ thống quản lý rạp chiếu phim được xây dựng theo kiến trúc **Microservices** kết hợp **Hexagonal Architecture**, sử dụng **Camunda BPM** cho điều phối giao dịch phân tán (Saga Orchestration) và tích hợp mã hoá end-to-end (AES + RSA).

---

## 📐 Kiến trúc tổng quan

```
┌──────────────────────────────────────────────────────────────┐
│                    Angular 20 Frontend                       │
│        (TailwindCSS · Angular Material · Ng-Zorro)           │
└─────────────────────────┬────────────────────────────────────┘
                          │ AES Encrypted HTTP
┌─────────────────────────▼────────────────────────────────────┐
│               cinema-gateway :8080                           │
│        (Spring Cloud Gateway · Dynamic CORS · Rate Limit)    │
├──────────┬──────────┬──────────┬──────────┬──────────────────┤
│ cinema-  │ cinema-  │ cinema-  │ cinema-  │ cinema-          │
│ iam      │ catalog  │ facility │ schedul- │ booking          │
│ :8081    │          │          │ ing      │ :8085            │
│          │          │          │          │ (Camunda BPM)    │
├──────────┴──────────┴──────────┴──────────┴──────────────────┤
│ cinema-admin :8086 (CRUD · JasperReports · Security Config)  │
├──────────────────────────────────────────────────────────────┤
│ cinema-notification (Kafka Consumer · Email · PDF Ticket)     │
└──────────┬──────────┬──────────┬──────────┬──────────────────┘
           │          │          │          │
    ┌──────▼──┐ ┌─────▼───┐ ┌───▼────┐ ┌──▼──────┐
    │Postgres │ │  Redis  │ │ Kafka  │ │Keycloak │
    │   16    │ │ 7-alpine│ │ 7.4.0  │ │ 26.2.5  │
    └─────────┘ └─────────┘ └────────┘ └─────────┘
```

---

## 🛠️ Tech Stack

| Layer | Công nghệ |
|---|---|
| **Backend** | Java 17, Spring Boot 3.2.4, Spring Cloud 2023.0.1 |
| **Frontend** | Angular 20.3, TailwindCSS 3.4, Angular Material, Ng-Zorro-Antd |
| **Database** | PostgreSQL 16 (multi-schema) |
| **Cache & Session** | Redis 7 |
| **Message Broker** | Apache Kafka (Confluent 7.4.0) |
| **Process Engine** | Camunda BPM 7 (Embedded) |
| **Identity** | Keycloak 26.2.5 + Custom SPI |
| **Payment** | VNPay Sandbox |
| **Observability** | Micrometer Tracing, OpenTelemetry, Jaeger |
| **Report** | JasperReports (PDF ticket, revenue reports) |
| **Logging** | Log4j2 (thay Logback) |
| **CI/CD** | GitHub Actions → Docker Hub |
| **Containerization** | Docker Compose (dev & production) |

---

## 📦 Cấu trúc project

```
sys_cinemas/
├── cinema-frontend/           # Angular 20 SPA
├── cinema-microservices/       # Spring Boot multi-module Maven
│   ├── cinema-common/          #   Shared filters, security, utils
│   ├── cinema-gateway/         #   API Gateway (Spring Cloud Gateway)
│   ├── auth-service/           #   IAM — xác thực, phân quyền, RSA, JWT
│   ├── booking-service/        #   Đặt vé, Camunda BPM, VNPay
│   └── management-service/     #   Catalog + Facility + Scheduling + Admin + Notification
├── cinema-keycloak-spi/        # Custom User Storage Provider cho Keycloak
├── keycloak-theme/             # Custom Keycloak login theme
├── docker-compose.yml          # Development environment
├── docker-compose.production.yml # Production (pull images from Docker Hub)
├── init-databases.sql          # Khởi tạo schemas PostgreSQL
├── .env.example                # Template biến môi trường (xem bên dưới)
└── .github/workflows/ci-cd.yml # CI/CD pipeline
```

---

## ⚡ Yêu cầu hệ thống

- **Java** 17+
- **Maven** 3.9+
- **Node.js** 20+ & npm 10+
- **Docker** & Docker Compose v2
- **Git**

---

## 🚀 Cài đặt & Chạy

### 1. Clone repository

```bash
git clone https://github.com/<your-username>/sys_cinemas.git
cd sys_cinemas
```

### 2. Cấu hình biến môi trường

```bash
cp .env.example .env
# Mở file .env và điền các giá trị thực tế
# Xem chi tiết tại: .env.example
```

> ⚠️ **KHÔNG BAO GIỜ** commit file `.env` lên Git. File đã được thêm vào `.gitignore`.

### 3. Khởi động Infrastructure (Docker)

```bash
# Chạy PostgreSQL, Redis, Kafka, Keycloak, Jaeger, MailHog
docker compose --profile infra up -d
```

### 4. Build & chạy Backend

```bash
cd cinema-microservices
mvn clean install -DskipTests

# Chạy từng service (hoặc dùng IDE)
cd auth-service && mvn spring-boot:run
cd ../booking-service && mvn spring-boot:run
cd ../management-service && mvn spring-boot:run
cd ../cinema-gateway && mvn spring-boot:run
```

### 5. Chạy Frontend

```bash
cd cinema-frontend
npm install
npm start
# Truy cập: http://localhost:4200
```

### 6. Chạy toàn bộ bằng Docker (tùy chọn)

```bash
# Chạy tất cả (infra + gateway + app services)
docker compose --profile all up -d
```

---

## 🌐 Ports & Services

| Service | Port | Mô tả |
|---|---|---|
| **Frontend** | `4200` | Angular dev server |
| **API Gateway** | `8080` | Entry point cho tất cả API |
| **Auth Service** | `8081` | IAM, JWT, RSA |
| **Booking Service** | `8085` | Đặt vé, Camunda, VNPay |
| **Management Service** | `8086` | Admin, Catalog, Facility, Scheduling, Notification |
| **Keycloak** | `8089` | Identity Provider (SSO) |
| **PostgreSQL** | `5432` | Database |
| **Redis** | `6379` | Cache & Session |
| **Kafka** | `9092` | Message Broker |
| **Jaeger UI** | `16686` | Distributed Tracing |
| **MailHog UI** | `8025` | Email testing (dev) |

---

## 🔐 Bảo mật

- **End-to-End Encryption**: Request/Response body được mã hoá AES giữa Frontend ↔ Backend
- **RSA Password Encryption**: Mật khẩu được mã hoá RSA trước khi gửi qua mạng
- **JWT + Single Session**: Mỗi user chỉ có 1 phiên hoạt động, token cũ tự động bị thu hồi qua Redis Blacklist
- **API Key Protection**: Public API yêu cầu `X-Client-Key`, Internal API yêu cầu `X-Internal-Api-Key`
- **Idempotency**: Header `X-Idempotency-Key` chống duplicate request
- **Distributed Lock**: Redis lock chống double booking (bán trùng ghế)

---

## 📋 Tài liệu bổ sung

- [Chi tiết kiến trúc & nghiệp vụ](PROJECT_OVERVIEW.md)
- [Hướng dẫn Camunda nâng cao](CAMUNDA_ADVANCED_GUIDE.md)
- [Hướng dẫn biến môi trường](.env.example)

---

## 📄 License

Private project — All rights reserved.
