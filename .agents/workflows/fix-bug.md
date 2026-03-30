---
description: Quy trình chẩn đoán và sửa bug trong ứng dụng BookingTour Backend
---

## Mục đích
Hướng dẫn từng bước để tìm nguyên nhân gốc rễ (root cause) và sửa bug một cách có hệ thống, tránh fix theo cảm tính.

## Tech Stack cần lưu ý
- **MyBatis**: Không có lazy loading, SQL viết tay → dễ lỗi câu truy vấn, tên cột sai
- **Spring Security + JWT**: Lỗi 403/401 thường do filter chain hoặc token hết hạn
- **PostgreSQL**: Case-sensitive với tên cột nếu dùng dấu ngoặc kép
- **Lombok**: Lỗi NPE có thể do thiếu `@NoArgsConstructor` khi Jackson deserialize

---

## Bước 1: Mô tả bug rõ ràng

Trước khi hỏi AI hoặc debug, cần xác định đủ 4 thông tin:

| Thông tin | Ví dụ |
|-----------|-------|
| **Endpoint bị lỗi** | `POST /api/auth/login` |
| **HTTP status trả về** | `500 Internal Server Error` |
| **Request body / params** | `{"username": "danh", "password": "123"}` |
| **Thông báo lỗi trong console** | Stack trace đầy đủ |

---

## Bước 2: Đọc log console

Log level của mapper đang ở `DEBUG` trong dev, vì vậy luôn kiểm tra:

```
# Các loại lỗi phổ biến và nơi tìm chúng:

[ERROR] o.s.b.a.e.web.DefaultErrorAttributes  → Lỗi nghiệp vụ / exception chưa được handle
[ERROR] com.zaxxer.hikari                     → Không kết nối được PostgreSQL
[WARN]  o.s.s.w.a.UsernamePasswordAuth...     → Sai username/password khi login
[DEBUG] com.danh.bookingtour.mapper           → Câu SQL thực tế đang chạy
```

1. Chạy app ở chế độ dev và tái hiện bug
// turbo
```powershell
cd c:\Users\DELL\Desktop\bookingtour
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev 2>&1 | Tee-Object -FilePath .\bug-log.txt
```

---

## Bước 3: Phân loại lỗi và hướng xử lý

### 🔴 Lỗi 401 Unauthorized
**Nguyên nhân thường gặp:**
- Không gửi header `Authorization: Bearer <token>` 
- Token đã hết hạn (mặc định 86400000ms = 1 ngày)
- Token sai định dạng

**Kiểm tra:**
- File: `src/main/java/com/danh/bookingtour/security/JwtAuthenticationFilter.java`
- Xem JWT secret có khớp giữa lúc generate và verify không

### 🔴 Lỗi 403 Forbidden
**Nguyên nhân thường gặp:**
- Endpoint chưa được `permitAll()` trong Security config
- Role của user trong DB không khớp với `@PreAuthorize` trên controller

**Kiểm tra:**
- File: `src/main/java/com/danh/bookingtour/config/SecurityConfig.java` (hoặc tương đương)

### 🔴 Lỗi 500 với `BadSqlGrammarException`
**Nguyên nhân:** SQL trong `@Select`/`@Insert` bị sai tên bảng/cột

**Kiểm tra:**
- Đối chiếu annotation SQL trong `mapper/` với `schema.sql`
- Bật log DEBUG để xem câu SQL thực tế: đã bật sẵn trong `application-dev.yaml`

### 🔴 Lỗi `NullPointerException`
**Nguyên nhân thường gặp:**
- Thiếu `@NoArgsConstructor` trên Entity/DTO → Jackson không deserialize được
- MyBatis trả về `null` thay vì `Optional.empty()` → cần kiểm tra kiểu trả về

### 🔴 Lỗi kết nối DB (`HikariPool`)
**Kiểm tra:**
- PostgreSQL service đang chạy chưa? 
- `application-dev.yaml`: url=`jdbc:postgresql://localhost:5432/booking_tour`, user=`postgres`, pass=`0508`

```powershell
# Kiểm tra PostgreSQL đang lắng nghe không
Test-NetConnection -ComputerName localhost -Port 5432
```

---

## Bước 4: Sửa bug và verify

1. Sửa code
2. Restart app (Ctrl+C rồi chạy lại `/run-dev`)
3. Gọi lại API để xác nhận đã fix
4. Kiểm tra không có regression ở các API liên quan

---

## Bước 5: Ghi lại nguyên nhân (tùy chọn)

Thêm comment vào code nếu đây là lỗi tinh tế, dễ tái phát:
```java
// NOTE: MyBatis không tự map camelCase nếu không bật map-underscore-to-camel-case
// Đã cấu hình trong application.yaml: mybatis.configuration.map-underscore-to-camel-case=true
```

---

## Checklist Fix Bug
- [ ] Mô tả rõ: endpoint + status + error message
- [ ] Đọc full stack trace trong console
- [ ] Phân loại lỗi (401/403/500/NPE/DB connection)
- [ ] Kiểm tra đúng file tương ứng
- [ ] Sửa và restart app
- [ ] Test lại API để xác nhận
- [ ] Kiểm tra regression API liên quan
