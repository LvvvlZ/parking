package com.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {

    private String token;

    private Long userId;

    /** SUPER_ADMIN/ADMIN/USER */
    private String role;

    private String name;
}
