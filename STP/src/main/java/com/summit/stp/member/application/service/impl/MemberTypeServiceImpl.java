package com.summit.stp.member.application.service.impl;

import com.summit.stp.order.api.dto.MemberTypeUpdateRequest;
import com.summit.stp.member.application.service.MemberTypeService;
import com.summit.stp.member.application.vo.MemberTypeVO;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.repository.MemberTypeRepository;
import com.summit.stp.shared.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberTypeServiceImpl implements MemberTypeService {
    private final MemberTypeRepository memberTypeRepository;

    @Override
    public Result<List<MemberTypeVO>> queryMemberTypes() {
        List<MemberType> list = memberTypeRepository.list(1);
        List<MemberTypeVO> res = list.stream()
                .map(memberType -> MemberTypeVO.builder()
                        .id(memberType.getTypeId())
                        .name(memberType.getTypeName())
                        .description(memberType.getDescription())
                        .build()
                ).toList();
        return Result.success(res);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateMemberType(MemberTypeUpdateRequest request) {
        if (request.getId() == null) {
            return Result.error("ID不能为空");
        }
        MemberType existing = memberTypeRepository.selectById(request.getId());
        if (existing == null) {
            return Result.error("会员类型不存在");
        }

        MemberType updated = MemberType.builder()
                .typeId(existing.getTypeId())
                .typeName(request.getName() != null ? request.getName() : existing.getTypeName())
                .description(request.getDescription() != null ? request.getDescription() : existing.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : existing.getStatus())
                .build();

        memberTypeRepository.save(updated);
        return Result.success();
    }

    @Override
    public Result<MemberTypeVO> queryTypeById(Long typeId) {
        MemberType memberType = memberTypeRepository.selectById(typeId);
        if (memberType == null) {
            return Result.error("会员类型不存在");
        }
        return Result.success(MemberTypeVO.builder()
                .id(memberType.getTypeId())
                .name(memberType.getTypeName())
                .description(memberType.getDescription())
                .build());
    }
}
