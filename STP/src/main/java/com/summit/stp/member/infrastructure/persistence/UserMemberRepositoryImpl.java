package com.summit.stp.member.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.MemberTypeRepository;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.member.infrastructure.persistence.mapper.UserMemberMapper;
import com.summit.stp.member.infrastructure.persistence.po.UserMemberPO;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.wrapper.BaseWrapper;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class UserMemberRepositoryImpl implements UserMemberRepository {
    private final UserMemberMapper userMemberMapper;
    private final MemberTypeRepository memberTypeRepository;

    @Override
    public UserMember queryUserMemberByUserId(long creatorId) {
        LambdaQueryWrapper<UserMemberPO> wrapper = new LambdaQueryWrapper<UserMemberPO>().eq(UserMemberPO::getUserId, creatorId);
        UserMemberPO userMemberPO = userMemberMapper.selectOne(wrapper);
        if (userMemberPO == null) {
            return null;
        }
        return convertToDomain(userMemberPO);
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
        return convertToDomain(userMemberPO);
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
    private UserMember convertToDomain(UserMemberPO po) {
        MemberType memberType = po.getPackageTypeId() != null ? memberTypeRepository.selectById(po.getPackageTypeId()) : null;
        return UserMember.builder()
                .userId(po.getUserId())
                .totalRecharge(po.getTotalRecharge())
                .level(MemberLevelConfig.builder().level(po.getVipLevel()).build())
                .packageTypeId(po.getPackageTypeId())
                .memberType(memberType)
                .levelUpgradeTime(po.getLevelUpgradeTime())
                .updateTime(po.getUpdateTime())
                .expireTime(po.getExpireTime())
                .dailyRate(po.getDailyRate())
                .build();
    }
}
