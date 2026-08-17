package com.summit.stp.member.domain.repository;

import com.summit.stp.member.domain.model.Member;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MemberRepository<T> {
    Optional<T> findMemberById(Long id);

    List<T> findMemberByType(Long typeId, int status);

    Map<Long, Member> findMemberByIds(List<Long> packageIds);

    void save(T member);


    void delete(T memberById);

    List<T> list();

}

