package com.campus.ershou.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ershou.common.*;
import com.campus.ershou.dto.*;
import com.campus.ershou.entity.*;
import com.campus.ershou.mapper.*;
import com.campus.ershou.util.CaptchaStore;
import com.campus.ershou.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    @Autowired private SysUserMapper userMapper;
    @Autowired private MerchantInfoMapper merchantInfoMapper;
    @Autowired private WalletMapper walletMapper;
    @Autowired private UserPointsMapper pointsMapper;
    @Autowired private CaptchaStore captchaStore;
    @Autowired private JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Map<String, Object> login(LoginDTO dto) {
        if (!captchaStore.verify(dto.getCaptchaKey(), dto.getCaptchaCode())) {
            throw new BusinessException("验证码错误");
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null || !encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!Constants.STATUS_APPROVED.equals(user.getStatus())) {
            throw new BusinessException("账号待审核或已被拒绝，暂无法登录");
        }
        return buildLoginResult(user);
    }

    @Transactional
    public void registerUser(UserRegisterDTO dto) {
        if (!captchaStore.verify(dto.getCaptchaKey(), dto.getCaptchaCode())) {
            throw new BusinessException("验证码错误");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setCity(dto.getCity());
        user.setGender(dto.getGender());
        user.setBankAccount(dto.getBankAccount());
        user.setRole(Constants.ROLE_USER);
        user.setStatus(Constants.STATUS_PENDING);
        userMapper.insert(user);
        initWalletAndPoints(user.getId());
    }

    @Transactional
    public void registerMerchant(MerchantRegisterDTO dto) {
        if (!captchaStore.verify(dto.getCaptchaKey(), dto.getCaptchaCode())) {
            throw new BusinessException("验证码错误");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setCity(dto.getCity());
        user.setGender(dto.getGender());
        user.setBankAccount(dto.getBankAccount());
        user.setRole(Constants.ROLE_MERCHANT);
        user.setStatus(Constants.STATUS_PENDING);
        userMapper.insert(user);
        MerchantInfo mi = new MerchantInfo();
        mi.setUserId(user.getId());
        mi.setShopName(dto.getShopName());
        mi.setBusinessLicense(dto.getBusinessLicense());
        mi.setIdCardImage(dto.getIdCardImage());
        mi.setMerchantLevel(1);
        merchantInfoMapper.insert(mi);
        initWalletAndPoints(user.getId());
    }

    private void initWalletAndPoints(Long userId) {
        Wallet w = new Wallet();
        w.setUserId(userId);
        walletMapper.insert(w);
        UserPoints p = new UserPoints();
        p.setUserId(userId);
        pointsMapper.insert(p);
    }

    private Map<String, Object> buildLoginResult(SysUser user) {
        Map<String, Object> map = new HashMap<>();
        map.put("token", jwtUtil.generateToken(user.getId(), user.getRole()));
        user.setPassword(null);
        map.put("user", user);
        if (Constants.ROLE_MERCHANT.equals(user.getRole())) {
            map.put("merchant", merchantInfoMapper.selectOne(
                    new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getUserId, user.getId())));
        }
        return map;
    }
}
