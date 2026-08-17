package com.summit.stp.user.application.service.impl;


import com.summit.stp.common.application.domain.exception.NoFoundUserInfoException;
import com.summit.stp.user.api.vo.UserAuthVO;
import com.summit.stp.user.application.service.UserAuthSupport;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户认证支持服务实现
 * 专门为认证服务提供用户数据，与主业务服务层隔离
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthSupportImpl implements UserAuthSupport {

    private final UserRepository<User> userRepository;

    @Override
    public UserAuthVO getUserAuthByUsername(String username) {
        User user = userRepository.findUserByName(username).orElseThrow(NoFoundUserInfoException::new);
        return convertToAuthVO(user);
    }

    @Override
    public UserAuthVO getUserAuthByPhone(String phone) {
        User user = userRepository.findUserByPhone(phone).orElseThrow(NoFoundUserInfoException::new);
        return convertToAuthVO(user);
    }

    @Override
    public UserAuthVO getUserAuthById(Long userId) {
        User user = userRepository.findUserById(userId).orElseThrow(NoFoundUserInfoException::new);
        return convertToAuthVO(user);
    }

    /**
     * 将 User 领域模型转换为 UserAuthVO
     * 包含密码字段，仅供认证服务内部使用
     */
    private UserAuthVO convertToAuthVO(User user) {
        if (user == null) {
            return null;
        }
        return UserAuthVO.builder()
                .userId(user.getId())
                .username(user.getUsername().getValue())
                .password(user.getPassword().getEncryptedValue())
                .phoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber().getValue() : null)
                .statusCode(user.getStatusCode())
                .build();
    }
}
