package com.summit.stp.user.application.service;

import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.user.application.UserApplicationService;
import com.summit.stp.user.application.command.UserPutCommand;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.user.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAPPServiceImpl implements UserApplicationService {
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void put(UserPutCommand command) {
        // 1. 始终从会话上下文获取当前用户，确保越权隔离
        String currentUsername = UserHolder.getUser().getUsername();
        log.info("用户修改请求: {}", currentUsername);

        // 2. 加载现有实体
        User user = userRepository.findUserByName(currentUsername);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 处理密码修改
        if (StringUtils.hasText(command.getNewPassword())) {
            user.changePassword(command.getOldPassword(), Password.fromRaw(command.getNewPassword()));
        }

        // 4. 处理手机号修改（通过领域服务协调验证码）
        if (StringUtils.hasText(command.getPhoneNumber())) {
            PhoneNumber newPhone = PhoneNumber.of(command.getPhoneNumber());
            userDomainService.changePhone(user, newPhone, command.getVerifyCode());
        }

        // 5. 持久化更新后的实体
        userRepository.save(user);
        log.info("用户修改成功: {}", currentUsername);
    }
}
