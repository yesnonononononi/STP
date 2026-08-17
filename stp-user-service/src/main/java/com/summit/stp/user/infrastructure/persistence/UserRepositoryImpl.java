package com.summit.stp.user.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.user.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl extends AbstractRepository<User, UserPO> implements UserRepository<User> {


    public UserRepositoryImpl(BaseMapper<UserPO> baseMapper) {
        super(baseMapper);
    }


    @Override
    public Long saveUser(User user) {
        Number id = save(user, UserPO::getId);
        return id != null ? id.longValue() : null;
    }

    @Override
    public Optional<User> findUserByName(String username) {
        return  findBy(username,UserPO::getUname);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return findBy(id,UserPO::getId);
    }

    @Override
    public Map<Long, User> findUserByIds(Collection<Long> userIds) {
        return findList(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    @Override
    public Optional<User>findUserByPhone(String phone) {
        return findBy(phone,UserPO::getPhone);
    }

    @Override
    protected UserPO toPO(User entity) {
        return UserPO.toPO(entity);
    }

    @Override
    protected User toModel(UserPO po) {
        return UserPO.toDomain(po, null, null, null);
    }
}
