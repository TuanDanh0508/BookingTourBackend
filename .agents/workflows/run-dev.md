---
description: Chạy ứng dụng Spring Boot ở môi trường dev
---

## Mục đích
Khởi động ứng dụng BookingTour Backend với Spring profile `dev`, kết nối PostgreSQL local.

## Điều kiện tiên quyết
- Java 21+ đã được cài và có trong PATH
- PostgreSQL đang chạy, database **`bookingtour`** đã được tạo
- File `application-dev.yaml` đã cấu hình đúng datasource (url, username, password)

## Các bước thực hiện

### 1. Kiểm tra PostgreSQL đang chạy
// turbo
```powershell
Test-NetConnection -ComputerName localhost -Port 5432
```
> Kết quả mong đợi: `TcpTestSucceeded: True`. Nếu `False`, cần khởi động PostgreSQL service trước.

### 2. Chạy ứng dụng với profile dev
// turbo
```powershell
cd c:\Users\DELL\Desktop\bookingtour
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. Kiểm tra ứng dụng đã khởi động thành công
Tìm dòng sau trong log console:
```
Started BookingtourApplication in X.XXX seconds
```
Hoặc gọi health check (nếu có Spring Actuator):
```powershell
curl http://localhost:8080/actuator/health
```

## Ghi chú
- **Port mặc định**: `8080` (cấu hình trong `application.yaml`)
- **Database name**: `bookingtour` (xem `application-dev.yaml`)
- **JWT Secret**: Lấy từ biến môi trường `JWT_SECRET` nếu có, còn không dùng giá trị default trong `application.yaml`
- Nếu muốn đổi profile sang `staging` hoặc `prod`, thay `-Dspring-boot.run.profiles=dev` tương ứng
- MyBatis XML mappers được load từ `classpath:mapper/*.xml` (cấu hình trong `application.yaml`)
