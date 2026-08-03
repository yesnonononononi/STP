package com.summit.stp.member.application.service;

import com.summit.stp.common.application.vo.MemberTypeVO;
import com.summit.stp.common.application.vo.MemberVO;
import com.summit.stp.common.result.Result;

import java.util.List;
import java.util.Map;

public interface MemberAppService {
    Result<MemberVO> queryMemberById(Long id);

    Result<List<MemberVO>> queryMemberByType(Long typeId, int status);


    Result<List<MemberTypeVO>> list();

    Result<MemberTypeVO> queryMemberTypeById(Long id);

    Result<Map<Long, MemberVO>> queryMemberByIds(List<Long> ids);
}
