package com.summit.stp.user.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.common.util.TrendDateUtil;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.model.stats.UserTrendStat;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.user.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.summit.stp.user.api.vo.stats.UserTrendsVO;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    public Long countNewUser() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0));
        return getBaseMapper().selectCount(new LambdaQueryWrapper<UserPO>().ge(UserPO::getCreateTime, Instant.from(startOfDay.atZone(ZoneId.systemDefault()))));
    }

    @Override
    public long countTotalUsers() {
        Long count = getBaseMapper().selectCount(null);
        return count != null ? count : 0L;
    }

    @Override
    public long countUsersCreatedAfter(Instant startTime) {
        if (startTime == null) return 0L;
        Long count = getBaseMapper().selectCount(new LambdaQueryWrapper<UserPO>().ge(UserPO::getCreateTime, startTime));
        return count != null ? count : 0L;
    }

    @Override
    public UserTrendStat countUserTrends(int days) {
        int limitDays = (days > 0 && days <= 60) ? days : 7;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(limitDays - 1);
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        QueryWrapper<UserPO> wrapper = new QueryWrapper<UserPO>()
                .select("DATE(create_time) AS dateStr", "COUNT(*) AS dayCount")
                .ge("create_time", startInstant)
                .groupBy("DATE(create_time)");

        List<Map<String, Object>> mapList = getBaseMapper().selectMaps(wrapper);
        Map<String, Integer> resultMap = (mapList != null) ? mapList.stream()
                .filter(m -> m.get("dateStr") != null)
                .collect(Collectors.toMap(
                        m -> m.get("dateStr").toString(),
                        m -> m.get("dayCount") != null ? Integer.parseInt(m.get("dayCount").toString()) : 0,
                        (k1, k2) -> k1
                )) : Map.of();

        var trend = TrendDateUtil.buildTrendData(
                limitDays, resultMap, Function.identity(), 0
        );

        return UserTrendStat.builder()
                .dates(trend.dates())
                .newUserCountList(trend.values())
                .build();
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
