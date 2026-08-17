package com.summit.stp.user.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.user.application.command.CreateUserFollowCommand;
import com.summit.stp.user.application.vo.UserFollowVO;

public interface UserFollowAppService {
    void follow(CreateUserFollowCommand command);
    void unfollow(Long id);
    Page<UserFollowVO> getFolloweesPage(Long followerId, long page, long pageSize);
}
