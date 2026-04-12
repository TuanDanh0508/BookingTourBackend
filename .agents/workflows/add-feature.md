---
description: Thêm một tính năng mới (entity + API endpoint) theo kiến trúc hiện tại
---

## Mục đích
Hướng dẫn từng bước thêm một tính năng mới vào hệ thống BookingTour theo đúng kiến trúc phân lớp:
**Schema → Entity → Mapper Interface → XML Mapper → DTO → Controller**

## Tech Stack
- **Spring Boot 3.4.x** + **MyBatis (XML Mapper)** + **PostgreSQL** + **Lombok** + **JWT Security**
- Package gốc: `com.danh.bookingtour`
- XML mappers location: `src/main/resources/mapper/*.xml`

> ⚠️ **Kiến trúc XML Mapper**: Dự án đã migrate từ annotation-based (`@Select`, `@Insert`) sang XML mapper.
> **Không dùng** SQL annotation trực tiếp trên interface. Mọi SQL phải viết trong file XML tương ứng.

---

## Quy trình thêm tính năng

### Bước 1: Định nghĩa bảng trong schema.sql
Thêm câu lệnh `CREATE TABLE IF NOT EXISTS` vào:
`src/main/resources/schema.sql`

Ví dụ thêm bảng `m_tour` (theo quy ước đặt tên `m_` cho master data):
```sql
CREATE TABLE IF NOT EXISTS m_tour (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price       NUMERIC(15, 2) NOT NULL,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT NOW()
);
```

### Bước 2: Tạo Entity class
- **Vị trí**: `src/main/java/com/danh/bookingtour/entity/Tour.java`
- Dùng **Lombok** (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- Không dùng JPA annotations – MyBatis không cần `@Entity`
- Field names dùng **camelCase**, MyBatis tự map từ `snake_case` DB nhờ `map-underscore-to-camel-case: true`

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
    private Boolean isActive;
    private LocalDateTime createdAt;
}
```

### Bước 3: Tạo MyBatis Mapper Interface
- **Vị trí**: `src/main/java/com/danh/bookingtour/mapper/TourMapper.java`
- Annotate với `@Mapper`
- **Không viết SQL ở đây** – chỉ khai báo method signature
- Dùng `@Param` nếu method có nhiều tham số

```java
package com.danh.bookingtour.mapper;

import com.danh.bookingtour.entity.Tour;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface TourMapper {

    List<Tour> findAllActive();

    Optional<Tour> findByIdActive(Long id);

    int insert(Tour tour);

    int update(Tour tour);

    int softDelete(Long id);

    boolean existsByName(@Param("name") String name, @Param("excludeId") Long excludeId);
}
```

### Bước 4: Tạo XML Mapper
- **Vị trí**: `src/main/resources/mapper/TourMapper.xml`
- `namespace` phải khớp chính xác với fully-qualified interface name
- `id` của mỗi statement phải khớp tên method trong interface

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.danh.bookingtour.mapper.TourMapper">

    <select id="findAllActive" resultType="com.danh.bookingtour.entity.Tour">
        SELECT * FROM m_tour
        WHERE is_active = true
        ORDER BY created_at DESC
    </select>

    <select id="findByIdActive" resultType="com.danh.bookingtour.entity.Tour">
        SELECT * FROM m_tour WHERE id = #{id} AND is_active = true
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO m_tour (name, description, price, is_active)
        VALUES (#{name}, #{description}, #{price}, #{isActive})
    </insert>

    <update id="update">
        UPDATE m_tour
        SET name        = #{name},
            description = #{description},
            price       = #{price}
        WHERE id = #{id}
    </update>

    <update id="softDelete">
        UPDATE m_tour SET is_active = false WHERE id = #{id}
    </update>

    <select id="existsByName" resultType="boolean">
        SELECT COUNT(*) > 0
        FROM m_tour
        WHERE name = #{name}
          AND id != COALESCE(#{excludeId}, -1)
          AND is_active = true
    </select>

</mapper>
```

### Bước 5: Tạo DTO (Request / Response)
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

```java
// TourResponse.java
package com.danh.bookingtour.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
```

### Bước 6: Tạo Controller
- **Vị trí**: `src/main/java/com/danh/bookingtour/controller/TourController.java`
- Annotate với `@RestController` và `@RequestMapping("/api/v1/tours")`
- Inject **Mapper trực tiếp** (kiến trúc hiện tại không có Service layer riêng)
- Mapping Entity → Response ngay trong Controller (theo pattern của `ClientController`)

```java
package com.danh.bookingtour.controller;

import com.danh.bookingtour.dto.TourRequest;
import com.danh.bookingtour.dto.TourResponse;
import com.danh.bookingtour.entity.Tour;
import com.danh.bookingtour.mapper.TourMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourMapper tourMapper;

    @GetMapping
    public ResponseEntity<List<TourResponse>> getAll() {
        return ResponseEntity.ok(
            tourMapper.findAllActive().stream().map(this::mapToResponse).collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourResponse> getById(@PathVariable Long id) {
        return tourMapper.findByIdActive(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TourRequest request) {
        if (tourMapper.existsByName(request.getName(), null)) {
            return ResponseEntity.badRequest().body("Tour with this name already exists");
        }
        Tour tour = Tour.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .isActive(true)
                .build();
        tourMapper.insert(tour);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TourRequest request) {
        if (tourMapper.existsByName(request.getName(), id)) {
            return ResponseEntity.badRequest().body("Another tour with this name already exists");
        }
        return tourMapper.findByIdActive(id).map(existing -> {
            existing.setName(request.getName());
            existing.setDescription(request.getDescription());
            existing.setPrice(request.getPrice());
            tourMapper.update(existing);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourMapper.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    private TourResponse mapToResponse(Tour tour) {
        return TourResponse.builder()
                .id(tour.getId())
                .name(tour.getName())
                .description(tour.getDescription())
                .price(tour.getPrice())
                .isActive(tour.getIsActive())
                .createdAt(tour.getCreatedAt())
                .build();
    }
}
```

### Bước 7: Kiểm tra Security Config
Nếu endpoint cần bảo vệ bằng JWT, đảm bảo Security config đã cấu hình đúng.
- Endpoint public: thêm vào `permitAll()` trong `SecurityConfig`
- Endpoint cần auth: mặc định đã được bảo vệ bởi `JwtAuthenticationFilter`

### Bước 8: Chạy và test
Dùng workflow `/run-dev` để khởi động, sau đó test với `/test-api`.

---

## Checklist
- [ ] Tạo bảng trong `schema.sql` (dùng prefix `m_` cho master data)
- [ ] Tạo Entity class (Lombok, không dùng JPA, camelCase fields)
- [ ] Tạo Mapper **interface** (`@Mapper`, không viết SQL)
- [ ] Tạo **XML mapper** tại `src/main/resources/mapper/TênMapper.xml`
- [ ] Tạo DTO `Request` và `Response` riêng biệt
- [ ] Tạo Controller (`@RestController`, inject Mapper, có `mapToResponse()`)
- [ ] Kiểm tra Security config
- [ ] Test API với Postman hoặc curl
