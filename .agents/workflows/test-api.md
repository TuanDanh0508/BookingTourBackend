---
description: Test toàn bộ API của BookingTour một cách có hệ thống bằng curl hoặc Postman
---

## Mục đích
Hướng dẫn test từng nhóm API theo thứ tự logic, đảm bảo authentication flow hoạt động trước khi test các API cần bảo vệ.

## Base URL
```
http://localhost:8080
```

## Thứ tự test bắt buộc
```
1. Health check → 2. Register → 3. Login (lấy token) → 4. Client API → 5. Các API khác cần JWT
```

---

## Nhóm 0: Health Check

```powershell
# Kiểm tra app đang chạy
curl http://localhost:8080/actuator/health
```
**Kết quả mong đợi:** `{"status":"UP"}` hoặc HTTP 200.
Nếu lỗi kết nối, chạy lại `/run-dev` trước.

---

## Nhóm 1: Auth API (`/api/auth`)

### 1.1 Đăng ký tài khoản mới
```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"username\": \"testuser\", \"password\": \"Test@1234\", \"email\": \"test@example.com\"}'
```

**Kết quả mong đợi:**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

**Lỗi thường gặp:**
- `400 Bad Request` → username hoặc email đã tồn tại trong DB (không có response body chi tiết)

---

### 1.2 Đăng nhập
```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"username\": \"testuser\", \"password\": \"Test@1234\"}'
```

**Kết quả mong đợi:**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

**→ Copy token từ response, lưu vào biến để dùng cho các bước tiếp theo:**
```powershell
# Lưu token vào biến PowerShell để tái sử dụng
$TOKEN = "eyJhbGciOiJIUzI1NiJ9..."   # paste token vào đây
```

---

## Nhóm 2: Client API (`/api/v1/clients`) — Cần JWT

> Mọi request đến API được bảo vệ đều cần header:
> `Authorization: Bearer <token>`

### 2.1 Lấy danh sách clients (tất cả loại)
```powershell
curl -X GET http://localhost:8080/api/v1/clients `
  -H "Authorization: Bearer $TOKEN"
```

### 2.2 Lấy danh sách clients theo type
```powershell
# Các ClientType hợp lệ: AIRLINE, HOTEL, TRANSPORT, v.v. (xem enum ClientType)
curl -X GET "http://localhost:8080/api/v1/clients?type=AIRLINE" `
  -H "Authorization: Bearer $TOKEN"
```

### 2.3 Lấy client theo ID
```powershell
curl -X GET http://localhost:8080/api/v1/clients/1 `
  -H "Authorization: Bearer $TOKEN"
```
**Lỗi thường gặp:** `404 Not Found` → ID không tồn tại hoặc đã bị soft-delete (`is_active = false`)

### 2.4 Tạo client mới
```powershell
curl -X POST http://localhost:8080/api/v1/clients `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d '{
    \"name\": \"Vietnam Airlines\",
    \"type\": \"AIRLINE\",
    \"contactName\": \"Nguyen Van A\",
    \"phone\": \"0901234567\",
    \"email\": \"contact@vna.vn\",
    \"address\": \"Hanoi\",
    \"description\": \"National airline\"
  }'
```
**Lỗi thường gặp:** `400 Bad Request` → client với name + type này đã tồn tại

### 2.5 Cập nhật client
```powershell
curl -X PUT http://localhost:8080/api/v1/clients/1 `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d '{
    \"name\": \"Vietnam Airlines Updated\",
    \"type\": \"AIRLINE\",
    \"contactName\": \"Nguyen Van B\",
    \"phone\": \"0901234568\",
    \"email\": \"new@vna.vn\",
    \"address\": \"Ho Chi Minh City\",
    \"description\": \"Updated description\"
  }'
```

### 2.6 Xóa client (soft delete)
```powershell
curl -X DELETE http://localhost:8080/api/v1/clients/1 `
  -H "Authorization: Bearer $TOKEN"
```
**Kết quả mong đợi:** `204 No Content` — record không bị xóa khỏi DB, chỉ set `is_active = false`

---

## Nhóm 3: Kiểm tra các trường hợp lỗi (Negative Testing)

### 3.1 Gọi API protected mà không có token
```powershell
curl -X GET http://localhost:8080/api/v1/clients
# Mong đợi: 401 Unauthorized
```

### 3.2 Gọi với token sai
```powershell
curl -X GET http://localhost:8080/api/v1/clients `
  -H "Authorization: Bearer token_sai_123"
# Mong đợi: 401 Unauthorized hoặc 403 Forbidden
```

### 3.3 Đăng ký trùng username
```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"username\": \"testuser\", \"password\": \"abc\", \"email\": \"new@example.com\"}'
# Mong đợi: 400 Bad Request
```

### 3.4 Đăng nhập sai mật khẩu
```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"username\": \"testuser\", \"password\": \"sai_mat_khau\"}'
# Mong đợi: 401 Unauthorized
```

### 3.5 Tạo client trùng name + type
```powershell
curl -X POST http://localhost:8080/api/v1/clients `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d '{\"name\": \"Vietnam Airlines\", \"type\": \"AIRLINE\", \"contactName\": \"X\", \"phone\": \"0900000000\", \"email\": \"x@x.com\", \"address\": \"HN\", \"description\": \"\"}'
# Mong đợi: 400 Bad Request với message "Client with this name and type already exists"
```

---

## Bảng trạng thái HTTP cần nhớ

| Status | Ý nghĩa | Nguyên nhân thường gặp |
|--------|---------|------------------------|
| `200 OK` | Thành công | — |
| `204 No Content` | Thành công, không có body | DELETE thành công |
| `400 Bad Request` | Request sai | Dữ liệu trùng, thiếu trường bắt buộc |
| `401 Unauthorized` | Chưa xác thực | Thiếu/sai/hết hạn token |
| `403 Forbidden` | Không có quyền | Token đúng nhưng role không đủ |
| `404 Not Found` | Không tìm thấy | ID không tồn tại hoặc đã soft-delete |
| `500 Internal Server Error` | Lỗi server | SQL sai trong XML, NPE, DB connection lỗi |

---

## Checklist Test API
- [ ] App đang chạy ở profile `dev` (port 8080)
- [ ] Health check OK
- [ ] Test Register thành công → nhận được `token`
- [ ] Test Login → lưu `$TOKEN` vào biến PowerShell
- [ ] Test GET `/api/v1/clients` với token hợp lệ
- [ ] Test POST `/api/v1/clients` tạo mới thành công
- [ ] Test PUT, DELETE client
- [ ] Test negative: không có token → 401
- [ ] Test negative: dữ liệu trùng → 400
- [ ] Kiểm tra log console không có `[ERROR]` bất thường
