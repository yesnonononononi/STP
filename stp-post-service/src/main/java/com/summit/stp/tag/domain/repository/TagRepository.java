package com.summit.stp.tag.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.service.SearchSuggest.SuggestVO;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.tag.domain.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository {
    Optional<Tag> findById(Long id);
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
