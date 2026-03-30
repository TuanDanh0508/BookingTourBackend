---
description: Chạy ứng dụng Spring Boot ở môi trường dev
---

## Mục đích
Khởi động ứng dụng BookingTour Backend với Spring profile `dev`, kết nối PostgreSQL local.

## Điều kiện tiên quyết
- Java 25 đã được cài và có trong PATH
- PostgreSQL đang chạy và database `bookingtour` đã được tạo
- File `application-dev.yaml` đã cấu hình đúng datasource

## Các bước thực hiện

1. Chạy ứng dụng với profile dev
// turbo
```powershell
cd c:\Users\DELL\Desktop\bookingtour
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

2. Kiểm tra ứng dụng đã khởi động thành công
   - Mở trình duyệt hoặc Postman, gọi `GET http://localhost:8080/actuator/health` (nếu có actuator)
   - Hoặc kiểm tra log console: tìm dòng `Started BookingtourApplication in X seconds`

## Ghi chú
- Port mặc định: `8080`
- Nếu muốn đổi profile sang `staging` hoặc `prod`, thay `-Dspring-boot.run.profiles=dev` tương ứng
- JWT secret được lấy từ biến môi trường `JWT_SECRET` nếu có, còn không sẽ dùng giá trị mặc định trong `application.yaml`
