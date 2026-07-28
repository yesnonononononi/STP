package com.summit.stp.toolbox.application.impl;

import com.summit.stp.toolbox.application.UserSignLogService;
import com.summit.stp.toolbox.domain.model.UserSignLog;
import com.summit.stp.toolbox.domain.repository.UserSignLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSignLogServiceImpl implements UserSignLogService {
    private final UserSignLogRepository userSignLogRepository;

    @Override
    public void createSignLog(UserSignLog userSignLog) {
        userSignLogRepository.save(userSignLog);
    }


    @Override
    public List<UserSignLog> getSignLogsByUserId(Long userId,Integer month) {
        return userSignLogRepository.findByUserId(userId, month);
    }
}
