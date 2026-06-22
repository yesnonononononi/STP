package com.summit.stp.member.application.service;

import com.summit.stp.member.application.vo.MemberTypeVO;
import com.summit.stp.order.api.dto.MemberTypeUpdateRequest;
import com.summit.stp.shared.result.Result;

import java.util.List;

public interface MemberTypeService {
    Result<List<MemberTypeVO>> queryMemberTypes();

    Result<Void> updateMemberType(MemberTypeUpdateRequest request);


    Result<MemberTypeVO> queryTypeById(Long typeId);
}
