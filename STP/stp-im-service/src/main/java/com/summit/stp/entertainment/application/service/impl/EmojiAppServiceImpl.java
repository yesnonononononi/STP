package com.summit.stp.entertainment.application.service.impl;

import com.summit.stp.entertainment.application.service.EmojiAppService;
import com.summit.stp.entertainment.application.vo.EmojiPackageVO;
import com.summit.stp.entertainment.application.vo.EmojiVO;
import com.summit.stp.entertainment.domain.model.Emoji;
import com.summit.stp.entertainment.domain.model.EmojiPackage;
import com.summit.stp.entertainment.domain.repository.EmojiPackageRepository;
import com.summit.stp.entertainment.domain.repository.EmojiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmojiAppServiceImpl implements EmojiAppService {
    private final EmojiRepository emojiRepository;
    private final EmojiPackageRepository emojiPackageRepository;

    private EmojiVO toVO(Emoji emoji) {
        return EmojiVO.builder()
                .id(emoji.getId())
                .name(emoji.getName())
                .type(emoji.getType().getVal())
                .url(emoji.getUrl())
                .tiny(emoji.getTiny())
                .build();
    }

    private EmojiPackageVO toPackageVO(EmojiPackage pkg) {
        return EmojiPackageVO.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .build();
    }

    @Override
    public List<EmojiVO> queryList(Long packageId) {
        return emojiRepository.queryList(packageId).stream().map(this::toVO).toList();
    }

    @Override
    public List<EmojiPackageVO> queryPackages(Integer status) {
        return emojiPackageRepository.queryPackages(status).stream().map(this::toPackageVO).toList();
    }
}
