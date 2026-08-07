package com.summit.stp.toolbox.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.toolbox.domain.model.UserSignLog;
import com.summit.stp.toolbox.domain.repository.UserSignLogRepository;
import com.summit.stp.toolbox.infrastructure.persistence.mapper.UserSignLogMapper;
import com.summit.stp.toolbox.infrastructure.persistence.po.UserSignLogPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserSignLogRepositoryImpl implements UserSignLogRepository {
    private final UserSignLogMapper userSignLogMapper;

    @Override
    public void save(UserSignLog userSignLog) {
        UserSignLogPO po = toPO(userSignLog);
        userSignLogMapper.insert(po);
    }

    @Override
    public List<UserSignLog> findByUserId(Long userId,Integer month) {
        // 1. 使用 YearMonth 安全构造该年月的第一天和最后一天
        int year = LocalDate.now().getYear();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);// 该月1号
        LocalDate endDate = yearMonth.atEndOfMonth();// 该月最后一天

        // 2. 查询条件：大于等于月初 AND 小于等于月末
        List<UserSignLogPO> pos = userSignLogMapper.selectList(
                new LambdaQueryWrapper<UserSignLogPO>()
                        .eq(UserSignLogPO::getUserId, userId)
                        .ge(UserSignLogPO::getSignDate, startDate)  // 加上起始条件
                        .le(UserSignLogPO::getSignDate, endDate)    // 保留结束条件
        );
        return pos.stream().map(this::toModel).collect(Collectors.toList());
    }

    private UserSignLogPO toPO(UserSignLog model) {
        if (model == null) {
            return null;
        }
        UserSignLogPO po = new UserSignLogPO();
        po.setPublicId(model.getId());
        po.setUserId(model.getUserId());
        po.setSignDate(model.getSignDate());
        po.setSignTime(model.getSignTime());
        po.setSignSource(model.getSignSource());
        po.setRewardPoints(model.getRewardPoints());
        po.setContinuousDaysSnapshot(model.getContinuousDaysSnapshot());
        po.setExtra(model.getExtra());
        return po;
    }

    private UserSignLog toModel(UserSignLogPO po) {
        if (po == null) {
            return null;
        }
        return UserSignLog.builder()
                .id(po.getPublicId())
                .userId(po.getUserId())
                .signDate(po.getSignDate())
                .signTime(po.getSignTime())
                .signSource(po.getSignSource())
                .rewardPoints(po.getRewardPoints())
                .continuousDaysSnapshot(po.getContinuousDaysSnapshot())
                .extra(po.getExtra())
                .build();
    }


}
