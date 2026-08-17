package com.summit.stp.toolbox.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.toolbox.domain.model.UserSignStats;
import com.summit.stp.toolbox.domain.repository.UserSignStatsRepository;
import com.summit.stp.toolbox.infrastructure.persistence.po.UserSignStatsPO;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public class UserSignStatsRepositoryImpl extends AbstractRepository<UserSignStats, UserSignStatsPO> implements UserSignStatsRepository {

    public UserSignStatsRepositoryImpl(BaseMapper<UserSignStatsPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public void save(UserSignStats userSignStats) {
        if (userSignStats == null) return;
        if (userSignStats.getUserId() != null && findById(userSignStats.getUserId()).isPresent()) {
            super.updateById(userSignStats);
        } else {
            super.save(userSignStats);
        }
    }

    @Override
    public UserSignStats findByUserId(Long userId) {
        return findById(userId).orElse(null);
    }

    @Override
    public UserSignStats initSignStat(Long userId, int totalDays, int currentContinuousDays, int maxContinuousDays, LocalDate lastSignDate) {
        UserSignStats entity = UserSignStats.builder()
                .userId(userId)
                .totalDays(totalDays)
                .currentContinuousDays(currentContinuousDays)
                .maxContinuousDays(maxContinuousDays)
                .lastSignDate(lastSignDate)
                .updateTime(LocalDateTime.now())
                .build();
        save(entity);
        return entity;
    }

    @Override
    protected UserSignStatsPO toPO(UserSignStats model) {
        if (model == null) {
            return null;
        }
        UserSignStatsPO po = new UserSignStatsPO();
        po.setUserId(model.getUserId());
        po.setTotalDays(model.getTotalDays());
        po.setCurrentContinuousDays(model.getCurrentContinuousDays());
        po.setMaxContinuousDays(model.getMaxContinuousDays());
        po.setLastSignDate(model.getLastSignDate());
        po.setUpdateTime(model.getUpdateTime());
        return po;
    }

    @Override
    protected UserSignStats toModel(UserSignStatsPO po) {
        if (po == null) {
            return null;
        }
        return UserSignStats.builder()
                .userId(po.getUserId())
                .totalDays(po.getTotalDays())
                .currentContinuousDays(po.getCurrentContinuousDays())
                .maxContinuousDays(po.getMaxContinuousDays())
                .lastSignDate(po.getLastSignDate())
                .updateTime(po.getUpdateTime())
                .build();
    }
}

