# Cinema System - Tổng quan Công nghệ & Kiến trúc Hệ thống

Tài liệu này cung cấp cái nhìn toàn diện về bối cảnh công nghệ, kiến trúc Hexagonal và các quy tắc triển khai chi tiết của dự án Quản lý Rạp chiếu phim.

---

## 1. Bối cảnh Công nghệ (Technology Stack)

### Backend (Java / Spring Boot)

- **Core:** Java 17, Spring Boot 4.x.
- **Kiến trúc:** Clean Architecture (Hexagonal Architecture).
- **Security:** Spring Security, JWT, RSA Encryption (cho Password/Token), **X-API-KEY**, **2FA**.
- **Database:** PostgreSQL 16 (Sử dụng schema riêng biệt: `auth`, `catalog`, `booking`... không dùng schema `public`).
- **Caching:** Redis (Quản lý Online status, Blacklist Token, Role cache, Session).
- **Migration:** Flyway (Quản lý schema database).
- **Mapping:** Sử dụng **Object Mapper** (MapStruct) để chuyển đổi giữa các lớp dữ liệu.
- **Observability:** OpenTelemetry Java Agent, **Log4j2** với Custom XML layout (thay thế Logback).

### Frontend (Angular)

- **Core:** Angular 17+, TypeScript.
- **Styling:** Vanilla CSS / SCSS (Giao diện Premium/Modern).
- **State Management:** Reactive Programming với RxJS.
- **Security:** Mã hóa Token lưu trữ phía Client, mã hóa Request/Response.

### Infrastructure (DevOps)

- **Containerization:** Docker & Docker Compose với cấu hình `deploy.resources.limits`.
- **Auth Provider:** Keycloak (Hệ thống định danh trung gian, không dùng DB Keycloak lưu dữ liệu người dùng).
- **Environment:** Spring Boot Profiles & Environment Variables (dev, prod).

---

## 2. Quy tắc Triển khai & Thiết kế (Implementation Rules)

- **Service Pattern:** Không viết logic vào một file Service duy nhất; sử dụng mô hình **Interface - Implementation** (`Port` và `ServiceImplement`).
- **Idempotency:** Triển khai cơ chế chống trùng lặp cho các tác vụ quan trọng (đặt vé, thanh toán).
- **Logging Standards:**
  - Log chi tiết giữa các tầng để theo dõi debug.
  - Ẩn thông tin nhạy cảm (Password, Token) trong log.
  - Log chi tiết **thời gian phản hồi (Response Time)** của mỗi API.
- **Localization:** Múi giờ đồng nhất `Asia/Ho_Chi_Minh` trên toàn hệ thống.
- **RBAC (Role-Based Access Control):** 1 User có thể có nhiều Role.
- **Single Session:** Mỗi tài khoản chỉ được phép có 1 phiên đăng nhập duy nhất tại một thời điểm.

### 2.1 RBAC & Permission Mapping

- **Role mặc định:** `USER`, cấp quyền sử dụng chức năng mua vé, kiểm tra lịch chiếu, xem trạng thái ghế.
- **Role quản trị:** `ADMIN` và `STAFF`, cấp quyền truy cập các endpoint quản lý admin.
- **Format quyền:** Mỗi role được ánh xạ thành authority JWT theo cú pháp `ROLE_<ROLE_NAME>`.
- **Role policies hiện tại:** `SecurityConfig` cho phép cả `ROLE_admin`, `ROLE_ADMIN`, `ROLE_STAFF` truy cập `/api/v1/admin/**`.

#### 2.1.1 Quyền hạn theo nhóm endpoint

- Public (không cần authentication):
  - `POST /api/v1/auth/login`
  - `POST /api/v1/auth/register`
  - `GET /api/v1/auth/public-key`
  - `GET /api/v1/movies/**`
  - `GET /api/v1/showtimes/**`
  - `POST /api/v1/vnpay/**`
  - `/error`
- Authenticated user:
  - `GET /api/v1/auth/me`
  - `POST /api/v1/auth/logout`
  - `POST /api/v1/bookings`
  - Các API khác không thuộc `/api/v1/admin/**` và không công khai.
