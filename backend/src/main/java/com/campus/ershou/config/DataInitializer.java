package com.campus.ershou.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ershou.common.Constants;
import com.campus.ershou.entity.SysUser;
import com.campus.ershou.entity.Wallet;
import com.campus.ershou.mapper.SysUserMapper;
import com.campus.ershou.mapper.WalletMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private WalletMapper walletMapper;
    @org.springframework.beans.factory.annotation.Value("${app.upload.path}")
    private String uploadPath;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        try {
            Files.createDirectories(Path.of(uploadPath));
            initAdmin();
        } catch (Exception e) {
            System.err.println("[DataInitializer] 初始化失败，请确认 MySQL 已启动且已执行 sql/init.sql: " + e.getMessage());
        }
    }

    private void initAdmin() {
        SysUser admin = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin"));
        if (admin == null) {
            admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setPhone("13800000000");
            admin.setEmail("admin@campus.edu");
            admin.setCity("北京");
            admin.setGender(1);
            admin.setBankAccount("6222021234567890");
            admin.setRole(Constants.ROLE_ADMIN);
            admin.setStatus(Constants.STATUS_APPROVED);
            userMapper.insert(admin);
            Wallet w = new Wallet();
            w.setUserId(admin.getId());
            w.setBalance(new BigDecimal("100000"));
            walletMapper.insert(w);
        } else if (!encoder.matches("admin123", admin.getPassword())) {
            admin.setPassword(encoder.encode("admin123"));
            userMapper.updateById(admin);
        }
    }
}
