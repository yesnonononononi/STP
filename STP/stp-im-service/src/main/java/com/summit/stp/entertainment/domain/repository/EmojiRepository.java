package com.summit.stp.entertainment.domain.repository;

import com.summit.stp.entertainment.domain.model.Emoji;

import java.util.List;

public interface EmojiRepository {
    List<Emoji> queryList(Long packageId);
}
