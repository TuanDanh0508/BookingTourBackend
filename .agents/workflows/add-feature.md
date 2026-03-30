---
description: Thêm một tính năng mới (entity + API endpoint) theo kiến trúc hiện tại
---

## Mục đích
Hướng dẫn từng bước thêm một tính năng mới vào hệ thống BookingTour theo đúng kiến trúc phân lớp: Entity → Mapper (MyBatis) → Repository → Service → Controller → DTO.

## Tech Stack
- **Spring Boot 3.4.5** + **MyBatis** + **PostgreSQL** + **Lombok** + **JWT Security**
- Package gốc: `com.danh.bookingtour`

## Quy trình thêm tính năng

### Bước 1: Định nghĩa bảng trong schema.sql
Thêm câu lệnh `CREATE TABLE IF NOT EXISTS` vào:
`src/main/resources/schema.sql`

Ví dụ thêm bảng `tours`:
```sql
CREATE TABLE IF NOT EXISTS tours (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price       NUMERIC(15, 2) NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW()
);
```

### Bước 2: Tạo Entity class
- **Vị trí**: `src/main/java/com/danh/bookingtour/entity/Tour.java`
- Dùng **Lombok** (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- Không dùng JPA annotations – MyBatis không cần `@Entity`

```java
package com.danh.bookingtour.entity;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tour {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private LocalDateTime createdAt;
}
```

### Bước 3: Tạo MyBatis Mapper interface
- **Vị trí**: `src/main/java/com/danh/bookingtour/mapper/TourMapper.java`
- Annotate với `@Mapper`
- Dùng annotation SQL (`@Select`, `@Insert`, v.v.) hoặc XML mapper tùy chọn

```java
package com.danh.bookingtour.mapper;

import com.danh.bookingtour.entity.Tour;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

@Mapper
public interface TourMapper {

    @Select("SELECT * FROM tours ORDER BY created_at DESC")
    List<Tour> findAll();

    @Select("SELECT * FROM tours WHERE id = #{id}")
    Optional<Tour> findById(Long id);

    @Insert("INSERT INTO tours (name, description, price) VALUES (#{name}, #{description}, #{price})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Tour tour);

    @Update("UPDATE tours SET name=#{name}, description=#{description}, price=#{price} WHERE id=#{id}")
    int update(Tour tour);

    @Delete("DELETE FROM tours WHERE id=#{id}")
    int deleteById(Long id);
}
```

### Bước 4: Tạo DTO (Request / Response)
- **Vị trí**: `src/main/java/com/danh/bookingtour/dto/`
- Tạo `TourRequest.java` và `TourResponse.java` riêng biệt

```java
// TourRequest.java
package com.danh.bookingtour.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TourRequest {
    private String name;
    private String description;
    private BigDecimal price;
}
```

### Bước 5: Tạo Controller
- **Vị trí**: `src/main/java/com/danh/bookingtour/controller/TourController.java`
- Annotate với `@RestController` và `@RequestMapping("/api/v1/tours")`
- Inject Mapper trực tiếp hoặc qua Service layer

```java
package com.danh.bookingtour.controller;

import com.danh.bookingtour.dto.TourRequest;
import com.danh.bookingtour.entity.Tour;
import com.danh.bookingtour.mapper.TourMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourMapper tourMapper;

    @GetMapping
    public ResponseEntity<List<Tour>> getAll() {
        return ResponseEntity.ok(tourMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tour> getById(@PathVariable Long id) {
        return tourMapper.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody TourRequest request) {
        Tour tour = Tour.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();
        tourMapper.insert(tour);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody TourRequest request) {
        Tour tour = Tour.builder()
                .id(id)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();
        tourMapper.update(tour);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourMapper.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Bước 6: Kiểm tra Security Config
Nếu endpoint cần bảo vệ bằng JWT, đảm bảo Security config đã cấu hình đúng.
Nếu endpoint public (ví dụ: xem danh sách tour), thêm vào `permitAll()`.

### Bước 7: Chạy và test
Dùng workflow `/run-dev` để khởi động, sau đó test với Postman:
- `GET http://localhost:8080/api/v1/tours`
- `POST http://localhost:8080/api/v1/tours` với body JSON

## Checklist
- [ ] Tạo bảng trong schema.sql
- [ ] Tạo Entity class (Lombok, không dùng JPA)
- [ ] Tạo Mapper interface (@Mapper)
- [ ] Tạo DTO Request / Response
- [ ] Tạo Controller với @RestController
- [ ] Kiểm tra Security config
- [ ] Test API với Postman
