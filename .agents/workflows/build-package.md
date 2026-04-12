---
description: Build và đóng gói ứng dụng Spring Boot thành file JAR
---

## Mục đích
Compile, chạy test và đóng gói ứng dụng BookingTour thành file `.jar` để deploy.

## Điều kiện tiên quyết
- Java 21+ đã cài và có trong PATH
- PostgreSQL đang chạy (cần thiết nếu chạy kèm test)
- Biến môi trường `JWT_SECRET` đã được set (nếu chạy trên staging/prod)

## Các bước thực hiện

### 1. Kiểm tra Java version
// turbo
```powershell
java -version
```
> Kết quả mong đợi: `openjdk version "21.x.x"` hoặc cao hơn. Nếu thấp hơn, cần cài lại JDK.

### 2. Clean và build project (bỏ qua test)
// turbo
```powershell
cd c:\Users\DELL\Desktop\bookingtour
.\mvnw.cmd clean package -DskipTests
```

### 3. Kiểm tra file JAR đã được tạo
// turbo
```powershell
dir c:\Users\DELL\Desktop\bookingtour\target\*.jar
```

### 4. (Khuyến nghị trước khi deploy) Chạy kèm test
```powershell
.\mvnw.cmd clean verify
```

## Kết quả
- File JAR sẽ nằm tại: `target/bookingtour-0.0.1-SNAPSHOT.jar`

## Chạy JAR với từng profile

| Môi trường | Lệnh |
|------------|------|
| Dev (local) | `java -jar target/bookingtour-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev` |
| Staging | `java -jar target/bookingtour-0.0.1-SNAPSHOT.jar --spring.profiles.active=staging` |
| Production | `java -DJWT_SECRET=<secret> -jar target/bookingtour-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod` |

## Ghi chú
- `-DskipTests` bỏ qua test để build nhanh hơn trong quá trình dev
- Luôn chạy `clean verify` (có test) trước khi deploy lên **production**
- JDK và Maven Wrapper (`mvnw.cmd`) phải khớp version trong `pom.xml`
