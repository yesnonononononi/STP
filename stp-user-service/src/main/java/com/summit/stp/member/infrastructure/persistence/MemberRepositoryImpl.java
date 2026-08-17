package com.summit.stp.member.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.member.domain.model.Member;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.repository.MemberRepository;
import com.summit.stp.member.infrastructure.persistence.mapper.MemberMapper;
import com.summit.stp.member.infrastructure.persistence.po.MemberPackagePO;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class MemberRepositoryImpl extends AbstractRepository<Member, MemberPackagePO> implements MemberRepository<Member> {


    public MemberRepositoryImpl(MemberMapper memberMapper) {
        super(memberMapper);
    }

    @Override
    public Optional<Member> findMemberById(Long id) {
       return findById(id);
    }

    @Override
    public List<Member> findMemberByType(Long typeId, int status) {
        MemberType type = MemberType.getById(typeId);
        if (type == null || type.getStatus() != status) {
            return List.of();
        }
        return  findListBy(typeId,MemberPackagePO::getTypeId);
    }

    @Override
    public Map<Long, Member> findMemberByIds(List<Long> packageIds) {
        return findList(packageIds).stream().collect(Collectors.toMap(Member::getId, Function.identity()));
    }

    @Override
    public List<Member> list() {
        return getBaseMapper().selectList(new LambdaQueryWrapper<>()).stream().map(this::toModel).toList();
    }


    @Override
    protected MemberPackagePO toPO(Member member) {
        if (member == null) return null;
        return MemberPackagePO.builder()
                .id(member.getId())
                .name(member.getName())
                .price(member.getPrice())
                .duration(member.getDuration())
                .discount(member.getDiscount())
                .description(member.getDescription())
                .typeId(member.getTypeId())
                .dailyRate(member.getDailyRate())
                .priority(member.getPriority())
                .stock(member.getStock())
                .createTime(member.getCreateTime())
                .build();
    }

    @Override
    protected Member toModel(MemberPackagePO po) {
        if (po == null) return null;
        Long typeId = po.getTypeId();
        MemberType type = typeId != null ? MemberType.getById(typeId) : null;
        return convertToDomain(po, type);
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
                .createTime(po.getCreateTime())
                .dailyRate(po.getDailyRate())
                .priority(po.getPriority())
                .stock(po.getStock())
                .build();
    }
}