- Admin / Staff only:
  - `GET|POST|PUT|DELETE /api/v1/admin/movies/**`
  - `GET|POST|PUT|DELETE /api/v1/admin/showtimes/**`
  - `GET|POST|PUT|DELETE /api/v1/admin/facilities/**`
  - `GET|PUT /api/v1/admin/users/**`

#### 2.1.2 Ma trận quyền chi tiết

- `USER`:
  - đọc: phim, lịch chiếu, sơ đồ ghế, thông tin session hiện tại
  - tạo: đặt vé, đăng ký tài khoản, đăng nhập/đăng xuất
  - sửa/xóa: không có quyền admin để thay đổi dữ liệu hệ thống
- `STAFF` / `ADMIN`:
  - đọc: danh sách người dùng, phim, suất chiếu, phòng, ghế
  - tạo: phim mới, suất chiếu mới, phòng chiếu mới
  - sửa: cập nhật phim, hủy suất chiếu, cập nhật ghế, đổi role người dùng, khóa người dùng
  - xóa: xóa phim, xóa suất chiếu, xóa phòng, xóa người dùng/role nếu có mở rộng sau này

#### 2.1.3 Nguyên tắc mở rộng RBAC

- Mỗi service/endpoint cần xác định rõ hành vi `read`, `write`, `update`, `delete`.
- Nếu mở rộng quyền chi tiết, nên phân tách thành `authority` phụ như `MOVIE_CREATE`, `SHOWTIME_CANCEL`, `USER_MANAGE`.
- Luôn ưu tiên kiểm tra Authorization ở lớp Presentation/Controller và tầng Service để tránh bypass.

---

## 3. Kiến trúc Hệ thống (Architectural Layers)

Hệ thống tuân thủ nghiêm ngặt mô hình 4 lớp:

1.  **Domain (Lõi):** Chứa Entities nghiệp vụ thuần túy, không phụ thuộc Framework.
2.  **Application (Điều phối):** Chứa Use Cases, Ports (Interface) và DTOs.
3.  **Infrastructure (Hạ tầng):** Chứa Adapters (Postgres, Redis, JWT, VNPay), Configs.
4.  **Presentation (Giao tiếp):** REST Controllers, Global Exception Handlers.

---

## 4. Luồng Xác thực & Hoạt động (System Flow)

### Luồng Xác thực qua Keycloak

1.  **FE:** Gửi tài khoản/mật khẩu xuống **BE**.
2.  **BE:** Chuyển tiếp sang **Keycloak**.
3.  **Keycloak (Custom Provider):** Kiểm tra xác thực qua DB/Remote của hệ thống, trả kết quả về BE.
4.  **BE:** Nhận OK, tạo **JWT Token** (chỉ chứa username, role định danh).
5.  **Persistence:** Lưu Token vào **Database** và **Redis** để quản lý phiên.
6.  **FE:** Nhận Token và mã hóa khi lưu trữ.

### Luồng Xử lý Request

1.  **Request:** Đi kèm Token và **x-api-key**.
2.  **Security Filter:** Kiểm tra tính hợp lệ, so khớp Token trong DB/Cache.
3.  **Validation:** Kiểm tra phiên đăng nhập duy nhất (nếu đăng nhập chỗ khác, Token cũ bị "cắm cờ" vô hiệu hóa).
4.  **Execution:** Thực thi logic qua các tầng, log thời gian phản hồi và trả kết quả mã hóa về FE.

---

## 5. Cấu trúc thư mục dự án

```bash
cinema/
├── src/main/java/com/example/cinema/
│   ├── domain/             # Entities, Repositories (Interfaces)
│   ├── application/        # UseCases (Ports & Implements), DTOs
│   ├── infrastructure/     # Database Adapters, Redis, Security, External
│   └── presentation/       # Controllers, Exception Handlers
├── src/main/resources/     # application.yaml, Log4j2.xml, SQL Migrations
└── .env                    # Biến môi trường bảo mật
```

---

_Tài liệu này được cập nhật đầy đủ theo các yêu cầu kỹ thuật của dự án Cinema._
