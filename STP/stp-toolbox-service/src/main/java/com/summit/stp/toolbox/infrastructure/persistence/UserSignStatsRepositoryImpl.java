package com.summit.stp.toolbox.infrastructure.persistence;

import com.summit.stp.toolbox.domain.model.UserSignStats;
import com.summit.stp.toolbox.domain.repository.UserSignStatsRepository;
import com.summit.stp.toolbox.infrastructure.persistence.mapper.UserSignStatsMapper;
import com.summit.stp.toolbox.infrastructure.persistence.po.UserSignStatsPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class UserSignStatsRepositoryImpl implements UserSignStatsRepository {
    private final UserSignStatsMapper userSignStatsMapper;

    @Override
    public void save(UserSignStats userSignStats) {
        UserSignStatsPO po = toPO(userSignStats);
        UserSignStatsPO existing = userSignStatsMapper.selectById(po.getUserId());
        if (existing != null) {
            userSignStatsMapper.updateById(po);
        } else {
            userSignStatsMapper.insert(po);
        }
    }

    @Override
    public UserSignStats findByUserId(Long userId) {
        UserSignStatsPO po = userSignStatsMapper.selectById(userId);
        return toModel(po);
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

    private UserSignStatsPO toPO(UserSignStats model) {
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

    private UserSignStats toModel(UserSignStatsPO po) {
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
