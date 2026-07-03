---
trigger: glob
globs: ["**/*.java", "**/pom.xml", "**/build.gradle", "**/application*.yml", "**/application*.properties"]
description: "Quy tắc code chuẩn cho dự án Spring Boot: IoC/DI, cấu hình, dependency, database, testing, bảo mật, hiệu năng."
---

# Quy tắc code chung cho dự án Spring Boot

## 1. IoC/DI & AOP
- Ưu tiên **constructor injection** thay vì field/setter injection để đảm bảo tính bất biến và dễ test.
- Hiểu rõ bean lifecycle và scope (singleton, prototype, request, session) trước khi tùy chỉnh.
- Dùng AOP có chọn lọc (logging, transaction, security) — tránh lạm dụng khiến luồng code khó theo dõi.

## 2. Cấu trúc & cấu hình dự án
- Dùng đúng `spring-boot-starter-*` thay vì thêm thư viện lẻ.
- Dùng Spring Profiles (`application-{profile}.yml`) để tách biệt môi trường dev/test/staging/prod.
- Ưu tiên `.yml` hơn `.properties` cho cấu hình phân cấp phức tạp.
- Không hardcode cấu hình — externalize qua biến môi trường hoặc Config Server.
- Dùng `@ConfigurationProperties` (thay vì nhiều `@Value`) để binding type-safe.

## 3. Quản lý Dependency
- Dùng Spring Boot/Cloud **BOM** để đồng bộ version, tránh dependency hell.
- Định kỳ chạy `mvn dependency:tree` / `gradle dependencies` để dọn dependency thừa và phát hiện xung đột.

## 4. Database & Transaction
- Đặt `@Transactional` ở tầng **Service**, không đặt ở Repository.
- Cẩn thận proxy issue khi gọi method transactional từ trong cùng class.
- Theo dõi và tránh **N+1 query** (dùng fetch join / `@EntityGraph`).
- Dùng JPQL/Native Query/Criteria API khi query mặc định của Spring Data JPA không tối ưu.
- Bật SQL logging khi cần debug hiệu năng truy vấn.

## 5. Exception Handling
- Xử lý lỗi tập trung bằng `@ControllerAdvice` + `@ExceptionHandler`.
- Không để lộ stack trace/thông tin nhạy cảm ra response.
- Định nghĩa custom exception có ý nghĩa nghiệp vụ rõ ràng.

## 6. Testing
- **Unit test**: cô lập logic, dùng `@MockBean`/Mockito.
- **Integration test**: dùng `@SpringBootTest` (toàn bộ context) hoặc slice test (`@DataJpaTest`, `@WebMvcTest`...) để chạy nhanh hơn.
- Dùng **Testcontainers** cho test cần DB/message queue thật, đảm bảo môi trường test nhất quán.

## 7. Bảo mật
- Cấu hình rõ ràng `SecurityFilterChain` (permitAll, authenticated, hasRole...).
- Chọn cơ chế auth phù hợp: Form Login / Basic Auth / OAuth2-OIDC / JWT.
- Không bao giờ lưu password dạng plain text — luôn dùng `PasswordEncoder` (BCrypt).
- Theo dõi và cập nhật kịp thời các bản vá lỗ hổng của Spring Security.

## 8. Giám sát & hiệu năng
- Bật Spring Boot **Actuator** (health, metrics, info, env) và tích hợp Prometheus/Grafana.
- Dùng profiler (VisualVM, JProfiler) khi cần tìm bottleneck.
- Áp dụng caching (Spring Cache Abstraction + Redis/Caffeine/EhCache) cho dữ liệu ít thay đổi hoặc tác vụ tốn tài nguyên.

---

**Nguyên tắc cốt lõi:** hiểu bản chất framework thay vì chỉ dùng annotation "ma thuật", tách biệt rõ các concern (config/service/security/exception), test đầy đủ ở nhiều tầng, và luôn giám sát hiệu năng/bảo mật trong suốt vòng đời ứng dụng.