package com.summit.stp.member.domain.repository;

import com.summit.stp.member.domain.model.Member;

import java.util.List;

public interface MemberRepository {
    Member findMemberById(Long id);

    List<Member> findMemberByType(Long typeId, int status);
}
