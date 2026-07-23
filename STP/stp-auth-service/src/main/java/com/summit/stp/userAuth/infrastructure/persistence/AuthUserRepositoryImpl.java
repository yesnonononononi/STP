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
        return findUserByWrapper(wrapper);
    }

    @Override
    public Optional<AuthUser> findByPhone(String phone) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getPhone, phone);
        return findUserByWrapper(wrapper);
    }


    private Optional<AuthUser> findUserByWrapper(LambdaQueryWrapper<UserPO> wrapper){
        UserPO userPO = userMapper.selectOne(wrapper);

        if (userPO == null) {
            return Optional.empty();
        }

        AuthUser user = AuthUser.builder()
                .id(userPO.getId())
                .username(Username.of(userPO.getUname()))
                .password(Password.fromHash(userPO.getPassword()))
                .phoneNumber(PhoneNumber.of(userPO.getPhone()))
                .statusCode(userPO.getStatusCode())
                .build();


        return Optional.of(user);
    }

    @Override
    public void save(AuthUser user) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUname, user.getUsername());
        UserPO existingPO = userMapper.selectOne(wrapper);
        
        if (existingPO != null) {
            existingPO.setPassword(user.getPassword().getEncryptedValue());
            existingPO.setPhone(user.getPhoneNumber().getValue());
            existingPO.setStatusCode(user.getStatusCode() != null ? user.getStatusCode() : 1);
            userMapper.updateById(existingPO);
        } else {
            UserPO userPO = new UserPO();
            userPO.setUname(user.getUsername().getValue());
            userPO.setPassword(user.getPassword().getEncryptedValue());
            userPO.setPhone(user.getPhoneNumber().getValue());
            userPO.setStatusCode(1); // 默认激活状态
            userMapper.insert(userPO);
        }
    }

    @Override
    public boolean existsByPhone(String phone) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getPhone, phone);
        return userMapper.selectCount(wrapper) > 0;
    }
}

