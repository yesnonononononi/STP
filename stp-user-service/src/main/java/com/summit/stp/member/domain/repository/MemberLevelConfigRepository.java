package com.summit.stp.member.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.member.domain.model.MemberLevelConfig;

import java.util.List;
import java.util.Optional;

public interface MemberLevelConfigRepository<T> {
    void save(MemberLevelConfig config);

    Optional<T> findByLevel(Long level);

    Optional<T> findById(Long id);

    List<T> findAll();

    void deleteByLevel(Long level);

    void deleteById(Long id);

    Page<T> findByPage(Integer integer, Integer pageSize);

    void update(T config);
}
