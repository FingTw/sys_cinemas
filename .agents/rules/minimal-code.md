---
trigger: always_on
---

Nguyên tắc code tối giản (Lazy Senior Dev Principle)

Code tốt nhất là code bạn không bao giờ phải viết. Trước khi viết bất kỳ dòng code nào, hãy đi qua checklist theo đúng thứ tự — dừng lại ngay khi có câu trả lời "có".

Thứ tự ưu tiên (bắt buộc kiểm tra tuần tự)
1. Có thực sự cần tồn tại không? → YAGNI

Trước khi code, hỏi: tính năng/hàm/class này có đang được yêu cầu ngay bây giờ không, hay chỉ là "phòng khi sau này cần"?
Nếu không ai dùng ngay → bỏ qua, đừng viết "for future flexibility".
Không tạo abstraction, interface, config layer cho use-case chưa tồn tại.

2. Đã có sẵn trong codebase chưa? → Reuse, đừng viết lại

Tìm kiếm trong project trước: đã có util function, service, component nào làm việc tương tự chưa?
Nếu có nhưng chưa hoàn toàn khớp → ưu tiên mở rộng/tham số hóa hàm cũ thay vì tạo bản sao gần giống.
Tránh copy-paste logic giữa các file — nếu thấy mình đang copy, dừng lại và refactor thành hàm dùng chung.

3. Standard library làm được không? → Dùng luôn

Kiểm tra ngôn ngữ/runtime đang dùng đã có sẵn hàm cho việc này chưa (vd: Collections, Stream, java.time, Optional trong Java; itertools, pathlib trong Python...).
Không tự viết lại thứ mà stdlib đã cung cấp (parse date, sort, format string, hash, UUID...).

4. Tính năng native của platform/framework làm được không? → Dùng luôn

Với Spring Boot: đã có annotation/cơ chế có sẵn chưa (@Valid, @Cacheable, @Scheduled, Pageable...) trước khi tự viết logic validate/cache/schedule tay.
Với trình duyệt/OS/DB: kiểm tra tính năng native (CSS thay vì JS, index DB thay vì lọc ở code, constraint DB thay vì validate tay ở nhiều tầng).

5. Dependency đã cài trong project làm được không? → Dùng luôn

Xem pom.xml/build.gradle/package.json đã có thư viện nào xử lý việc này chưa (vd: Apache Commons, Lombok, Jackson...) trước khi viết hàm helper riêng.
Không tự ý thêm dependency mới nếu chưa hỏi — nếu thấy cần thư viện mới, đề xuất trước khi thêm.

6. Giải quyết được trong 1 dòng? → Viết 1 dòng

Nếu logic đơn giản đến mức compress được thành 1 expression rõ ràng, đừng tách thành method/class/file riêng.
Không tạo wrapper/helper chỉ để gọi lại 1 hàm có sẵn mà không thêm giá trị gì.

7. Chỉ khi không còn cách nào ở trên: viết code tối thiểu vừa đủ chạy đúng

Viết đúng phần cần thiết cho yêu cầu hiện tại — không thêm tham số, flag, hay nhánh xử lý cho trường hợp chưa được yêu cầu.
Không tạo abstraction/interface/design pattern "cho đẹp" nếu hiện tại chỉ có 1 implementation.
Ưu tiên code dễ đọc, dễ xóa hơn code "linh hoạt" nhưng phức tạp.

Quy tắc bổ trợ

Trước khi tạo file/class/function mới, luôn tự hỏi: "Có cách nào để KHÔNG phải tạo cái này không?"
Nếu không chắc bước nào trong checklist áp dụng, hỏi lại người dùng thay vì tự ý viết thêm code "để chắc".
Khi refactor, ưu tiên xóa code hơn là thêm code mới để xử lý case cũ.
Không giữ lại code chết (dead code), comment-out code, hoặc TODO không có ticket/lý do rõ ràng.


Tóm tắt: Every line of code is a liability. Trước khi viết, luôn tìm cách tái sử dụng — thứ tự ưu tiên: bỏ qua → dùng lại code có sẵn → dùng stdlib → dùng tính năng native → dùng dependency đã cài → 1 dòng → và chỉ viết mới khi đã loại hết các lựa chọn trên.