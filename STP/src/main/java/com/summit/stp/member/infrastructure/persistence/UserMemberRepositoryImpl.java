package com.summit.stp.member.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.MemberLevelConfigRepository;
import com.summit.stp.member.domain.repository.MemberTypeRepository;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.member.infrastructure.persistence.mapper.UserMemberMapper;
import com.summit.stp.member.infrastructure.persistence.po.UserMemberPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class UserMemberRepositoryImpl implements UserMemberRepository {
    private final UserMemberMapper userMemberMapper;
    private final MemberTypeRepository memberTypeRepository;
    private final MemberLevelConfigRepository memberLevelConfigRepository;
    @Override
    public UserMember queryUserMemberByUserId(long creatorId) {
        LambdaQueryWrapper<UserMemberPO> wrapper = new LambdaQueryWrapper<UserMemberPO>().eq(UserMemberPO::getUserId, creatorId);
        UserMemberPO userMemberPO = userMemberMapper.selectOne(wrapper);
        if (userMemberPO == null)  return null;
        MemberLevelConfig level = memberLevelConfigRepository.findByLevel(userMemberPO.getVipLevel());
        return convertToDomain(userMemberPO,level);
    }

    @Override
    public void save(UserMember userMember) {
        userMemberMapper.insertOrUpdate(convertToPO(userMember));
    }

    @Override
    public UserMember queryUserMemberByUserIdForUpdate(long creatorId) {
        UserMemberPO userMemberPO = userMemberMapper.selectByUserIdForUpdate(creatorId);
        if (userMemberPO == null) {
            return null;
        }
        return convertToDomain(userMemberPO, memberLevelConfigRepository.findByLevel(userMemberPO.getVipLevel()));
    }

    private UserMemberPO convertToPO(UserMember userMember) {
        return UserMemberPO.builder()
                .userId(userMember.getUserId())
                .totalRecharge(userMember.getTotalRecharge())
                .vipLevel(userMember.getLevel().getLevel())
                .packageTypeId(userMember.getPackageTypeId())
                .levelUpgradeTime(userMember.getLevelUpgradeTime())
                .updateTime(userMember.getUpdateTime())
                .expireTime(userMember.getExpireTime())
                .dailyRate(userMember.getDailyRate())
                .build();
    }
    private UserMember convertToDomain(UserMemberPO po,MemberLevelConfig memberLevelConfig) {
        MemberType memberType = po.getPackageTypeId() != null ? memberTypeRepository.selectById(po.getPackageTypeId()) : null;
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

    @Override
    public Map<Long, UserMember> queryUserMemberByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<UserMemberPO> wrapper = new LambdaQueryWrapper<UserMemberPO>()
                .in(UserMemberPO::getUserId, userIds);
        List<UserMemberPO> poList = userMemberMapper.selectList(wrapper);
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
                        (v1, v2) -> v1
                ));
    }
}
