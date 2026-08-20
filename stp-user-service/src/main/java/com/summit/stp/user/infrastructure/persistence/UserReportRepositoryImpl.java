package com.summit.stp.user.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.user.domain.model.UserReport;
import com.summit.stp.user.domain.model.UserReportRepository;
import com.summit.stp.user.domain.model.UserReportStatusEnum;
import com.summit.stp.user.infrastructure.persistence.po.UserReportPO;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserReportRepositoryImpl extends AbstractRepository<UserReport, UserReportPO> implements UserReportRepository<UserReport> {

    public UserReportRepositoryImpl(BaseMapper<UserReportPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public void save(UserReport report) {
        super.save(report);
    }

    @Override
    public Optional<UserReport> findById(Long id) {
        if (id == null) return Optional.empty();
        UserReportPO po = getBaseMapper().selectById(id);
        return Optional.ofNullable(toModel(po));
    }

    @Override
    public void delete(Long id) {
        delete(id, UserReportPO::getId);
    }

    @Override
    public Page<UserReport> queryReportsPage(Integer status, long page, long pageSize) {
        LambdaQueryWrapper<UserReportPO> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(UserReportPO::getStatus, status);
        }
        wrapper.orderByDesc(UserReportPO::getCreateTime);

        Page<UserReportPO> poPage = getBaseMapper().selectPage(new Page<>(page, pageSize), wrapper);
        Page<UserReport> modelPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        modelPage.setRecords(poPage.getRecords().stream().map(this::toModel).toList());
        return modelPage;
    }

    @Override
    public long countPendingReports() {
        LambdaQueryWrapper<UserReportPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserReportPO::getStatus, 0);
        Long count = getBaseMapper().selectCount(wrapper);
        return count != null ? count : 0L;
    }

    @Override
    public long countProcessedReportsAfter(LocalDateTime startTime) {
        if (startTime == null) return 0L;
        LambdaQueryWrapper<UserReportPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(UserReportPO::getStatus, 0)
               .ge(UserReportPO::getUpdateTime, startTime);
        Long count = getBaseMapper().selectCount(wrapper);
        return count != null ? count : 0L;
    }

    @Override
    protected UserReportPO toPO(UserReport entity) {
        if (entity == null) return null;
        UserReportPO po = new UserReportPO();
        po.setId(entity.getId());
        po.setReporterId(entity.getReporterId());
        po.setReportedId(entity.getReportedId());
        po.setReason(entity.getReason());
        po.setStatus(entity.getStatus() != null ? entity.getStatus().getCode() : 0);
        po.setCreateTime(entity.getCreateTime());
        po.setUpdateTime(entity.getUpdateTime());
        return po;
    }

    @Override
    protected UserReport toModel(UserReportPO po) {
        if (po == null) return null;
        return UserReport.builder()
                .id(po.getId())
                .reporterId(po.getReporterId())
                .reportedId(po.getReportedId())
                .reason(po.getReason())
                .status(UserReportStatusEnum.fromCode(po.getStatus()))
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}
