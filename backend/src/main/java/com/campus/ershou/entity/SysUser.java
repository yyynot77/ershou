package com.campus.ershou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String city;
    private Integer gender;
    private String bankAccount;
    private String role;
    private String status;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
