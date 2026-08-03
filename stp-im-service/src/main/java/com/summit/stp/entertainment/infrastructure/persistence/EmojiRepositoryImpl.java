package com.summit.stp.entertainment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.entertainment.domain.model.Emoji;
import com.summit.stp.entertainment.domain.repository.EmojiRepository;
import com.summit.stp.entertainment.infrastructure.persistence.mapper.EmojiMapper;
import com.summit.stp.entertainment.infrastructure.persistence.mapper.EmojiPackageMapper;
import com.summit.stp.entertainment.infrastructure.persistence.po.EmojiPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
@RequiredArgsConstructor
@Repository
public class EmojiRepositoryImpl implements EmojiRepository {
    private final EmojiMapper emojiMapper;
    private final EmojiPackageMapper emojiPackageMapper;

    @Override
    public List<Emoji> queryList(Long packageId) {
        List<EmojiPO> emojiPOS = emojiMapper.selectList(new LambdaQueryWrapper<EmojiPO>().eq(EmojiPO::getPackageId, packageId));
        return emojiPOS.stream()
                .map(this::toDomain)
                .toList();
    }

    private Emoji toDomain(EmojiPO po){
        return Emoji.builder()
                .id(po.getId())
                .packageId(po.getPackageId())
                .name(po.getName())
                .type(Emoji.Type.fromCode(po.getType()))
                .url(po.getUrl())
                .tiny(po.getTiny())
                .build();
    }
}
