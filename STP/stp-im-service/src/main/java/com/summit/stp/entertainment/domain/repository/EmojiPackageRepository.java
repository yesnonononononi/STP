package com.summit.stp.entertainment.domain.repository;

import com.summit.stp.entertainment.domain.model.EmojiPackage;

import java.util.List;

public interface EmojiPackageRepository {
    List<EmojiPackage> queryPackages(Integer status);
}
