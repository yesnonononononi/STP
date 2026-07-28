package com.summit.stp.toolbox.application;

import com.summit.stp.toolbox.domain.model.UserSignLog;

import java.util.List;

public interface UserSignLogService {
    void createSignLog(UserSignLog userSignLog);

    List<UserSignLog> getSignLogsByUserId(Long userId,Integer month);
}
