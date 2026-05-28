package com.summit.stp.user.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.model.Username;
import com.summit.stp.user.domain.model.Email;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.user.infrastructure.persistence.mapper.UserMapper;
import com.summit.stp.user.infrastructure.persistence.mapper.UserStatMapper;
import com.summit.stp.user.infrastructure.persistence.po.UserPO;
import com.summit.stp.user.infrastructure.persistence.po.UserStatPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserMapper userMapper;
    private final UserStatMapper userStatMapper;

    @Override
    public void save(User user) {
        UserPO po = toPO(user);
        // 如果存在则更新，不存在则插入 (MyBatis Plus saveOrUpdate 或者先查后处理)
        UserPO existing = userMapper.selectOne(new LambdaQueryWrapper<UserPO>()
                .eq(UserPO::getUname, po.getUname()));
        if (existing != null) {
            po.setId(existing.getId());
            userMapper.updateById(po);
        } else {
            userMapper.insert(po);
        }
        // 保存用户统计信息
        saveUserStat(po.getId(), user);
    }

    @Override
    public void put(User user) {
        save(user);
    }

    @Override
    public User findUserByName(String username) {
        UserPO po = userMapper.selectOne(new LambdaQueryWrapper<UserPO>()
                .eq(UserPO::getUname, username));
        if (po == null) {
            return null;
        }
        return fromPO(po);
    }

    @Override
    public User findUserById(Long id) {
        UserPO userPO = userMapper.selectById(id);
        if (userPO == null) {
            return null;
        }
        return fromPO(userPO);
    }

    @Override
    public void updateProfile(User user) {
        userMapper.updateById(toPO(user));
        saveUserStat(user.getId(), user);
    }

    private void saveUserStat(Long userId, User user) {
        UserStatPO stat = userStatMapper.selectById(userId);
        boolean isNew = (stat == null);
        if (isNew) {
            stat = new UserStatPO();
            stat.setUserId(userId);
        }
        stat.setFans(user.getFans() != null ? user.getFans() : 0L);
        stat.setTopic(user.getTopic() != null ? user.getTopic() : 0L);
        stat.setLiked(user.getLiked() != null ? user.getLiked() : 0L);
        if (isNew) {
            userStatMapper.insert(stat);
        } else {
            userStatMapper.updateById(stat);
        }
    }

    private UserPO toPO(User user) {
        UserPO po = new UserPO();
        po.setNick(user.getNick());
        po.setId(user.getId());
        po.setUname(user.getUsername().getValue());
        po.setPassword(user.getPassword().getEncryptedValue());
        po.setPhone(user.getPhoneNumber() != null ? user.getPhoneNumber().getValue() : null);
        po.setStatusCode(user.getStatusCode());
        po.setAvatar(user.getAvatar());
        po.setIntroduction(user.getIntroduction());
        po.setIp(user.getIp());
        po.setGender(user.getGender());
        po.setAge(user.getAge());
        return po;
    }

    private User fromPO(UserPO po) {
        UserStatPO stat = null;
        if (po.getId() != null) {
            stat = userStatMapper.selectById(po.getId());
        }
        long fans = (stat != null && stat.getFans() != null) ? stat.getFans() : 0L;
        long topic = (stat != null && stat.getTopic() != null) ? stat.getTopic() : 0L;
        long liked = (stat != null && stat.getLiked() != null) ? stat.getLiked() : 0L;

        return User.builder()
                .username(Username.of(po.getUname()))
                .password(Password.fromHash(po.getPassword()))
                .introduction(po.getIntroduction())
                .phoneNumber(po.getPhone() != null ? PhoneNumber.of(po.getPhone()) : null)
                .statusCode(po.getStatusCode())
                .fans(fans)
                .nick(po.getNick())
                .liked(liked)
                .topic(topic)
                .email(Email.of(po.getEmail()))
                .avatar(po.getAvatar())
                .ip(po.getIp())
                .id(po.getId())
                .gender(po.getGender())
                .age(po.getAge())
                .build();
    }
}
