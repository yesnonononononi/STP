package com.summit.stp.message.entertainment.domain.repository;

import com.summit.stp.message.entertainment.domain.model.Emoji;

import java.util.List;

public interface EmojiRepository {
    List<Emoji> queryList(Long packageId);
}
