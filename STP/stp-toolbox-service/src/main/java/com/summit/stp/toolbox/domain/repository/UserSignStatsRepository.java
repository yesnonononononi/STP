package com.summit.stp.toolbox.domain.repository;

import com.summit.stp.toolbox.domain.model.UserSignStats;

public interface UserSignStatsRepository {
    void save(UserSignStats userSignStats);
    UserSignStats findByUserId(Long userId);

    UserSignStats initSignStat(Long userId,int totalDays, int currentContinuousDays, int maxContinuousDays);
}
