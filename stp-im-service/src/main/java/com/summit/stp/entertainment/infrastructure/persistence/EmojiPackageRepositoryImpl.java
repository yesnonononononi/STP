package com.summit.stp.entertainment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.entertainment.domain.model.EmojiPackage;
import com.summit.stp.entertainment.domain.repository.EmojiPackageRepository;
import com.summit.stp.entertainment.infrastructure.persistence.po.EmojiPackagePO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmojiPackageRepositoryImpl extends AbstractRepository<EmojiPackage, EmojiPackagePO> implements EmojiPackageRepository {

    public EmojiPackageRepositoryImpl(BaseMapper<EmojiPackagePO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public List<EmojiPackage> queryPackages(Integer status) {
        return findListBy(status, EmojiPackagePO::getStatus);
    }

    @Override
    protected EmojiPackagePO toPO(EmojiPackage domain) {
        if (domain == null) return null;
        return EmojiPackagePO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .coverImage(domain.getCoverImage())
                .status(domain.getStatus())
                .createTime(domain.getCreateTime())
                .updateTime(domain.getUpdateTime())
                .build();
    }

    @Override
    protected EmojiPackage toModel(EmojiPackagePO po) {
        if (po == null) return null;
        return EmojiPackage.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .coverImage(po.getCoverImage())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}

