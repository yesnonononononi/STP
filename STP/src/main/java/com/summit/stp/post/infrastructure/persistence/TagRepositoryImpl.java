package com.summit.stp.post.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.post.domain.repository.TagRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.TagMapper;
import com.summit.stp.post.infrastructure.persistence.po.TagPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {
    private final TagMapper tagMapper;

    @Override
    public TagPO findById(Long id) {
        return tagMapper.selectById(id);
    }

    @Override
    public TagPO findByName(String name) {
        return tagMapper.selectOne(
                new LambdaQueryWrapper<TagPO>().eq(TagPO::getTagName, name)
        );
    }

    @Override
    public void save(TagPO tag) {
        if (tag.getId() == null || tag.getId() == 0) {
            tagMapper.insert(tag);
        } else {
            tagMapper.updateById(tag);
        }
    }

    @Override
    public void delete(Long id) {
        tagMapper.deleteById(id);
    }

    @Override
    public Page<TagPO> queryByPage(long page, long pageSize) {
        return tagMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<TagPO>().orderByDesc(TagPO::getUseCount)
        );
    }

    @Override
    public List<TagPO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return tagMapper.selectBatchIds(ids);
    }
}
