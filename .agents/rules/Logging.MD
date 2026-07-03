---
trigger: model_decision
description: Áp dụng khi viết log (logger.info/error/warn/debug), xử lý exception cần log, hoặc cấu hình logging framework. Chuẩn hóa nội dung, level, và cấu trúc log.
---

Quy tắc Logging (Java/Spring Boot)

Coi logging là một "tính năng" của hệ thống, không phải thứ viết cho có. Rule này bổ trợ cho phần Exception Handling trong spring-boot.md và phần Observability trong microservices.md.

1. Dùng framework logging chuẩn, không dùng System.out.println

Luôn dùng SLF4J + Logback (mặc định trong Spring Boot) qua Logger/LoggerFactory, không dùng System.out.println/e.printStackTrace().
Khai báo logger theo chuẩn: private static final Logger log = LoggerFactory.getLogger(ClassName.class); (hoặc dùng Lombok @Slf4j).

2. Không log thông tin nhạy cảm

Tuyệt đối không log: password, token, API key, số thẻ, thông tin định danh cá nhân (PII) dạng thô.
Nếu cần log dữ liệu liên quan, phải mask trước:

java// Sai
log.error("Login failed for request: {}", request);

// Đúng
log.error("Login failed username={} password={}", request.getUsername(), "[HIDDEN]");

Với object phức tạp (DTO chứa field nhạy cảm), cân nhắc override toString() để tự động mask, tránh việc lỡ log nguyên object.

3. Message phải cụ thể, có ngữ cảnh — không log chung chung

Tránh message mơ hồ kiểu "Bắt đầu" / "Xong" không kèm thông tin gì.
Luôn kèm theo tham số liên quan: tên tác vụ, ID đối tượng, thời gian xử lý, kết quả.

java// Sai
log.info("Bắt đầu xử lý");
log.info("Xử lý xong");

// Đúng
log.info("Bắt đầu xử lý booking, bookingId={}, userId={}", bookingId, userId);
log.info("Xử lý booking hoàn tất, bookingId={}, durationMs={}", bookingId, duration);
4. Không log dư thừa, cồng kềnh

Chỉ log vừa đủ thông tin hữu ích cho việc debug/monitor — mỗi log là chi phí lưu trữ + xử lý ở hệ thống tập trung (ELK).
Không log trong vòng lặp lớn ở mức INFO trở lên (dùng DEBUG nếu thực sự cần, và tắt ở production).
Không log toàn bộ payload/object lớn (request body, response body đầy đủ) trừ khi ở mức DEBUG và có giới hạn kích thước.

5. Luôn log lỗi trước khi throw exception khác

Khi bắt exception và ném ra exception khác (wrap lại), phải log lỗi gốc trước — nếu không, log sau này sẽ mất dấu vết nguyên nhân thật sự.

javatry {
    paymentService.charge(order);
} catch (PaymentGatewayException ex) {
    log.error("Thanh toán thất bại orderId={}", order.getId(), ex);
    throw new BusinessException("Không thể xử lý thanh toán", ex);
}

Luôn truyền exception gốc (ex) làm tham số cuối để logger in ra đầy đủ stack trace.

6. Dùng đúng Log Level
LevelKhi nào dùngERRORLỗi ảnh hưởng luồng nghiệp vụ, cần chú ý ngay (exception, thất bại thao tác quan trọng)WARNTình huống bất thường nhưng hệ thống vẫn xử lý được (retry thành công, dùng fallback, deprecated API)INFOSự kiện nghiệp vụ quan trọng, mốc luồng xử lý chính (tạo booking, thanh toán thành công)DEBUGChi tiết kỹ thuật phục vụ debug, chỉ bật khi cần điều tra sự cốTRACEChi tiết cực nhỏ, gần như không dùng ở production

Production: chỉ bật INFO trở lên (cấu hình qua logback-spring.xml theo profile).
Dev/staging: có thể bật DEBUG để điều tra chi tiết.
Không dùng INFO để log lỗi, không dùng ERROR cho tình huống bình thường (validate fail do input sai của user thường chỉ nên là WARN, không phải ERROR).

7. Không dùng DEBUG level cho dữ liệu monitoring hệ thống

Metrics phục vụ giám sát hệ thống (latency, request count, health) chỉ cần INFO/ERROR — không phụ thuộc vào DEBUG log vì level này thường tắt ở production.
Dữ liệu phục vụ dashboard/alert nên đi qua Micrometer/Actuator (xem microservices.md mục Metrics), không parse từ log DEBUG.

8. Luôn có Trace ID / Correlation ID trong log

Mọi request phải mang theo traceId xuyên suốt các service (dùng Spring Cloud Sleuth / Micrometer Tracing hoặc MDC thủ công).
Cấu hình log pattern để tự in traceId trong mọi dòng log:

%d{yyyy-MM-dd HH:mm:ss} [%thread] [traceId=%X{traceId}] %-5level %logger{36} - %msg%n

Không log mà thiếu trace ID trong hệ thống phân tán — sẽ không thể nối lại luồng xử lý khi debug qua nhiều service.

9. Thiết lập chuẩn tối thiểu cho toàn hệ thống

Thống nhất format log JSON có cấu trúc (không log tự do dạng câu văn) để dễ query trên Kibana — liên kết mục Logging trong microservices.md.
Mọi log ở request boundary (Controller/Filter) tối thiểu phải có: traceId, endpoint, httpMethod, statusCode, durationMs.
Định nghĩa 1 LoggingFilter/Interceptor dùng chung để tự động log request/response ở mức đủ dùng, tránh mỗi Controller tự log theo cách khác nhau.


Tóm tắt: Log phải hữu ích, an toàn (không lộ dữ liệu nhạy cảm), đúng level, có ngữ cảnh cụ thể, không dư thừa, và luôn mang trace ID để debug được trong hệ thống phân tán.