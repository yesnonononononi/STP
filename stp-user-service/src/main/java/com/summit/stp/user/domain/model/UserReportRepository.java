package com.summit.stp.user.domain.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.RepositoryTemplate;
import com.summit.stp.user.infrastructure.persistence.po.UserReportPO;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserReportRepository<T extends UserReport> extends RepositoryTemplate<UserReport, UserReportPO> {
    void save(UserReport report);

    Optional<UserReport> findById(Long id);

    void delete(Long id);

    Page<UserReport> queryReportsPage(Integer status, long page, long pageSize);

    long countPendingReports();

    long countProcessedReportsAfter(LocalDateTime startTime);


}
