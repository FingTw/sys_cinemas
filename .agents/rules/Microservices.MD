---
trigger: model_decision
description: Áp dụng khi thiết kế/triển khai kiến trúc microservice: API Gateway, Service Discovery, giao tiếp giữa service, database per service, caching, messaging, observability, bảo mật.
---

Quy tắc kiến trúc Microservice (Production-grade)

Dựa trên 9 thành phần thiết yếu của một hệ microservice production. Rule này bổ trợ cho spring-boot.md và rest-api.md — dùng chung khi service giao tiếp qua REST.

1. API Gateway

Tất cả traffic từ client đi qua 1 điểm vào duy nhất (Spring Cloud Gateway / Kong / Nginx) — không để client gọi thẳng vào từng service.
Gateway chỉ xử lý: routing, rate limiting, request/response transformation, xác thực token ở tầng biên. Không nhét business logic vào gateway.
Cấu hình timeout & retry riêng cho từng route, có fallback khi service downstream lỗi.

2. Service Registry & Discovery

Mỗi service tự đăng ký với registry (Eureka / Consul) khi start, deregister khi shutdown gracefully.
Không hardcode IP/port của service khác trong code — luôn resolve qua service name (service-a, không phải 10.0.0.5:8080).
Cấu hình health check để registry loại bỏ instance chết khỏi danh sách route.

3. Service Layer — ranh giới giữa các service

Mỗi service sở hữu 1 bounded context rõ ràng (theo Domain-Driven Design) — không chia service theo tầng kỹ thuật (vd: tách riêng "service DB" và "service logic" là sai).
Giao tiếp đồng bộ giữa service: dùng REST/gRPC qua interface rõ ràng, có version (liên kết rest-api.md).
Tránh gọi chuỗi đồng bộ nhiều tầng (A→B→C→D) — dễ gây cascading failure; ưu tiên async qua message queue khi có thể.
Dùng Circuit Breaker (Resilience4j) cho mọi lời gọi liên service — không để 1 service chậm kéo sập toàn hệ thống.

4. Authorization Server

Xác thực tập trung 1 nơi (Keycloak / Auth0 / custom OAuth2 server) — service con chỉ validate token, không tự xử lý login.
Dùng JWT có scope/claim rõ ràng cho từng service; access token thời gian sống ngắn, kèm refresh token riêng.
Service-to-service call (không qua user) dùng client credentials flow, không dùng chung token của user.

5. Database Layer

Database per service — mỗi service có DB riêng, không share schema giữa 2 service khác nhau.
Không query trực tiếp DB của service khác — phải qua API hoặc event.
Có chiến lược replication (master-replica) cho service đọc nhiều; đọc từ replica, ghi vào master.
Đồng bộ dữ liệu giữa các service qua event-driven (Saga pattern) thay vì distributed transaction (2PC) — tránh khóa xuyên service.

6. Distributed Cache

Dữ liệu đọc nhiều, ít đổi (session, config, lookup table) → cache ở tầng chung (Redis) thay vì cache local từng instance (tránh cache không đồng bộ).
Luôn đặt TTL rõ ràng, không cache vô thời hạn.
Có chiến lược invalidate cache khi dữ liệu gốc thay đổi (event-based invalidation, không chỉ dựa vào TTL).

7. Distributed Messaging

Giao tiếp bất đồng bộ giữa service (Kafka/RabbitMQ) cho các việc: đồng bộ dữ liệu, gửi notification, xử lý nền.
Message phải idempotent-safe ở consumer — cùng 1 message xử lý 2 lần không được gây lỗi dữ liệu.
Dùng Dead Letter Queue (DLQ) cho message xử lý lỗi liên tục, không để consumer retry vô hạn chặn queue.
Không dùng message queue để thay thế cho request-response cần trả kết quả ngay (dùng REST/gRPC cho case đó).

8. Observability — Metrics

Mỗi service expose metrics chuẩn (Micrometer → Prometheus): request count, latency, error rate, resource usage.
Dùng Actuator (/actuator/prometheus) cho service Spring Boot — liên kết phần Actuator trong spring-boot.md.
Dashboard tập trung (Grafana), có alert khi latency/error rate vượt ngưỡng — không chỉ xem log tay khi có sự cố.

9. Observability — Logging

Log tập trung qua pipeline (Logstash → Elasticsearch → Kibana), không để log rải rác từng server riêng lẻ.
Log dạng structured (JSON), không log plain text tự do — dễ query/filter trên Kibana.
Luôn gắn correlation ID / trace ID xuyên suốt 1 request qua nhiều service (dùng Sleuth/OpenTelemetry) để trace được toàn bộ luồng khi debug.
Không log thông tin nhạy cảm (password, token, dữ liệu cá nhân) — mask trước khi ghi log.

Nguyên tắc chung khi code cho microservice

Design for failure: mọi lời gọi ra ngoài service (DB, service khác, cache, queue) phải có timeout, retry có giới hạn, và fallback.
Stateless service: không lưu state trong memory của instance — mọi state cần chia sẻ phải nằm ở DB/cache/queue, để scale ngang tự do.
Contract-first: định nghĩa API contract (OpenAPI) hoặc message schema trước khi code, để các team/service khác không bị phá vỡ khi thay đổi.
Config theo môi trường, không build lại image khi đổi config (12-factor app).
Mỗi service có pipeline CI/CD, test, và deploy độc lập — không để việc deploy service này phụ thuộc vào service khác.