package com.summit.stp.user.domain.repository;

import com.summit.devframeworkdddstarter.repo.RepositoryTemplate;
import com.summit.stp.user.domain.model.UserStat;
import com.summit.stp.user.infrastructure.persistence.po.UserStatPO;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface UserStatRepository extends RepositoryTemplate<UserStat,UserStatPO> {
    Map<Long, UserStat> batchFindByIds(Collection<Long> ids);


}
