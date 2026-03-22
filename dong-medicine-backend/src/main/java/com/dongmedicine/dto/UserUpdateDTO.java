package com.dongmedicine.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {
    
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20字符之间")
    private String username;
}
