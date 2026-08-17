package com.summit.stp.member.application.service;

import com.summit.stp.member.api.dto.MemberTypeUpdateRequest;
import com.summit.stp.user.api.vo.MemberTypeVO;
import com.summit.stp.common.application.api.result.Result;

import java.util.List;

public interface MemberTypeService {
    Result<List<MemberTypeVO>> queryMemberTypes();

    Result<Void> updateMemberType(MemberTypeUpdateRequest request);


    Result<MemberTypeVO> queryTypeById(Long typeId);
}

