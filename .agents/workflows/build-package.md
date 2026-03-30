---
description: Build và đóng gói ứng dụng Spring Boot thành file JAR
---

## Mục đích
Compile, chạy test và đóng gói ứng dụng BookingTour thành file `.jar` để deploy.

## Các bước thực hiện

1. Clean và build project
// turbo
```powershell
cd c:\Users\DELL\Desktop\bookingtour
.\mvnw.cmd clean package -DskipTests
```

2. Kiểm tra file JAR đã được tạo
// turbo
```powershell
dir c:\Users\DELL\Desktop\bookingtour\target\*.jar
```

3. (Tùy chọn) Chạy kèm test trước khi đóng gói
```powershell
.\mvnw.cmd clean verify
```

## Kết quả
- File JAR sẽ nằm tại: `target/bookingtour-0.0.1-SNAPSHOT.jar`
- Chạy JAR trực tiếp với profile cụ thể:
```powershell
java -jar target/bookingtour-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Ghi chú
- `-DskipTests` bỏ qua test để build nhanh hơn trong quá trình dev
- Luôn chạy `clean verify` (có test) trước khi deploy lên production
