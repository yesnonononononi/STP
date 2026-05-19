package com.summit.stp.user.infrastructure.persistence;

import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.model.Username;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.user.infrastructure.persistence.mapper.UserMapper;
import com.summit.stp.user.infrastructure.persistence.po.UserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {
    @Autowired
    private UserMapper userMapper;

    @Override
    public void save(User user) {
        UserPO po = toPO(user);
        // 如果存在则更新，不存在则插入 (MyBatis Plus saveOrUpdate 或者先查后处理)
        UserPO existing = userMapper.selectById(po.getUname());
        if (existing != null) {
            userMapper.updateById(po);
        } else {
            userMapper.insert(po);
        }
    }

    @Override
    public void put(User user) {
        save(user);
    }

    @Override
    public User findUserByName(String username) {
        UserPO po = userMapper.selectById(username);
        if (po == null) {
            return null;
        }
        return fromPO(po);
    }

    private UserPO toPO(User user) {
        UserPO po = new UserPO();
        po.setUname(user.getUsername().getValue());
        po.setPassword(user.getPassword().getEncryptedValue());
        po.setPhone(user.getPhoneNumber().getValue());
        po.setStatusCode(user.getStatusCode());
        return po;
    }

    private User fromPO(UserPO po) {
        // 使用反射或构造函数还原领域模型
        // 注意：Password.fromHash 假设数据库存的是加密后的
        return User.create(
            Username.of(po.getUname()),
            Password.fromHash(po.getPassword()),
            PhoneNumber.of(po.getPhone())
        );
    }
}
