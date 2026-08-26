---
trigger: model_decision
description: Áp dụng khi tạo mới component, service, hoặc module ở frontend, hoặc khi quyết định 1 file UI mới nên đặt ở đâu. Chuẩn Modular + Atomic Design: module lo ranh giới tính năng, atomic design lo giao diện.
---

Quy tắc cấu trúc thư mục Frontend (Modular + Atomic Design)

Nguyên tắc cốt lõi: Module lo ranh giới tính năng — Atomic Design lo giao diện. Cấu trúc dưới đây mô tả bằng ví dụ đặt tên kiểu Angular (*.module.ts, *.service.ts); nếu dự án dùng React/Vue, giữ nguyên tư tưởng thư mục, chỉ đổi hậu tố file cho khớp convention của framework đó (vd OrdersPage.tsx, useOrders.ts).

Cấu trúc chuẩn
src/
├── shared/ui/                  # design system dùng chung toàn app
│   ├── atoms/                  # Button, Input, Icon
│   ├── molecules/               # SearchBar, Field
│   └── organisms/                # Modal, DataTable
│
├── modules/                     # mỗi tính năng = 1 module
│   ├── orders/
│   │   ├── components/           # atomic riêng của module này
│   │   │   ├── molecules/
│   │   │   └── organisms/        # OrderList
│   │   ├── orders.service.ts
│   │   └── orders.module.ts
│   │
│   └── products/
│       ├── components/
│       │   └── organisms/        # ProductCard
│       └── products.module.ts
│
└── app.module.ts                 # gom mọi module
Combo Rule — thứ tự lắp ráp UI
atom → molecule → organism → module

atom: dùng chung toàn app, không chứa business logic, không biết dữ liệu tới từ đâu (Button không biết nó đang submit đơn hàng hay đăng nhập).
molecule: ghép vài atom thành 1 nhóm chức năng nhỏ có ý nghĩa (SearchBar = Input + Button), vẫn không gắn với 1 module cụ thể nếu dùng chung được.
organism: 1 khối UI hoàn chỉnh, có thể thuộc về 1 module cụ thể (OrderList chỉ có ý nghĩa trong module Orders) — khác với atom/molecule là tài sản dùng chung.
module: gói toàn bộ 1 tính năng — UI riêng (organisms riêng) + logic riêng (*.service.ts) + khai báo (*.module.ts).

Quy tắc quyết định: component mới nên đặt ở đâu?
Tự hỏi theo thứ tự:

Component này có tái sử dụng được ở nhiều module khác nhau không, và không phụ thuộc dữ liệu nghiệp vụ cụ thể?
→ Có → đặt ở shared/ui/ (atoms/molecules/organisms tùy độ phức tạp).
→ Không → đi tiếp bước 2.
Component này chỉ có ý nghĩa trong 1 module cụ thể (gắn với data/business logic riêng của module đó)?
→ Có → đặt ở modules/{ten-module}/components/ (thường là organisms/ vì đã ghép nhiều phần).
Component đơn giản chỉ gồm 1-2 element HTML, chưa xứng đáng là 1 khối riêng?
→ Cân nhắc có thực sự cần tách component không, hay giữ inline trong organism cha (tránh over-engineering, liên kết minimal-code.md).

Quy tắc cho *.service.ts (logic/data layer của module)

Mỗi module có 1 service riêng chứa: gọi API, xử lý state cục bộ của module, transform dữ liệu cho component dùng.
Service của module không được import trực tiếp service của module khác — nếu 2 module cần chia sẻ logic, trích xuất phần dùng chung ra shared/ (tương tự nguyên tắc DRY trong clean-code.md).
Không đặt gọi API trực tiếp trong component — luôn qua service, để component chỉ lo hiển thị.

Quy tắc cho *.module.ts

Mỗi module tự khai báo, tự quản lý dependency của chính nó (component, service thuộc module).
app.module.ts (hoặc file gốc tương đương) chỉ gom các module lại, không chứa logic hay khai báo trực tiếp component/service ở đây.
Module mới thêm vào hệ thống không được yêu cầu sửa code bên trong module khác đã có (chỉ cần đăng ký ở nơi gom module).

Khi nào KHÔNG cần tách quá chi tiết

App nhỏ/đồ án, ít component dùng chung → có thể gộp molecules và organisms thành 1 thư mục components/ chung trong module, không cần tách đủ 3 cấp atomic nếu số lượng component còn ít (liên kết nguyên tắc YAGNI — đừng tạo cấu trúc cho quy mô chưa tới).
Chỉ tách đầy đủ atoms/molecules/organisms ở shared/ui/ khi đã có ≥ 2-3 component thực sự dùng lại nhiều nơi, tránh tạo thư mục rỗng "cho đủ chuẩn".


Tóm tắt: Atom/molecule dùng chung toàn app, đặt ở shared/ui/. Organism và logic nghiệp vụ thuộc về 1 module cụ thể, đặt trong modules/{ten-module}/. Component mới luôn tự hỏi "dùng chung được không" trước khi quyết định đặt ở đâu.