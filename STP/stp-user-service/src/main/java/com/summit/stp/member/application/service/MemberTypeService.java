package com.summit.stp.member.application.service;

import com.summit.stp.common.application.vo.MemberTypeVO;
import com.summit.stp.common.application.dto.MemberTypeUpdateRequest;
import com.summit.stp.common.result.Result;

import java.util.List;

public interface MemberTypeService {
    Result<List<MemberTypeVO>> queryMemberTypes();

    Result<Void> updateMemberType(MemberTypeUpdateRequest request);


    Result<MemberTypeVO> queryTypeById(Long typeId);
}
