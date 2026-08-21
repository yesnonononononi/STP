package com.summit.stp.user.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.user.domain.model.UserStat;
import com.summit.stp.user.domain.repository.UserStatRepository;
import com.summit.stp.user.infrastructure.persistence.po.UserStatPO;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class UserStatRepositoryImpl extends AbstractRepository<UserStat, UserStatPO> implements UserStatRepository {

    public UserStatRepositoryImpl(BaseMapper<UserStatPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    protected UserStatPO toPO(UserStat entity) {
        return UserStatPO.builder()
                .userId(entity.getUserId())
                .fans(entity.getFans())
                .topic(entity.getTopic())
                .liked(entity.getLiked())
                .build();
    }

    @Override
    protected UserStat toModel(UserStatPO po) {
        return UserStat.builder()
                .userId(po.getUserId())
                .fans(po.getFans())
                .topic(po.getTopic())
                .liked(po.getLiked())
                .build();
    }

    @Override
    public Map<Long, UserStat> batchFindByIds(Collection<Long> ids) {
        List<UserStat> listIn = findListIn(ids, UserStatPO::getUserId);
        return listIn.stream().collect(Collectors.toMap(UserStat::getUserId, Function.identity()));
    }


}
