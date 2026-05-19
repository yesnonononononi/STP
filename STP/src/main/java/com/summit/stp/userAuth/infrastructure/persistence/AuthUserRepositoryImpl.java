package com.summit.stp.userAuth.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.model.Username;
import com.summit.stp.userAuth.domain.model.AuthUser;
import com.summit.stp.userAuth.domain.repository.AuthUserRepository;
import com.summit.stp.userAuth.infrastructure.persistence.mapper.UserAuthMapper;
import com.summit.stp.userAuth.infrastructure.persistence.po.UserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuthUserRepositoryImpl implements AuthUserRepository {
    private final  UserAuthMapper userMapper;

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUname, username);
        UserPO userPO = userMapper.selectOne(wrapper);
        
        if (userPO == null) {
            return Optional.empty();
        }
        
        AuthUser user = AuthUser.create(
            Username.of(userPO.getUname()),
            Password.fromHash(userPO.getPassword()),
            PhoneNumber.of(userPO.getPhone())
        );
        user.setStatusCode(userPO.getStatusCode());
        
        return Optional.of(user);
    }

    @Override
    public void save(AuthUser user) {
        UserPO userPO = new UserPO();
        userPO.setUname(user.getUsernameValue());
        userPO.setPassword(user.getPassword().getEncryptedValue());
        userPO.setPhone(user.getPhoneNumber().getValue());
        userPO.setStatusCode(1); // 默认激活状态
        userMapper.insert(userPO);
    }

    @Override
    public boolean existsByPhone(String phone) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getPhone, phone);
        return userMapper.selectCount(wrapper) > 0;
    }
}

