---
trigger: model_decision
description: Áp dụng khi thiết kế cấu trúc package/module mới, hoặc khi cần quyết định 1 class/logic nên đặt ở layer nào theo Clean Architecture (Entities, Use Case, Interface Adapter, Framework & Drivers).
---

Quy tắc Clean Architecture (áp dụng cho backend Java/Spring Boot)

Nguyên tắc cốt lõi: các layer bên trong (business logic) không được biết gì về layer bên ngoài (framework, DB, web). Dependency luôn hướng vào trong. Rule này bổ trợ cho spring-boot.md, rest-api.md, microservices.md.

4 layer theo Dependency Rule (trong → ngoài)
1. Entities (Domain Model) — layer trong cùng

Chứa business logic thuần túy, mô hình hóa nghiệp vụ cốt lõi (vd: Movie, Booking, User).
Là POJO thuần Java — không annotation của Spring/JPA/Jackson, không import bất kỳ framework nào.
Có thể chạy độc lập trên JVM thường, test được mà không cần Spring context, không cần mock DB.

2. Use Case (Application Business Rules)

Chứa logic nghiệp vụ cụ thể của ứng dụng (vd: CreateBookingUseCase, CancelReservationUseCase).
Định nghĩa interface (port) cho những gì cần từ bên ngoài (vd: BookingRepository interface) — không phụ thuộc implementation cụ thể.
Không biết HTTP là gì, không biết JPA/Hibernate là gì — chỉ gọi qua interface đã định nghĩa.
Đây là nơi đặt hầu hết unit test — test được toàn bộ mà không cần Spring context, DB thật, hay mock HTTP.

3. Interface Adapter

Chuyển đổi dữ liệu giữa Use Case và thế giới bên ngoài:

Controller: nhận HTTP request, map sang input của Use Case, không chứa business logic.
Presenter/Mapper: convert Entity/domain model ↔ DTO (request/response) — liên kết quy tắc DTO trong rest-api.md.
Repository Implementation: implement interface đã định nghĩa ở Use Case layer, dùng Spring Data JPA/JDBC thật.


Layer này được phép biết cả domain (bên trong) lẫn framework (bên ngoài), vì nhiệm vụ của nó là kết nối 2 phía.

4. Framework & Drivers — layer ngoài cùng

Chứa mọi thứ thuộc về công nghệ cụ thể: Spring Boot config, JPA Entity annotation, DB driver, message broker client, web server config.
Thay đổi framework/DB ở layer này không được ảnh hưởng tới business logic ở layer trong.
Thường không cần viết nhiều logic ở đây — chủ yếu là config, wiring, và implementation chi tiết.

Cấu trúc package đề xuất (Java/Spring Boot)
com.example.cinema
├── domain/                    # Entities — POJO thuần, không phụ thuộc framework
│   ├── Movie.java
│   └── Booking.java
├── application/                # Use Case + Port (interface)
│   ├── usecase/
│   │   ├── CreateBookingUseCase.java
│   │   └── CancelBookingUseCase.java
│   └── port/
│       ├── BookingRepositoryPort.java   # interface, chưa implement
│       └── NotificationPort.java
├── adapter/                    # Interface Adapter
│   ├── web/
│   │   ├── BookingController.java
│   │   └── dto/
│   │       ├── BookingRequest.java
│   │       └── BookingResponse.java
│   ├── persistence/
│   │   ├── BookingRepositoryImpl.java   # implement BookingRepositoryPort
│   │   └── entity/
│   │       └── BookingJpaEntity.java    # có @Entity, khác với domain.Booking
│   └── messaging/
│       └── NotificationAdapter.java
└── infrastructure/              # Framework & Drivers
    ├── config/
    │   ├── SecurityConfig.java
    │   └── BeanConfig.java
    └── CinemaApplication.java   # @SpringBootApplication
Quy tắc bắt buộc khi code theo kiến trúc này

Không import annotation Spring/JPA (@Entity, @Service, @Repository, @Autowired...) vào package domain/. Nếu cần class Entity JPA riêng cho persistence, tạo class riêng ở adapter/persistence/entity/ và map qua lại bằng mapper — không dùng chung 1 class cho cả domain model lẫn JPA entity.
Use Case không gọi trực tiếp Repository implementation — chỉ gọi qua interface (port) đã định nghĩa ở application/port/. Việc wiring implementation cụ thể do Spring DI xử lý (@Bean/@Service implement interface).
Controller không chứa business logic — chỉ nhận request, gọi Use Case, map response. Nếu thấy if/else xử lý nghiệp vụ trong Controller → chuyển vào Use Case.
Dependency chỉ được trỏ vào trong: infrastructure → adapter → application → domain. Không bao giờ ngược lại. Nếu domain cần import gì từ adapter/infrastructure → thiết kế sai, cần dừng và refactor.
Khi viết unit test cho Use Case, không cần @SpringBootTest — chỉ cần mock interface (port) bằng Mockito, test chạy nhanh và độc lập hoàn toàn với framework.

Khi nào KHÔNG cần áp dụng đầy đủ 4 layer

Với CRUD đơn giản, ít business logic thực sự (chỉ map thẳng request → DB) → áp Clean Architecture đầy đủ có thể overkill (liên kết nguyên tắc YAGNI trong minimal-code.md).
Cân nhắc dùng cấu trúc rút gọn (Controller → Service → Repository) cho module đơn giản, và chỉ tách đủ 4 layer cho module có nghiệp vụ phức tạp, nhiều rule, cần test kỹ.
Không tạo interface/port cho những chỗ chỉ có và sẽ luôn chỉ có 1 implementation duy nhất, trừ khi thực sự cần test double.


Tóm tắt: Business logic (domain + use case) độc lập hoàn toàn với framework và DB. Interface Adapter là lớp trung gian chuyển đổi dữ liệu 2 chiều. Dependency luôn hướng vào trong. Chỉ áp dụng đầy đủ khi nghiệp vụ đủ phức tạp để việc tách layer mang lại giá trị thực sự.