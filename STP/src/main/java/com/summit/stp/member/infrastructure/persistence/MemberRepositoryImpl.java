package com.summit.stp.member.infrastructure.persistence;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.order.domain.exception.NoSuchMemberException;
import com.summit.stp.member.domain.model.Member;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.repository.MemberRepository;
import com.summit.stp.member.domain.repository.MemberTypeRepository;
import com.summit.stp.order.infrastructure.persistence.mapper.MemberMapper;
import com.summit.stp.member.infrastructure.persistence.po.MemberPackagePO;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    private final MemberMapper memberMapper;
    private final MemberTypeRepository memberTypeRepository;

    // 缓存会员详情，有效期 5 分钟 (300,000 毫秒)
    private final cn.hutool.cache.Cache<Long, MemberPackagePO> memberCache = cn.hutool.cache.CacheUtil.newTimedCache(300000);
    // 缓存会员分类详情，有效期 5 分钟
    private final cn.hutool.cache.Cache<Long, MemberType> typeCache = cn.hutool.cache.CacheUtil.newTimedCache(300000);

    @Override
    public Member findMemberById(Long id) {
        MemberPackagePO memberPO = memberCache.get(id, () -> {
            MemberPackagePO temp = memberMapper.selectById(id);
            if (temp == null) {
                throw new NoSuchMemberException();
            }
            return temp;
        });

        Long typeId = memberPO.getTypeId();
        MemberType type = typeCache.get(typeId, () -> memberTypeRepository.selectById(typeId));

        return convertToDomain(memberPO, type);
    }

    @Override
    public List<Member> findMemberByType(Long typeId, int status) {
        List<MemberPackagePO> membersWithType = memberMapper.findMemberByType(typeId, status);
        return membersWithType.stream()
                .map(po -> convertToDomain(po, null))
                .toList();
    }

    private Member convertToDomain(MemberPackagePO po, @Nullable MemberType type) {
        return Member.builder()
                .id(po.getId())
                .name(po.getName())
                .price(po.getPrice())
                .duration(po.getDuration())
                .discount(po.getDiscount())
                .description(po.getDescription())
                .type(type)
                .dailyRate(po.getDailyRate())
                .priority(po.getPriority())
                .build();
    }
}
