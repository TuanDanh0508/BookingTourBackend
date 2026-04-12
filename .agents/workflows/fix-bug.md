---
description: Quy trình chẩn đoán và sửa bug trong ứng dụng BookingTour Backend
---

## Mục đích
Hướng dẫn từng bước để tìm nguyên nhân gốc rễ (root cause) và sửa bug một cách có hệ thống, tránh fix theo cảm tính.

## Tech Stack cần lưu ý
- **MyBatis XML Mapper**: SQL viết trong `src/main/resources/mapper/*.xml` → dễ lỗi namespace sai, `id` method không khớp
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
| **Request body / params** | `{"username": "danh", "password": "..."}` |
| **Thông báo lỗi trong console** | Stack trace đầy đủ |

---

## Bước 2: Đọc log console

Log level của mapper đang ở `DEBUG` trong dev, vì vậy luôn kiểm tra:

```
# Các loại lỗi phổ biến và nơi tìm chúng:

[ERROR] o.s.b.a.e.web.DefaultErrorAttributes  → Lỗi nghiệp vụ / exception chưa được handle
[ERROR] com.zaxxer.hikari                     → Không kết nối được PostgreSQL
[WARN]  o.s.s.w.a.UsernamePasswordAuth...     → Sai username/password khi login
[DEBUG] com.danh.bookingtour.mapper           → Câu SQL thực tế đang chạy (từ XML)
```

1. Chạy app ở chế độ dev và tái hiện bug, lưu log ra file
// turbo
```powershell
cd c:\Users\DELL\Desktop\bookingtour
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev 2>&1 | Tee-Object -FilePath .\bug-log.txt
```

2. Sau khi tái hiện lỗi, tìm dòng `[ERROR]` hoặc `Exception` trong log
```powershell
Select-String -Path .\bug-log.txt -Pattern "ERROR|Exception" | Select-Object -First 30
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
- File: `src/main/java/com/danh/bookingtour/config/SecurityConfig.java`

### 🔴 Lỗi 500 với `BadSqlGrammarException`
**Nguyên nhân:** SQL trong XML mapper bị sai tên bảng/cột

**Kiểm tra:**
- Đối chiếu SQL trong `src/main/resources/mapper/*.xml` với `schema.sql`
- Bật log DEBUG để xem câu SQL thực tế đang chạy (đã bật sẵn trong `application-dev.yaml`)
- Kiểm tra tên cột trong XML dùng `snake_case` (VD: `contact_name`, không phải `contactName`)

### 🔴 Lỗi `BindingException` / `Invalid bound statement`
**Nguyên nhân:** XML Mapper không được load đúng

**Kiểm tra:**
1. `namespace` trong XML phải khớp chính xác với package + tên interface:
   ```xml
   <mapper namespace="com.danh.bookingtour.mapper.TourMapper">
   ```
2. `id` của mỗi statement phải khớp tên method trong interface
3. Trong `application.yaml`, kiểm tra:
   ```yaml
   mybatis:
     mapper-locations: classpath:mapper/*.xml
   ```
4. File XML phải nằm đúng tại `src/main/resources/mapper/`

### 🔴 Lỗi `NullPointerException`
**Nguyên nhân thường gặp:**
- Thiếu `@NoArgsConstructor` trên Entity/DTO → Jackson không deserialize được
- MyBatis trả về `null` thay vì `Optional.empty()` → cần kiểm tra kiểu trả về trong XML

### 🔴 Lỗi kết nối DB (`HikariPool`)
**Kiểm tra:**
- PostgreSQL service đang chạy chưa?
- Xem lại datasource config trong `application-dev.yaml` (url, username, password)

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

```xml
<!-- NOTE: namespace phải là fully-qualified interface name, không phải tên class ngắn -->
<mapper namespace="com.danh.bookingtour.mapper.TourMapper">
```

---

## Checklist Fix Bug
- [ ] Mô tả rõ: endpoint + status + error message
- [ ] Đọc full stack trace trong console (hoặc `bug-log.txt`)
- [ ] Phân loại lỗi (401/403/500/NPE/DB connection/XML binding)
- [ ] Kiểm tra đúng file tương ứng (XML mapper nếu lỗi SQL/binding)
- [ ] Sửa và restart app
- [ ] Test lại API để xác nhận
- [ ] Kiểm tra regression API liên quan
