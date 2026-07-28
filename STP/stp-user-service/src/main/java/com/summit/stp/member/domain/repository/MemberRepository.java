package com.summit.stp.member.domain.repository;

import com.summit.stp.common.application.vo.MemberVO;
import com.summit.stp.member.domain.model.Member;

import java.util.List;
import java.util.Map;

public interface MemberRepository {
    Member findMemberById(Long id);

    List<Member> findMemberByType(Long typeId, int status);

    Map<Long, MemberVO> findMemberByIds(List<Long> packageIds);
}
