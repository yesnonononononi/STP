package com.summit.stp.userAuth.infrastructure.persistence;

import cn.hutool.core.util.StrUtil;
import com.summit.stp.common.application.domain.model.Password;
import com.summit.stp.common.application.domain.model.PhoneNumber;
import com.summit.stp.common.application.domain.model.Username;
import com.summit.stp.user.api.client.AuthFeignClient;
import com.summit.stp.user.api.client.UserFeignClient;
import com.summit.stp.user.api.vo.UserAuthVO;
import com.summit.stp.userAuth.domain.model.AuthUser;
import com.summit.stp.userAuth.domain.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.function.Supplier;

@Repository
@RequiredArgsConstructor
public class AuthUserRepositoryImpl implements AuthUserRepository {

    private final AuthFeignClient authFeignClient;
    private final UserFeignClient userFeignClient;

    /**
     * 根据用户名或用户ID或手机号查找用户
     *
     * @param username 用户名
     * @param userId   用户ID
     * @param phone    手机号
     * @param error    错误供应商
     * @return AuthUser 认证用户领域模型
     */
    @Override
    public <T extends Throwable> AuthUser findUserByOrThrow(String username, Long userId, String phone, Supplier<? extends T> error) throws T {
        UserAuthVO vo = null;
        
        // 根据不同条件调用对应的认证接口
        if (userId != null) {
            vo = authFeignClient.getUserAuthById(userId).getData();
        } else if (StrUtil.isNotBlank(username)) {
            vo = authFeignClient.getUserAuthByUsername(username).getData();
        } else if (StrUtil.isNotBlank(phone)) {
            vo = authFeignClient.getUserAuthByPhone(phone).getData();
        }
        
        if (Objects.isNull(vo)) {
            throw error.get();
        }
        
        // 将 UserAuthVO 转换为 AuthUser 领域模型
        return AuthUser.builder()
                .userId(vo.getUserId())
                .username(Username.of(vo.getUsername()))
                .password(Password.fromHash(vo.getPassword()))
                .phoneNumber(vo.getPhoneNumber() != null ? PhoneNumber.of(vo.getPhoneNumber()) : null)
                .statusCode(vo.getStatusCode())
                .build();
    }

    @Override
    public void save(AuthUser user) {
        // 认证服务不负责保存用户数据
        // 用户数据的保存由用户服务负责
    }

    @Override
    public void updateUserIp(Long userId, String ipLocation) {
        userFeignClient.updateUserIp(userId,ipLocation);
    }
}
