---
trigger: glob
globs: **/controller/**", "**/*Controller.java", "**/*Resource.java", "**/*Api.java", "**/dto/**
---

---
trigger: glob
globs: ["**/controller/**", "**/*Controller.java", "**/*Resource.java", "**/*Api.java", "**/dto/**"]
description: "Chuẩn thiết kế RESTful API: URL, HTTP method/status, versioning, phân trang, bảo mật, testing, tài liệu hóa."
---

# Quy tắc viết RESTful API chuẩn (kết hợp Spring Boot)

## 1. Thiết kế URL & Resource
- Dùng **danh từ số nhiều** cho resource, không dùng động từ: `/users` thay vì `/getUsers`.
- Phân cấp resource rõ ràng: `/users/{id}/orders` thay vì nhồi tham số vào query string.
- Đặt tên endpoint nhất quán (snake_case/kebab-case cho path, camelCase cho JSON field — chọn 1 chuẩn và giữ xuyên suốt dự án).
- **Trong Spring Boot**: dùng `@RestController` + `@RequestMapping("/api/v1/users")` ở class level, method dùng `@GetMapping`, `@PostMapping`... theo đúng HTTP verb tương ứng hành động (không dùng GET để thực hiện thay đổi dữ liệu).

## 2. HTTP Method & Status Code
| Hành động | Method | Status thành công |
|---|---|---|
| Lấy danh sách/chi tiết | GET | 200 OK |
| Tạo mới | POST | 201 Created |
| Cập nhật toàn bộ | PUT | 200 OK |
| Cập nhật một phần | PATCH | 200 OK |
| Xóa | DELETE | 204 No Content |

- Status lỗi: `400` (dữ liệu không hợp lệ), `401` (chưa xác thực), `403` (không có quyền), `404` (không tìm thấy), `409` (conflict), `500` (lỗi server).
- **Trong Spring Boot**: trả status chính xác bằng `ResponseEntity<T>` hoặc `@ResponseStatus`. Xử lý lỗi tập trung bằng `@ControllerAdvice` + `@ExceptionHandler` — map custom exception sang đúng status code, không để lộ stack trace.

## 3. Versioning
- Ưu tiên versioning qua URL: `/api/v1/users` — dễ debug, dễ cache, dễ đọc log hơn so với version qua header.
- Nếu dùng header: `Accept: application/vnd.yourapi.v1+json`.
- Không phá vỡ API cũ khi ra version mới; giữ song song v1 và v2 trong thời gian deprecation.

## 4. Định dạng dữ liệu (Content Negotiation)
- Mặc định JSON; hỗ trợ thêm XML nếu cần qua `Content-Type`/`Accept` header.
- **Spring Boot** tự xử lý content negotiation qua `HttpMessageConverter` — chỉ cần khai đúng `produces`/`consumes` trong `@RequestMapping` khi cần giới hạn định dạng.
- Dùng DTO (Data Transfer Object) riêng cho request/response, không expose trực tiếp Entity JPA ra ngoài — tránh lộ cấu trúc DB và vòng lặp lazy-loading khi serialize.

## 5. Filtering, Sorting, Pagination
- Chuẩn hóa query param: `/users?page=2&size=10&sort=name,asc`.
- **Spring Boot**: dùng `Pageable` (Spring Data) làm tham số controller, trả về `Page<T>` hoặc map sang DTO response có `content`, `totalElements`, `totalPages`.
- Giới hạn `size` tối đa để tránh client lấy toàn bộ dữ liệu một lần (bảo vệ hiệu năng, liên kết với phần tối ưu query N+1 trong rule Spring Boot).

## 6. Authentication & Authorization
- Dùng OAuth2/JWT thay vì Basic Auth cho production.
- **Spring Boot**: cấu hình `SecurityFilterChain`, dùng `hasRole`/`hasAuthority` để phân quyền theo endpoint; validate JWT ở filter riêng hoặc dùng `spring-boot-starter-oauth2-resource-server`.
- Không bao giờ trả thông tin nhạy cảm (password, token nội bộ) trong response body.

## 7. Đặt tên & tính nhất quán
- Field JSON đặt tên nhất quán (camelCase khuyến nghị cho JSON).
- Response lỗi nên có cấu trúc chuẩn dùng chung toàn hệ thống, ví dụ:
```json
{
  "timestamp": "2026-07-02T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found",
  "path": "/api/v1/users/123"
}
```
- Định nghĩa 1 class `ErrorResponse` dùng chung trong `@ControllerAdvice`.

## 8. HATEOAS (tùy chọn)
- Với API phức tạp cần client tự khám phá hành động khả dụng, cân nhắc dùng `spring-hateoas` để thêm `_links` vào response (self, next, prev...).
- Không bắt buộc với API nội bộ đơn giản — cân nhắc chi phí phức tạp thêm vs lợi ích thực tế trước khi áp dụng.

## 9. Testing API
- Dùng `@WebMvcTest` + `MockMvc` để test controller layer độc lập (mock service).
- Dùng `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`/`WebTestClient` cho integration test full flow.
- Test cả các case lỗi (400/401/403/404) không chỉ happy path.

## 10. Tài liệu hóa API
- Dùng OpenAPI/Swagger (`springdoc-openapi`) để tự sinh tài liệu API, giữ đồng bộ với code qua annotation `@Operation`, `@Parameter`, `@Schema`.

---

**Nguyên tắc cốt lõi:** URL là danh từ mô tả resource, HTTP method/status code phản ánh đúng ngữ nghĩa hành động, tách DTO khỏi Entity, bảo mật bằng token-based auth, và luôn có versioning + tài liệu hóa để API dễ bảo trì lâu dài.