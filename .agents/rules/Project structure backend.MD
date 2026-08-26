---
trigger: model_decision
description: Áp dụng project/service, hoặc khi quyết định 1 file/folder mới nên đặt ở đâu trong cấu trúc thư mục backend. Chuẩn Clean Architecture folder structure, áp dụng cho mọi ngôn ngữ backend trong hệ thống
---

Quy tắc cấu trúc thư mục Backend (Clean Architecture — đa ngôn ngữ)

Đây là chuẩn thư mục tổng quát, dùng làm khung tham chiếu chung cho mọi service backend trong hệ thống, bất kể viết bằng ngôn ngữ nào. Với service Java/Spring Boot cụ thể, xem thêm clean-architecture.md để có mapping chi tiết theo package Java (domain/application/adapter/infrastructure) — 2 rule dùng chung 1 tư tưởng, khác nhau ở tên thư mục do khác convention ngôn ngữ.

Cấu trúc chuẩn
project/
├── src/
│   ├── domain/                # core — không phụ thuộc gì cả
│   │   ├── entities/
│   │   ├── value_objects/
│   │   └── interfaces/
│   │
│   ├── application/            # → domain
│   │   ├── use_cases/
│   │   ├── dtos/
│   │   ├── interfaces/
│   │   └── validators/
│   │
│   ├── infrastructure/         # → domain
│   │   ├── persistence/
│   │   ├── repositories/
│   │   └── services/
│   │
│   └── presentation/            # → application
│       ├── controllers/
│       ├── middleware/
│       └── main
│
└── tests/
    ├── unit/
    └── integration/
Dependency Rule (bắt buộc, không có ngoại lệ)
presentation → application → domain
infrastructure → domain

domain là core, không import bất kỳ thứ gì từ 3 layer còn lại — không framework, không DB driver, không HTTP.
application chỉ được phép phụ thuộc vào domain (qua interface đã định nghĩa ở đó).
infrastructure implement các interface được định nghĩa ở domain/application, không định nghĩa business logic riêng.
presentation chỉ gọi vào application (use case), không gọi thẳng infrastructure hay domain.

Vai trò từng thư mục
Thư mụcChứa gìKhông được chứadomain/entitiesModel nghiệp vụ cốt lõi (business object thuần)Annotation ORM, DTO request/responsedomain/value_objectsGiá trị bất biến gắn với nghiệp vụ (Money, Email, DateRange...)Logic truy vấn, logic I/Odomain/interfacesPort — hợp đồng mà infrastructure phải implementImplementation cụ thểapplication/use_cases1 use case = 1 hành động nghiệp vụ cụ thểLogic HTTP, logic DB trực tiếpapplication/dtosObject truyền dữ liệu vào/ra use caseBusiness logicapplication/validatorsValidate input trước khi vào use caseValidate liên quan tới DB (unique check nên ở use case)infrastructure/persistenceKết nối DB, ORM configBusiness logicinfrastructure/repositoriesImplement interface repository từ domainLogic nghiệp vụ, quyết định nghiệp vụinfrastructure/servicesGọi service ngoài (email, payment gateway, third-party API)Business rule quyết định khi nào gọipresentation/controllersNhận request, gọi use case, trả responseBusiness logic, query DB trực tiếppresentation/middlewareAuth, logging, error handling ở tầng HTTPBusiness logic
Quy tắc đặt file mới — checklist tự hỏi trước khi tạo file

File này có phụ thuộc framework/DB/HTTP không? → Nếu không → thuộc domain/.
File này định nghĩa 1 hành động nghiệp vụ cụ thể (1 API = 1 hoặc vài use case)? → thuộc application/use_cases/.
File này implement chi tiết kỹ thuật (DB query thật, gọi API thật)? → thuộc infrastructure/.
File này xử lý HTTP request/response trực tiếp? → thuộc presentation/.
Nếu không chắc, mặc định đặt câu hỏi: "Nếu đổi framework/DB, file này có phải sửa không?" — có thì thuộc infrastructure/presentation; không thì thuộc domain/application.

Khi nào KHÔNG cần áp dụng đầy đủ 4 layer

Với service/module CRUD đơn giản, không có business rule phức tạp → dùng cấu trúc rút gọn (controller → service → repository), tránh over-engineering (liên kết minimal-code.md).
Chỉ tách đầy đủ 4 layer cho module có nghiệp vụ đủ phức tạp, nhiều rule, cần test độc lập với framework. 

Tóm tắt: Domain là trung tâm, không phụ thuộc gì. Mọi dependency chỉ được trỏ vào trong. Presentation và Infrastructure là 2 layer ngoài cùng, được phép biết framework/DB nhưng không được chứa business logic.