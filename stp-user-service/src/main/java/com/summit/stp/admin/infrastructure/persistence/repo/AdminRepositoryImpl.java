package com.summit.stp.admin.infrastructure.persistence.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.admin.domain.model.Admin;
import com.summit.stp.admin.domain.repo.AdminRepository;
import com.summit.stp.admin.infrastructure.persistence.po.AdminPO;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdminRepositoryImpl extends AbstractRepository<Admin, AdminPO> implements AdminRepository {

    public AdminRepositoryImpl(BaseMapper<AdminPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public void save(Admin admin) {
        if (admin == null) return;
        if (admin.getId() != null && findById(admin.getId()).isPresent()) {
            super.updateById(admin);
        } else {
            super.save(admin);
        }
    }

    @Override
    public List<Admin> list(Integer page, Integer pageSize) {
        Page<AdminPO> adminPage = new Page<>(page, pageSize);
        return getBaseMapper().selectPage(
                adminPage, new LambdaQueryWrapper<AdminPO>().orderByDesc(AdminPO::getOrder)
        )
                .getRecords()
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public Admin findByUserId(Long uid) {
        return findBy(uid, AdminPO::getUserId).orElse(null);
    }

    @Override
    protected AdminPO toPO(Admin entity) {
        return AdminPO.toPO(entity);
    }

    @Override
    protected Admin toModel(AdminPO po) {
        return AdminPO.toDomain(po);
    }
}

