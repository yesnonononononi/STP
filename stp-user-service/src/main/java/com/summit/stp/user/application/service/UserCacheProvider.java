package com.summit.stp.user.application.service;

import com.summit.stp.user.api.vo.UserSimpleVO;

import java.util.List;
import java.util.Map;

public interface UserCacheProvider {
    Map<Long, UserSimpleVO> batchGetUserSimpleVO(List<Long> userIds);

    @SuppressWarnings("unchecked")
    void batchSetUserSimpleVO(Map<Long, UserSimpleVO> userSimpleVOMap);

    void clearUserInfo(Long userId);
}

