package com.summit.stp.member.domain.repository;

import com.summit.stp.member.domain.model.MemberLevelConfig;

import java.util.List;

public interface MemberLevelConfigRepository {
    void save(MemberLevelConfig config);

    MemberLevelConfig findByLevel(Long level);

    List<MemberLevelConfig> findAll();

    void deleteByLevel(Long level);
}
