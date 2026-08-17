package com.summit.stp.admin.domain.repo;

import com.summit.stp.admin.domain.model.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminRepository {
    void save(Admin admin);

    List<Admin> list(Integer page, Integer pageSize);

    Optional<Admin> findById(Long id);

    Admin findByUserId(Long userId);
}

