package com.summit.stp.member.infrastructure.persistence;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.summit.stp.common.application.vo.MemberVO;
import com.summit.stp.member.domain.model.Member;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.repository.MemberRepository;
import com.summit.stp.member.infrastructure.persistence.po.MemberPackagePO;
import com.summit.stp.member.infrastructure.persistence.mapper.MemberMapper;
import com.summit.stp.common.application.domain.exception.NoSuchMemberException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    private final MemberMapper memberMapper;

    // 缓存会员详情，有效期 5 分钟 (300,000 毫秒)
    private final Cache<Long, MemberPackagePO> memberCache = CacheUtil.newTimedCache(300000);

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
        MemberType type = MemberType.getById(typeId);

        return convertToDomain(memberPO, type);
    }

    @Override
    public List<Member> findMemberByType(Long typeId, int status) {
        MemberType type = MemberType.getById(typeId);
        if (type == null || type.getStatus() != status) {
            return List.of();
        }
        List<MemberPackagePO> membersWithType = memberMapper.findMemberByType(typeId);
        return membersWithType.stream()
                .map(po -> convertToDomain(po, type))
                .toList();
    }

    @Override
    public Map<Long, MemberVO> findMemberByIds(List<Long> packageIds) {
        List<MemberPackagePO> members = memberMapper.selectByIds(packageIds);
        return members.stream().collect(Collectors.toMap(MemberPackagePO::getId, po->MemberVO.builder().name(po.getName()).build()));
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
                .typeId(po.getTypeId())
                .dailyRate(po.getDailyRate())
                .priority(po.getPriority())
                .stock(po.getStock())
                .build();
    }
}
