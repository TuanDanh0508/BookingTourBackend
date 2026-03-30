package com.danh.bookingtour.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    private Long id;
    private String name;
    private ClientType type;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
