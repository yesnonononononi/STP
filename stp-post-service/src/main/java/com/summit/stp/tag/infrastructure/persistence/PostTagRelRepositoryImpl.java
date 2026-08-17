package com.summit.stp.tag.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.tag.domain.model.PostTag;
import com.summit.stp.tag.domain.repository.PostTagRelRepository;
import com.summit.stp.tag.infrastructure.persistence.po.PostTagRelPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostTagRelRepositoryImpl extends AbstractRepository<PostTag, PostTagRelPO> implements PostTagRelRepository {

    public PostTagRelRepositoryImpl(BaseMapper<PostTagRelPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public void save(PostTag rel) {
        if (rel == null) {
            return;
        }
        if (rel.getId() != null && findById(rel.getId()).isPresent()) {
            super.updateById(rel);
        } else {
            super.save(rel);
        }
    }

    @Override
    public void delete(Long id) {
        delete(id, PostTagRelPO::getId);
    }

    @Override
    public void deleteByPostId(Long postId) {
        delete(postId, PostTagRelPO::getPostId);
    }

    @Override
    public List<PostTag> findByPostId(Long postId) {
        return findListBy(postId, PostTagRelPO::getPostId);
    }

    @Override
    public List<PostTag> findByTagId(Long tagId) {
        return findListBy(tagId, PostTagRelPO::getTagId);
    }

    @Override
    public List<PostTag> findByPostIds(List<Long> postIds) {
        return findListIn(postIds, PostTagRelPO::getPostId);
    }

    @Override
    public void batchSave(Long postId, List<Long> tags) {
        if (tags == null || tags.isEmpty()) return;
        List<PostTagRelPO> list = tags.stream().map(tagId -> PostTagRelPO.builder().postId(postId).tagId(tagId).build())
                .toList();
        getBaseMapper().insert(list);
    }

    @Override
    protected PostTag toModel(PostTagRelPO po) {
        if (po == null) {
            return null;
        }
        return PostTag.builder()
                .id(po.getId())
                .postId(po.getPostId())
                .tagId(po.getTagId())
                .createTime(po.getCreateTime())
                .build();
    }

    @Override
    protected PostTagRelPO toPO(PostTag domain) {
        if (domain == null) {
            return null;
        }
        return PostTagRelPO.builder()
                .id(domain.getId())
                .postId(domain.getPostId() != null ? domain.getPostId() : 0L)
                .tagId(domain.getTagId() != null ? domain.getTagId() : 0L)
                .createTime(domain.getCreateTime())
                .build();
    }
}

