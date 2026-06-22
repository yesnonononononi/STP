package com.summit.stp.user.application;

import com.summit.stp.user.application.vo.UserSimpleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserFiller {
    private final UserApplicationService userApplicationService;

    /**
     * 依据用户ID列表，批量获取并装载对应的用户简要信息 (UserSimpleVO) Map。
     *
     * @param userIds 用户ID集合
     * @return 用户ID -> UserSimpleVO 的映射 Map
     */
    public Map<Long, UserSimpleVO> fillUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> validUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (validUserIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userApplicationService.findSimpleUserByIds(validUserIds);
    }
}
