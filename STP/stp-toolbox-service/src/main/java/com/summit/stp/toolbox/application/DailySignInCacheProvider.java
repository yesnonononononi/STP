package com.summit.stp.toolbox.application;

import com.summit.stp.toolbox.api.dto.SignInInfoVO;
import com.summit.stp.toolbox.domain.model.UserSignStats;

import java.util.List;


public interface DailySignInCacheProvider {



    SignInInfoVO getSignInfo(Long userId, List<Integer> dates);

    void doSignIn(Long userId, UserSignStats userSignStats);


    List<Integer> getDates(Long userId, Integer month);


    void cacheBit(List<Integer> list, Long userId, Integer month);
}
