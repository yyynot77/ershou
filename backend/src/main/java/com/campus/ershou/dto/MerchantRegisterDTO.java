package com.campus.ershou.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MerchantRegisterDTO {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String realName;
    @NotBlank private String phone;
    private String email;
    private String city;
    private Integer gender;
    @NotBlank(message = "银行账号不能为空")
    @Pattern(regexp = "\\d{16}", message = "必须为16位数字")
    private String bankAccount;
    @NotBlank private String shopName;
    @NotBlank private String businessLicense;
    @NotBlank private String idCardImage;
    @NotBlank private String captchaKey;
    @NotBlank private String captchaCode;
}
