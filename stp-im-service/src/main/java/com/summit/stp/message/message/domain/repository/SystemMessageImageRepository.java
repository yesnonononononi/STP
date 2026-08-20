package com.summit.stp.message.message.domain.repository;

import com.summit.stp.message.message.domain.model.SystemMessageImage;

import java.util.List;
import java.util.Map;

public interface SystemMessageImageRepository {

    Map<Long, List<SystemMessageImage>> findByIds(List<Long> list);

    void saveImages(Long messageId, List<String> imageUrls);
}
