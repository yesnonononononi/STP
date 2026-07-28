package com.summit.stp.entertainment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.entertainment.domain.model.EmojiPackage;
import com.summit.stp.entertainment.domain.repository.EmojiPackageRepository;
import com.summit.stp.entertainment.infrastructure.persistence.mapper.EmojiPackageMapper;
import com.summit.stp.entertainment.infrastructure.persistence.po.EmojiPackagePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class EmojiPackageRepositoryImpl implements EmojiPackageRepository {

    private final EmojiPackageMapper emojiPackageMapper;

    @Override
    public List<EmojiPackage> queryPackages(Integer status) {
        // 查询所有启用的表情包 (status = 1)
        List<EmojiPackagePO> packagePOs = emojiPackageMapper.selectList(
                new LambdaQueryWrapper<EmojiPackagePO>().eq(EmojiPackagePO::getStatus, status)
        );
        
        return packagePOs.stream().map(this::toDomain).toList();
    }

    private EmojiPackage toDomain(EmojiPackagePO po) {
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
