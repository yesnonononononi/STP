package com.summit.stp.member.application.service.impl;

import com.summit.stp.member.application.service.MemberTypeService;
import com.summit.stp.shared.application.vo.MemberTypeVO;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.shared.api.dto.MemberTypeUpdateRequest;
import com.summit.stp.shared.result.Result;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberTypeServiceImpl implements MemberTypeService {

    @Override
    public Result<List<MemberTypeVO>> queryMemberTypes() {
        List<MemberTypeVO> res = Arrays.stream(MemberType.values())
                .filter(type -> type.getStatus() == 1)
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
        return Result.error("暂不支持动态更新枚举定义的会员类型");
    }

    @Override
    public Result<MemberTypeVO> queryTypeById(Long typeId) {
        MemberType memberType = MemberType.getById(typeId);
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
