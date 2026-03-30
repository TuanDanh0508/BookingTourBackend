package com.danh.bookingtour.dto;

import com.danh.bookingtour.entity.ClientType;
import lombok.Data;

@Data
public class ClientRequest {
    private String name;                  // Bắt buộc
    private ClientType type;              // Bắt buộc — HOTEL | BUS | RESTAURANT | ATTRACTION | GUIDE | TRANSPORTATION
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String description;
}
