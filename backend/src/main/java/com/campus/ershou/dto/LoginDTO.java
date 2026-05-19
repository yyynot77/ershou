package com.campus.ershou.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String captchaKey;
    @NotBlank
    private String captchaCode;
}
