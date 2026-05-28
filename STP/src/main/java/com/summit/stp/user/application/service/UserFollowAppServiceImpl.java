package com.summit.stp.user.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.user.application.command.CreateUserFollowCommand;
import com.summit.stp.user.application.command.UpdateUserFollowCommand;
import com.summit.stp.user.application.vo.UserFollowVO;
import com.summit.stp.user.domain.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFollowAppServiceImpl implements UserFollowAppService {
    private final UserFollowRepository userFollowRepository;

    @Override
    public UserFollowVO getUserFollowById(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void follow(CreateUserFollowCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void updateFollow(UpdateUserFollowCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void unfollow(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Page<UserFollowVO> getFollowersPage(Long followeeId, long page, long pageSize) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Page<UserFollowVO> getFolloweesPage(Long followerId, long page, long pageSize) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
