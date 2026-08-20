package com.summit.stp.message.entertainment.domain.repository;

import com.summit.stp.message.entertainment.domain.model.EmojiPackage;

import java.util.List;

public interface EmojiPackageRepository {
    List<EmojiPackage> queryPackages(Integer status);
}
