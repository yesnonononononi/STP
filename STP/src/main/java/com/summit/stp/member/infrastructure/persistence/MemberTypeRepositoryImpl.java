package com.summit.stp.member.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.repository.MemberTypeRepository;
import com.summit.stp.order.infrastructure.persistence.mapper.MemberTypeMapper;
import com.summit.stp.member.infrastructure.persistence.po.MemberTypePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberTypeRepositoryImpl implements MemberTypeRepository {
    private final MemberTypeMapper memberTypeMapper;

    @Override
    public List<MemberType> queryMemberTypeByName() {
        List<MemberTypePO> pos = memberTypeMapper.selectList(null);
        return pos.stream()
                .map(po -> MemberType.builder()
                        .typeId(po.getId())
                        .typeName(po.getName())
                        .status(po.getStatus())
                        .description(po.getDescription())
                        .priority(po.getPriority())
                        .build())
                .toList();
    }

    @Override
    public MemberType selectById(Long typeId) {
        MemberTypePO po = memberTypeMapper.selectById(typeId);
        if (po == null) {
            return null;
        }
        return MemberType.builder()
                .typeId(po.getId())
                .typeName(po.getName())
                .status(po.getStatus())
                .description(po.getDescription())
                .priority(po.getPriority())
                .build();
    }

    @Override
    public List<MemberType> list(int status) {
        List<MemberTypePO> pos = memberTypeMapper.selectList(new LambdaQueryWrapper<MemberTypePO>().eq(MemberTypePO::getStatus, status));
        return pos.stream()
                .map(po -> MemberType.builder()
                        .typeId(po.getId())
                        .typeName(po.getName())
                        .status(po.getStatus())
                        .description(po.getDescription())
                        .priority(po.getPriority())
                        .build())
                .toList();
    }

    @Override
    public void save(MemberType memberType) {
        MemberTypePO po = new MemberTypePO();
        po.setId(memberType.getTypeId());
        po.setName(memberType.getTypeName());
        po.setStatus(memberType.getStatus());
        po.setDescription(memberType.getDescription());
        po.setPriority(memberType.getPriority());
        if (po.getId() == null) {
            memberTypeMapper.insert(po);
        } else {
            memberTypeMapper.updateById(po);
        }
    }
}
