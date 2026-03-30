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
1. Health check → 2. Register → 3. Login (lấy token) → 4. Các API cần JWT
```

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
- `400 Bad Request` → username hoặc email đã tồn tại trong DB

---

### 1.2 Đăng nhập
```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"username\": \"testuser\", \"password\": \"Test@1234\"}'
```

**→ Copy token từ response, dùng cho tất cả bước tiếp theo**

```powershell
# Lưu token vào biến PowerShell để tái sử dụng
$TOKEN = "eyJhbGciOiJIUzI1NiJ9..."   # paste token vào đây
```

---

## Nhóm 2: API cần JWT (thêm header Authorization)

> Mọi request đến API được bảo vệ đều cần header:
> `Authorization: Bearer <token>`

### Template gọi API có JWT
```powershell
curl -X GET http://localhost:8080/api/v1/<endpoint> `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json"
```

---

## Nhóm 3: Kiểm tra các trường hợp lỗi (Negative Testing)

### 3.1 Gọi API protected mà không có token
```powershell
curl -X GET http://localhost:8080/api/v1/tours
# Mong đợi: 401 Unauthorized
```

### 3.2 Gọi với token sai
```powershell
curl -X GET http://localhost:8080/api/v1/tours `
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

---

## Bảng trạng thái HTTP cần nhớ

| Status | Ý nghĩa | Nguyên nhân thường gặp |
|--------|---------|------------------------|
| `200 OK` | Thành công | — |
| `400 Bad Request` | Request sai | Dữ liệu trùng, thiếu trường bắt buộc |
| `401 Unauthorized` | Chưa xác thực | Thiếu/sai token |
| `403 Forbidden` | Không có quyền | Token đúng nhưng role không đủ |
| `404 Not Found` | Không tìm thấy | ID không tồn tại trong DB |
| `500 Internal Server Error` | Lỗi server | SQL sai, NPE, DB connection lỗi |

---

## Checklist Test API
- [ ] App đang chạy ở profile `dev` (port 8080)
- [ ] Test Register thành công
- [ ] Test Login, lưu được token
- [ ] Test ít nhất 1 API cần JWT với token hợp lệ
- [ ] Test negative: không có token → 401
- [ ] Test negative: dữ liệu sai → 400
- [ ] Kiểm tra log console không có ERROR bất thường
