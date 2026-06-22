package com.summit.stp.post.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.post.domain.model.Tag;
import com.summit.stp.post.domain.repository.TagRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostTagRelMapper;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.mapper.TagMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostTagRelPO;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import com.summit.stp.post.infrastructure.persistence.po.TagPO;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.service.SearchSuggest.AbstractSuggest;
import com.summit.stp.shared.service.SearchSuggest.SuggestDto;
import com.summit.stp.shared.service.SearchSuggest.SuggestListVO;
import com.summit.stp.shared.service.SearchSuggest.SuggestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl extends AbstractSuggest<TagPO> implements TagRepository {
    private final TagMapper tagMapper;
    private final PostsMapper postsMapper;
    private final PostTagRelMapper postTagRelMapper;

    @Override
    public Tag findById(Long id) {
        TagPO tagPO = tagMapper.selectById(id);
        return toDomain(tagPO);
    }

    @Override
    public Tag findByName(String name) {
        TagPO tagPO = tagMapper.selectOne(
                new LambdaQueryWrapper<TagPO>().eq(TagPO::getTagName, name)
        );
        return toDomain(tagPO);
    }

    @Override
    public void save(Tag tag) {
        if (tag == null) {
            return;
        }
        TagPO po = toPO(tag);
        if (po.getId() == null || po.getId() == 0) {
            tagMapper.insert(po);
        } else {
            tagMapper.updateById(po);
        }
    }

    @Override
    public void delete(Long id) {
        tagMapper.deleteById(id);
    }

    @Override
    public Page<Tag> queryByPage(long page, long pageSize) {
        Page<TagPO> poPage = tagMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<TagPO>().orderByDesc(TagPO::getUseCount)
        );
        Page<Tag> domainPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        List<Tag> domainRecords = poPage.getRecords().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        domainPage.setRecords(domainRecords);
        return domainPage;
    }

    @Override
    public List<Tag> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<TagPO> pos = tagMapper.selectBatchIds(ids);
        return pos.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Result<SuggestVO> searchTag(String keyword, Integer limit) {
        SuggestVO suggestList = getSuggestList(
                SuggestDto.builder()
                        .keyword(keyword)
                        .limit(limit)
                        .build()
                , TagPO::getTagName, TagPO::getId
                , TagPO::getUseCount);
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
        // 1. 查询当前用户最新的帖子列表
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

        // 2. 根据帖子 ID 列表，从关系表中找出关联的 tagId 列表
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

        // 3. 批量查询标签并转换为领域实体 (保持时序)
        LambdaQueryWrapper<TagPO> tagWrapper = new LambdaQueryWrapper<TagPO>().in(TagPO::getId, tagIds).orderByDesc(TagPO::getUseCount);
        List<TagPO> tagPOs = tagMapper.selectList(tagWrapper);
        Map<Long, TagPO> tagPOMap = tagPOs.stream()
                .collect(Collectors.toMap(TagPO::getId, po -> po));

        return tagIds.stream()
                .map(tagPOMap::get)
                .filter(Objects::nonNull)
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Tag> queryTagByUseCount(Integer limit) {
        LambdaQueryWrapper<TagPO> wrapper = new LambdaQueryWrapper<TagPO>().eq(TagPO::getStatus, 1).orderByDesc(TagPO::getUseCount).last("limit " + limit);
        List<TagPO> tagPOS = tagMapper.selectList(wrapper);
        return tagPOS.stream().map(this::toDomain).toList();
    }


    @Override
    protected BaseMapper<TagPO> getBaseMapper() {
        return tagMapper;
    }

    private Tag toDomain(TagPO po) {
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

    private TagPO toPO(Tag tag) {
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
