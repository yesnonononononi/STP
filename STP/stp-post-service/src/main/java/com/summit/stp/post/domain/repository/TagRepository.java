package com.summit.stp.post.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.post.domain.model.Tag;
import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.service.SearchSuggest.SuggestVO;

import java.util.List;

public interface TagRepository {
    Tag findById(Long id);
    Tag findByUuid(String uuid);
    List<Long> findIdsByUuids(List<String> uuids);
    Tag findByName(String name);
    void save(Tag tag);
    void delete(Long id);
    Page<Tag> queryByPage(long page, long pageSize);
    List<Tag> findByIds(List<Long> ids);

    Result<SuggestVO> searchTag(String keyword, Integer limit);
    void incrementUseCount(Long id, Integer delta);

    List<Tag> getRecentTag(Integer limit);

    List<Tag> queryTagByUseCount(Integer limit);


}
