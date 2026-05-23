package com.summit.stp.member.domain.repository;

import com.summit.stp.member.domain.model.MemberType;

import java.util.List;

public interface MemberTypeRepository {
    List<MemberType> queryMemberTypeByName();

    MemberType selectById(Long typeId);

    List<MemberType> list(int status);

    void save(MemberType memberType);
}
