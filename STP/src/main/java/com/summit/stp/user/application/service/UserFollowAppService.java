package com.summit.stp.user.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.user.application.command.CreateUserFollowCommand;
import com.summit.stp.user.application.command.UpdateUserFollowCommand;
import com.summit.stp.user.application.vo.UserFollowVO;

public interface UserFollowAppService {
    UserFollowVO getUserFollowById(Long id);
    void follow(CreateUserFollowCommand command);
    void updateFollow(UpdateUserFollowCommand command);
    void unfollow(Long id);
    Page<UserFollowVO> getFollowersPage(Long followeeId, long page, long pageSize);
    Page<UserFollowVO> getFolloweesPage(Long followerId, long page, long pageSize);
}
