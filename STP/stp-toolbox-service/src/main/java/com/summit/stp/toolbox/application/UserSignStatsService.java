package com.summit.stp.toolbox.application;

import com.summit.stp.toolbox.api.dto.SignInInfoVO;
import com.summit.stp.toolbox.domain.model.UserSignStats;

public interface UserSignStatsService {

    UserSignStats getStatsByUserId(Long userId);

    void updateStats(UserSignStats stats);

    SignInInfoVO getSignInInfoVO(Long userId, Integer month);

    void doSignIn(Long userId);
}
