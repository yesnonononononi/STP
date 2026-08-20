package com.summit.stp.member.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.MemberLevelConfigRepository;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.member.infrastructure.persistence.mapper.UserMemberMapper;
import com.summit.stp.member.infrastructure.persistence.po.UserMemberPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class UserMemberRepositoryImpl extends AbstractRepository<UserMember, UserMemberPO>
        implements UserMemberRepository {
    @Autowired
    private UserMemberMapper userMemberMapper;
    @Autowired
    private MemberLevelConfigRepository<MemberLevelConfig> memberLevelConfigRepository;

    public UserMemberRepositoryImpl(BaseMapper<UserMemberPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public long countActiveMembers() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Long count = getBaseMapper().selectCount(new LambdaQueryWrapper<UserMemberPO>().gt(UserMemberPO::getExpireTime, now));
        return count != null ? count : 0L;
    }

    @Override
    public long countNewMembersAfter(Instant startTime) {
        if (startTime == null) return 0L;
        Timestamp startTs = Timestamp.from(startTime);
        Long count = getBaseMapper().selectCount(new LambdaQueryWrapper<UserMemberPO>().ge(UserMemberPO::getLevelUpgradeTime, startTs));
        return count != null ? count : 0L;
    }

    @Override
    public Map<Long, Long> countMemberLevelDistribution() {
        QueryWrapper<UserMemberPO> wrapper = new QueryWrapper<UserMemberPO>()
                .select("vip_level", "COUNT(*) as count")
                .groupBy("vip_level");
        List<Map<String, Object>> mapList = getBaseMapper().selectMaps(wrapper);
        if (mapList == null || mapList.isEmpty()) {
            return Map.of();
        }
        return mapList.stream()
                .filter(m -> m.get("vip_level") != null && m.get("count") != null)
                .collect(Collectors.toMap(
                        m -> Long.parseLong(m.get("vip_level").toString()),
                        m -> Long.parseLong(m.get("count").toString()),
                        (k1, k2) -> k1
                ));
    }

    @Override
    public Map<Long, Long> countPackageUserDistribution() {
        QueryWrapper<UserMemberPO> wrapper = new QueryWrapper<UserMemberPO>()
                .select("package_type_id", "COUNT(*) as count")
                .groupBy("package_type_id");
        List<Map<String, Object>> mapList = getBaseMapper().selectMaps(wrapper);
        if (mapList == null || mapList.isEmpty()) {
            return Map.of();
        }
        return mapList.stream()
                .filter(m -> m.get("package_type_id") != null && m.get("count") != null)
                .collect(Collectors.toMap(
                        m -> Long.parseLong(m.get("package_type_id").toString()),
                        m -> Long.parseLong(m.get("count").toString()),
                        (k1, k2) -> k1
                ));
    }

    @Override
    public UserMember queryUserMemberByUserId(long creatorId) {
        UserMemberPO po = getBaseMapper()
                .selectOne(new LambdaQueryWrapper<UserMemberPO>().eq(UserMemberPO::getUserId, creatorId));
        if (po == null)
            return null;
        MemberLevelConfig level = memberLevelConfigRepository.findByLevel(po.getVipLevel()).orElse(null);
        return convertToDomain(po, level);
    }

    @Override
    public void save(UserMember userMember) {
        if (userMember == null)
            return;
        if (findBy(userMember.getUserId(), UserMemberPO::getUserId).isPresent()) {
            super.updateById(userMember);
        } else {
            super.save(userMember);
        }
    }

    @Override
    public UserMember queryUserMemberByUserIdForUpdate(long creatorId) {
        UserMemberPO userMemberPO = userMemberMapper.selectByUserIdForUpdate(creatorId);
        if (userMemberPO == null) {
            return null;
        }
        return convertToDomain(userMemberPO,
                memberLevelConfigRepository.findByLevel(userMemberPO.getVipLevel()).orElse(null));
    }

    @Override
    public Map<Long, UserMember> queryUserMemberByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserMemberPO> poList = getBaseMapper().selectList(
                new LambdaQueryWrapper<UserMemberPO>().in(UserMemberPO::getUserId, userIds));
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyMap();
        }

        List<MemberLevelConfig> allConfigs = memberLevelConfigRepository.findAll();
        Map<Long, MemberLevelConfig> configMap = allConfigs != null
                ? allConfigs.stream().collect(Collectors.toMap(MemberLevelConfig::getLevel, c -> c, (v1, v2) -> v1))
                : Collections.emptyMap();

        return poList.stream()
                .collect(Collectors.toMap(
                        UserMemberPO::getUserId,
                        po -> convertToDomain(po, configMap.get(po.getVipLevel())),
                        (v1, v2) -> v1));
    }

    @Override
    public void update(UserMember userMember) {
        super.updateById(userMember);
    }

    @Override
    public Page<UserMember> queryPage(Integer page, Integer pageSize) {
        int currentPage = (page == null || page < 1) ? 1 : page;
        int currentSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        Page<UserMember> res = new Page<>();
        Page<UserMemberPO> p = new Page<>(currentPage, currentSize);
        Page<UserMemberPO> pages = getBaseMapper().selectPage(p, new LambdaQueryWrapper<>());
        List<UserMember> list = pages.getRecords().stream().map(this::toModel).toList();
        res.setTotal(pages.getTotal());
        res.setCurrent(pages.getCurrent());
        res.setRecords(list);
        return res;
    }

    @Override
    protected UserMemberPO toPO(UserMember userMember) {
        if (userMember == null)
            return null;
        return UserMemberPO.builder()
                .userId(userMember.getUserId())
                .totalRecharge(userMember.getTotalRecharge())
                .vipLevel(userMember.getLevel() != null ? userMember.getLevel().getLevel() : null)
                .packageTypeId(userMember.getPackageTypeId())
                .levelUpgradeTime(userMember.getLevelUpgradeTime())
                .updateTime(userMember.getUpdateTime())
                .expireTime(userMember.getExpireTime())
                .dailyRate(userMember.getDailyRate())
                .build();
    }

    @Override
    protected UserMember toModel(UserMemberPO po) {
        if (po == null)
            return null;
        return convertToDomain(po, null);
    }

    private UserMember convertToDomain(UserMemberPO po, MemberLevelConfig memberLevelConfig) {
        MemberType memberType = po.getPackageTypeId() != null ? MemberType.getById(po.getPackageTypeId()) : null;
        return UserMember.builder()
                .userId(po.getUserId())
                .totalRecharge(po.getTotalRecharge())
                .level(memberLevelConfig)
                .packageTypeId(po.getPackageTypeId())
                .memberType(memberType)
                .levelUpgradeTime(po.getLevelUpgradeTime())
                .updateTime(po.getUpdateTime())
                .expireTime(po.getExpireTime())
                .dailyRate(po.getDailyRate())
                .build();
    }
}
