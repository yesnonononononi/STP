package com.summit.stp.toolbox.domain.repository;

import com.summit.stp.toolbox.domain.model.UserSignLog;

import java.util.List;

public interface UserSignLogRepository {
    void save(UserSignLog userSignLog);
    List<UserSignLog> findByUserId(Long userId,Integer month);
}
