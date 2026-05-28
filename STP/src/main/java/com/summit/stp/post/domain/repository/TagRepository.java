package com.summit.stp.post.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.post.infrastructure.persistence.po.TagPO;
import java.util.List;

public interface TagRepository {
    TagPO findById(Long id);
    TagPO findByName(String name);
    void save(TagPO tag);
    void delete(Long id);
    Page<TagPO> queryByPage(long page, long pageSize);
    List<TagPO> findByIds(List<Long> ids);
}
