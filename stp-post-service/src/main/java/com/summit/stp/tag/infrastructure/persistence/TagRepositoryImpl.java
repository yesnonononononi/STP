package com.summit.stp.tag.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.service.SearchSuggest.SuggestListVO;
import com.summit.stp.common.application.service.SearchSuggest.SuggestVO;
import com.summit.stp.common.auth.UserHolder;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import com.summit.stp.tag.domain.model.Tag;
import com.summit.stp.tag.domain.repository.TagRepository;
import com.summit.stp.tag.infrastructure.persistence.mapper.PostTagRelMapper;
import com.summit.stp.tag.infrastructure.persistence.mapper.TagMapper;
import com.summit.stp.tag.infrastructure.persistence.po.PostTagRelPO;
import com.summit.stp.tag.infrastructure.persistence.po.TagPO;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class TagRepositoryImpl extends AbstractRepository<Tag, TagPO> implements TagRepository {
    private final TagMapper tagMapper;
    private final PostsMapper postsMapper;
    private final PostTagRelMapper postTagRelMapper;

    public TagRepositoryImpl(TagMapper tagMapper, PostsMapper postsMapper, PostTagRelMapper postTagRelMapper) {
        super(tagMapper);
        this.tagMapper = tagMapper;
        this.postsMapper = postsMapper;
        this.postTagRelMapper = postTagRelMapper;
    }

    @Override
    public Tag findByName(String name) {
        return findBy(name, TagPO::getTagName).orElse(null);
    }

    @Override
    public void save(Tag tag) {
        if (tag == null) {
            return;
        }
        if (tag.getId() != null && findById(tag.getId()).isPresent()) {
            super.updateById(tag);
        } else {
            super.save(tag);
        }
    }

    @Override
    public void delete(Long id) {
        delete(id, TagPO::getId);
    }

    @Override
    public Page<Tag> queryByPage(long page, long pageSize) {
        Page<TagPO> poPage = getBaseMapper().selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<TagPO>().orderByDesc(TagPO::getUseCount)
        );
        Page<Tag> domainPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        List<Tag> domainRecords = poPage.getRecords().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        domainPage.setRecords(domainRecords);
        return domainPage;
    }

    @Override
    public List<Tag> findByIds(List<Long> ids) {
        return findListIn(ids, TagPO::getId);
    }

    @Override
    public Result<SuggestVO> searchTag(String keyword, Integer limit) {
        String kw = keyword != null ? keyword.trim() : "";
        LambdaQueryWrapper<TagPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(kw), TagPO::getTagName, kw).last("limit " + limit);
        List<TagPO> res = getBaseMapper().selectList(wrapper);

        SuggestVO suggestList = SuggestVO.builder()
                .limit(limit)
                .suggestList(res.stream().map(item -> new SuggestListVO(item.getId(), item.getTagName(), item.getUseCount())).toList())
                .keyword(kw)
                .total(res.size())
                .build();

        List<SuggestListVO> list = suggestList.getSuggestList()
                .stream()
                .filter(item -> item.getExtra() != null)
                .sorted((o1, o2) -> ((Integer) o2.getExtra()).compareTo((Integer) o1.getExtra()))
                .toList();

        suggestList.setSuggestList(list);
        return Result.success(suggestList);
    }

    @Override
    public void incrementUseCount(Long id, Integer delta) {
        if (id == null || delta == null || delta == 0) {
            return;
        }
        tagMapper.incrUseCount(id, delta);
    }

    @Override
    public List<Tag> getRecentTag(Integer limit) {
        LambdaQueryWrapper<PostsPO> postWrapper = new LambdaQueryWrapper<PostsPO>()
                .eq(PostsPO::getCreatorId, UserHolder.getUser().getId())
                .orderByDesc(PostsPO::getCreateTime);
        postWrapper.last("limit " + limit);

        List<Long> postIds = postsMapper.selectList(postWrapper).stream()
                .map(PostsPO::getId)
                .toList();
        if (postIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tagIds = postTagRelMapper.selectList(
                        new LambdaQueryWrapper<PostTagRelPO>()
                                .in(PostTagRelPO::getPostId, postIds)
                                .orderByDesc(PostTagRelPO::getId)
                ).stream()
                .map(PostTagRelPO::getTagId)
                .distinct()
                .limit(limit)
                .toList();

        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<TagPO> tagWrapper = new LambdaQueryWrapper<TagPO>().in(TagPO::getId, tagIds).orderByDesc(TagPO::getUseCount);
        List<TagPO> tagPOs = getBaseMapper().selectList(tagWrapper);
        Map<Long, TagPO> tagPOMap = tagPOs.stream()
                .collect(Collectors.toMap(TagPO::getId, po -> po));

        return tagIds.stream()
                .map(tagPOMap::get)
                .filter(Objects::nonNull)
                .map(this::toModel)
                .toList();
    }

    @Override
    public List<Tag> queryTagByUseCount(Integer limit) {
        LambdaQueryWrapper<TagPO> wrapper = new LambdaQueryWrapper<TagPO>().eq(TagPO::getStatus, 1).orderByDesc(TagPO::getUseCount).last("limit " + limit);
        List<TagPO> tagPOS = getBaseMapper().selectList(wrapper);
        return tagPOS.stream().map(this::toModel).toList();
    }

    @Override
    protected Tag toModel(TagPO po) {
        if (po == null) {
            return null;
        }
        return Tag.builder()
                .id(po.getId())
                .tagName(po.getTagName())
                .sort(po.getSort())
                .useCount(po.getUseCount())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .build();
    }

    @Override
    protected TagPO toPO(Tag tag) {
        if (tag == null) {
            return null;
        }
        TagPO po = new TagPO();
        po.setId(tag.getId());
        po.setTagName(tag.getTagName());
        po.setSort(tag.getSort());
        po.setUseCount(tag.getUseCount());
        po.setStatus(tag.getStatus());
        po.setCreateTime(tag.getCreateTime());
        return po;
    }
}

