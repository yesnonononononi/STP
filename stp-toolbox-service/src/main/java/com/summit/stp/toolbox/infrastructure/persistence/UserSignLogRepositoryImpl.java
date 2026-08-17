package com.summit.stp.toolbox.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.toolbox.domain.model.UserSignLog;
import com.summit.stp.toolbox.domain.repository.UserSignLogRepository;
import com.summit.stp.toolbox.infrastructure.persistence.po.UserSignLogPO;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserSignLogRepositoryImpl extends AbstractRepository<UserSignLog, UserSignLogPO> implements UserSignLogRepository {

    public UserSignLogRepositoryImpl(BaseMapper<UserSignLogPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public void save(UserSignLog userSignLog) {
        if (userSignLog == null) return;
        if (userSignLog.getId() != null && findById(userSignLog.getId()).isPresent()) {
            super.updateById(userSignLog);
        } else {
            super.save(userSignLog);
        }
    }

    @Override
    public List<UserSignLog> findByUserId(Long userId, Integer month) {
        int year = LocalDate.now().getYear();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<UserSignLogPO> pos = getBaseMapper().selectList(
                new LambdaQueryWrapper<UserSignLogPO>()
                        .eq(UserSignLogPO::getUserId, userId)
                        .ge(UserSignLogPO::getSignDate, startDate)
                        .le(UserSignLogPO::getSignDate, endDate)
        );
        return pos.stream().map(this::toModel).collect(Collectors.toList());
    }

    @Override
    protected UserSignLogPO toPO(UserSignLog model) {
        if (model == null) {
            return null;
        }
        UserSignLogPO po = new UserSignLogPO();
        po.setId(model.getId());
        po.setUserId(model.getUserId());
        po.setSignDate(model.getSignDate());
        po.setSignTime(model.getSignTime());
        po.setSignSource(model.getSignSource());
        po.setRewardPoints(model.getRewardPoints());
        po.setContinuousDaysSnapshot(model.getContinuousDaysSnapshot());
        po.setExtra(model.getExtra());
        return po;
    }

    @Override
    protected UserSignLog toModel(UserSignLogPO po) {
        if (po == null) {
            return null;
        }
        return UserSignLog.builder()
                .id(po.getId())
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

