package com.campus.ershou.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ershou.common.Constants;
import com.campus.ershou.entity.UserPoints;
import com.campus.ershou.mapper.UserPointsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PointsService {
    @Autowired
    private UserPointsMapper pointsMapper;

    public UserPoints getOrCreate(Long userId) {
        UserPoints p = pointsMapper.selectOne(new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId));
        if (p == null) {
            p = new UserPoints();
            p.setUserId(userId);
            p.setPoints(0);
            pointsMapper.insert(p);
        }
        return p;
    }

    public BigDecimal calcDeductAmount(int usePoints) {
        if (usePoints <= 0) return BigDecimal.ZERO;
        UserPoints up = getOrCreate(com.campus.ershou.common.UserContext.getUserId());
        int actual = Math.min(usePoints, up.getPoints());
        return BigDecimal.valueOf(actual / Constants.POINTS_REDEEM_UNIT).setScale(2, RoundingMode.DOWN);
    }

    @Transactional
    public void earn(Long userId, BigDecimal payAmount, Long orderId) {
        int earn = payAmount.setScale(0, RoundingMode.DOWN).intValue() * Constants.POINTS_PER_YUAN;
        if (earn <= 0) return;
        UserPoints p = getOrCreate(userId);
        p.setPoints(p.getPoints() + earn);
        pointsMapper.updateById(p);
    }

    @Transactional
    public void redeem(Long userId, int points) {
        if (points <= 0) return;
        UserPoints p = getOrCreate(userId);
        if (p.getPoints() < points) {
            throw new com.campus.ershou.common.BusinessException("积分不足");
        }
        p.setPoints(p.getPoints() - points);
        pointsMapper.updateById(p);
    }
}
