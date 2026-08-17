package com.summit.stp.entertainment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.entertainment.domain.model.Emoji;
import com.summit.stp.entertainment.domain.repository.EmojiRepository;
import com.summit.stp.entertainment.infrastructure.persistence.po.EmojiPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmojiRepositoryImpl extends AbstractRepository<Emoji, EmojiPO> implements EmojiRepository {

    public EmojiRepositoryImpl(BaseMapper<EmojiPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public List<Emoji> queryList(Long packageId) {
        return findListBy(packageId, EmojiPO::getPackageId);
    }

    @Override
    protected EmojiPO toPO(Emoji domain) {
        if (domain == null) return null;
        return EmojiPO.builder()
                .id(domain.getId())
                .packageId(domain.getPackageId())
                .name(domain.getName())
                .type(domain.getType() != null ? domain.getType().getVal() : null)
                .url(domain.getUrl())
                .tiny(domain.getTiny())
                .build();
    }

    @Override
    protected Emoji toModel(EmojiPO po) {
        if (po == null) return null;
        return Emoji.builder()
                .id(po.getId())
                .packageId(po.getPackageId())
                .name(po.getName())
                .type(po.getType() != null ? Emoji.Type.fromCode(po.getType()) : null)
                .url(po.getUrl())
                .tiny(po.getTiny())
                .build();
    }
}

