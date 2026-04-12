---
description: Phân tích yêu cầu nghiệp vụ và lập kế hoạch kỹ thuật cho hệ thống BookingTour
---

## Mục đích
Khi nhận được yêu cầu nghiệp vụ (từ khách hàng, product owner, hoặc ý tưởng mới), workflow này giúp:
1. Hiểu rõ yêu cầu trước khi viết code
2. Phân rã thành các task kỹ thuật cụ thể
3. Xác định những rủi ro và phụ thuộc
4. Ưu tiên thứ tự thực hiện

---

## Bước 1: Thu thập và làm rõ yêu cầu

Khi nhận yêu cầu nghiệp vụ, hãy hỏi (hoặc tự xác định) đủ các thông tin sau:

### 1.1 Actor (Ai thực hiện?)
- Khách hàng (`ROLE_USER`)?
- Nhân viên / Admin (`ROLE_ADMIN`)?
- Hệ thống tự động (cron job, webhook)?

### 1.2 Phạm vi tính năng
- Đây là tính năng mới hay mở rộng tính năng cũ?
- Có ảnh hưởng đến dữ liệu hiện có không?
- Có cần thay đổi schema DB không?

### 1.3 Quy tắc nghiệp vụ (Business Rules)
- Điều kiện ràng buộc? (VD: "Không thể đặt tour đã hết chỗ")
- Luồng xử lý nếu lỗi? (VD: "Hoàn tiền nếu tour bị hủy")
- Trạng thái của entity? (VD: Booking có thể là: PENDING → CONFIRMED → CANCELLED)

---

## Bước 2: Mô hình hóa nghiệp vụ thành Data Model

Xác định các entity cần thiết và quan hệ giữa chúng.

### Template phân tích entity

```
Entity: [Tên entity]
├── Table: m_[tên] (master data) hoặc t_[tên] (transaction)
├── Attributes: [Các trường dữ liệu]
├── Relationships:
│   ├── [Entity A] → [kiểu quan hệ: 1-1 / 1-N / N-N]
│   └── ...
├── Business Rules:
│   ├── [Ràng buộc 1]
│   └── ...
└── Status Flow: [PENDING] → [CONFIRMED] → [CANCELLED]
```

### Ví dụ: Yêu cầu "Khách hàng đặt tour"

```
Entity: Booking
├── Table: t_booking
├── Attributes: id, user_id, tour_id, booking_date, total_price, status, created_at
├── Relationships:
│   ├── User → 1-N (1 user có nhiều booking)
│   └── Tour → N-1 (nhiều booking cho 1 tour)
├── Business Rules:
│   ├── Không thể đặt nếu tour đã đầy (available_slots <= 0)
│   ├── Giá = tour.price × số lượng người
│   └── Chỉ ROLE_USER mới được tạo booking
└── Status Flow: PENDING → CONFIRMED → CANCELLED / COMPLETED
```

### Quy ước đặt tên bảng hiện tại
- `m_` prefix: Master data (VD: `m_client`, `m_tour`)
- `users`: Bảng đặc biệt cho authentication, không có prefix
- Tên cột: `snake_case`, MyBatis tự map sang `camelCase` trong Java

---

## Bước 3: Phân rã thành Task kỹ thuật

Với mỗi yêu cầu nghiệp vụ, map thành các task theo kiến trúc hiện tại:

| # | Task kỹ thuật | File cần tạo/sửa | Ưu tiên |
|---|--------------|-----------------|---------|
| 1 | Thêm bảng vào schema | `src/main/resources/schema.sql` | 🔴 Cao |
| 2 | Tạo Entity class | `entity/Tour.java` | 🔴 Cao |
| 3 | Tạo Mapper **interface** | `mapper/TourMapper.java` | 🔴 Cao |
| 4 | Tạo **XML Mapper** | `src/main/resources/mapper/TourMapper.xml` | 🔴 Cao |
| 5 | Tạo DTO Request/Response | `dto/TourRequest.java`, `dto/TourResponse.java` | 🔴 Cao |
| 6 | Tạo Controller | `controller/TourController.java` | 🔴 Cao |
| 7 | Cập nhật Security config | `config/SecurityConfig.java` | 🟡 Trung bình |
| 8 | Xử lý lỗi nghiệp vụ | Trong Controller hoặc `exception/` | 🟡 Trung bình |
| 9 | Test API | Postman / curl (xem `/test-api`) | 🔵 Thấp |

> Xem chi tiết cách thực hiện từng bước tại workflow `/add-feature`.

---

## Bước 4: Đánh giá độ phức tạp và rủi ro

### Ma trận đánh giá

| Tiêu chí | Thấp (1) | Trung bình (2) | Cao (3) |
|----------|---------|----------------|---------|
| **Số entity mới** | 0-1 | 2-3 | 4+ |
| **Thay đổi schema hiện tại** | Không | Thêm cột | Đổi kiểu/xóa |
| **Logic nghiệp vụ phức tạp** | CRUD đơn giản | Có validation | Có workflow/trạng thái |
| **Ảnh hưởng Security** | Không | Thêm endpoint public | Thay đổi role/permissions |
| **Phụ thuộc API bên ngoài** | Không | Có (ít) | Có (nhiều/payment) |

**Tổng điểm:**
- 5-7: Có thể bắt đầu ngay, dùng `/add-feature`
- 8-11: Cần lập kế hoạch rõ trước khi code
- 12-15: Cần chia nhỏ thành nhiều sprint/PR

---

## Bước 5: Xác định thứ tự ưu tiên

Dùng nguyên tắc **MoSCoW**:

| Nhãn | Ý nghĩa | Ví dụ với BookingTour |
|------|---------|----------------------|
| **Must Have** | Bắt buộc, không có thì không hoạt động | Auth, xem danh sách tour, đặt tour |
| **Should Have** | Quan trọng nhưng có thể release sau | Lịch sử đặt tour, tìm kiếm tour |
| **Could Have** | Tốt nếu có | Rating tour, gợi ý tour |
| **Won't Have** | Không làm trong sprint này | App mobile, tích hợp thanh toán quốc tế |

---

## Bước 6: Output — Tạo danh sách task

Sau khi phân tích, yêu cầu AI tạo danh sách task cụ thể theo format:

```markdown
## Yêu cầu: [Tên tính năng]

### Must Have
- [ ] Tạo bảng `m_tour` trong schema.sql
- [ ] Tạo entity `Tour` + mapper interface `TourMapper`
- [ ] Tạo XML mapper `TourMapper.xml`
- [ ] API: GET /api/v1/tours (public)
- [ ] API: POST /api/v1/bookings (ROLE_USER, cần JWT)

### Should Have
- [ ] Validate: không đặt tour đã hết chỗ
- [ ] API: GET /api/v1/bookings/my (lịch sử của user hiện tại)

### Rủi ro
- Cần thêm cột `available_slots` vào bảng `m_tour`
- Logic tính giá cần xác nhận lại với business
```

---

## Checklist Phân tích Nghiệp vụ
- [ ] Xác định Actor (user/admin/system)
- [ ] Làm rõ Business Rules và trạng thái entity
- [ ] Vẽ quan hệ giữa các entity, xác định prefix bảng (`m_` vs `t_`)
- [ ] Phân rã thành task kỹ thuật (schema → entity → mapper interface → XML mapper → DTO → controller)
- [ ] Đánh giá độ phức tạp và rủi ro
- [ ] Phân loại Must/Should/Could/Won't
- [ ] Xác nhận lại với người yêu cầu trước khi code
