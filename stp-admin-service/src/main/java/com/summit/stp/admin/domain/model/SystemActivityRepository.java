package com.summit.stp.admin.domain.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Optional;

public interface SystemActivityRepository<T extends SystemActivity> {
    void save(SystemActivity activity);

    Optional<SystemActivity> findById(Long id);

    List<SystemActivity> findRecentActivities(int limit);

    Page<SystemActivity> queryActivitiesPage(String type, String module, long page, long pageSize);
}
