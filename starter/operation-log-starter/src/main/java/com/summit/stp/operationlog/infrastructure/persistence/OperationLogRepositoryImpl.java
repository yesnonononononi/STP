package com.summit.stp.operationlog.infrastructure.persistence;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.operationlog.domain.model.Operation;
import com.summit.stp.operationlog.domain.repository.OperationLogRepository;
import com.summit.stp.operationlog.infrastructure.persistence.po.OperationLogPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@Slf4j
public class OperationLogRepositoryImpl extends AbstractRepository<Operation, OperationLogPO> implements OperationLogRepository {

    public OperationLogRepositoryImpl(BaseMapper<OperationLogPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public void save(Operation operation) {
        super.save(operation);
    }

    @Override
    public List<Operation> listBy(String type, Long uid, Integer page, Integer size) {
        LambdaQueryWrapper<OperationLogPO> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(type)) {
            queryWrapper.eq(OperationLogPO::getType, type);
        }
        if (uid != null) {
            queryWrapper.eq(OperationLogPO::getUserId, uid);
        }

        Page<OperationLogPO> pages = new Page<OperationLogPO>().setCurrent(page).setSize(size);
        List<OperationLogPO> records = getBaseMapper().selectPage(pages, queryWrapper).getRecords();
        return records.stream().map(this::toModel).toList();
    }

    @Override
    public void delete(Collection<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            getBaseMapper().deleteByIds(ids);
        }
    }

    @Override
    protected OperationLogPO toPO(Operation entity) {
        return OperationLogPO.toPO(entity);
    }

    @Override
    protected Operation toModel(OperationLogPO po) {
        return OperationLogPO.toDomain(po);
    }
}
