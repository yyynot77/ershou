package com.campus.ershou.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegisterDTO {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String realName;
    @NotBlank private String phone;
    private String email;
    private String city;
    private Integer gender;
    @Pattern(regexp = "\\d{16}", message = "银行账号必须为16位数字")
    private String bankAccount;
    @NotBlank private String captchaKey;
    @NotBlank private String captchaCode;
}
