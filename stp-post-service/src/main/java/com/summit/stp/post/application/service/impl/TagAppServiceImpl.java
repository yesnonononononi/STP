package com.summit.stp.post.application.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.service.SearchSuggest.SuggestVO;
import com.summit.stp.common.result.CursorPageResult;
import com.summit.stp.common.result.Result;
import com.summit.stp.post.application.command.UpdateTagCommand;
import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.application.service.PostQueryService;
import com.summit.stp.post.application.service.TagAppService;
import com.summit.stp.post.application.service.TagCacheProvider;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.application.vo.TagVO;
import com.summit.stp.post.domain.model.Tag;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.post.domain.repository.TagRepository;
import com.summit.stp.post.infrastructure.persistence.PostRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagAppServiceImpl implements TagAppService {
    private final TagRepository tagRepository;
    private final PostTagRelRepository postTagRelRepository;
    private final PostCacheProvider postCacheProvider;
    private final TagCacheProvider tagCacheProvider;
    private final PostQueryService postQueryService;
    private final PostRepositoryImpl postRepositoryImpl;

    @Override
    public TagVO getTagById(Long id) {
        Tag tag = tagRepository.findById(id);
        return tag == null ? null : convertToVO(tag);
    }

    @Override
    public TagVO getTagByUuid(String uuid) {
        Tag tag = tagRepository.findByUuid(uuid);
        return tag == null ? null : convertToVO(tag);
    }

    @Override
    public TagVO getTagByName(String name) {
        Tag tag = tagRepository.findByName(name);
        return tag == null ? null : convertToVO(tag);
    }

    @Override
    public void createTag(String content) {
        Tag tag = Tag.builder()
                .uuid(IdUtil.fastSimpleUUID())
                .tagName(content)
                .sort(0)
                .status(0)
                .useCount(0)
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        tagRepository.save(tag);
    }

    @Override
    public void updateTag(UpdateTagCommand command) {
        Tag tag = tagRepository.findByUuid(command.getId());
        if (tag == null) {
            throw new ParameterException("标签不存在");
        }
        tag.updateInfo(command.getTagName(), command.getSort(), command.getStatus());
        tagRepository.save(tag);
        try {
            postCacheProvider.deleteTagDetail(tag.getId());
        } catch (Exception e) {
            // 忽略缓存清除异常
        }
    }

    @Override
    public void deleteTag(Long id) {
        tagRepository.delete(id);
        try {
            postCacheProvider.deleteTagDetail(id);
        } catch (Exception e) {
            // 忽略缓存清除异常
        }
    }

    @Override
    public void deleteTagByUuid(String uuid) {
        Tag tag = tagRepository.findByUuid(uuid);
        if (tag != null) {
            tagRepository.delete(tag.getId());
            try {
                postCacheProvider.deleteTagDetail(tag.getId());
            } catch (Exception e) {
                // 忽略缓存清除异常
            }
        }
    }

    @Override
    public Page<TagVO> getTagPage(long page, long pageSize) {
        Page<Tag> domainPage = tagRepository.queryByPage(page, pageSize);
        Page<TagVO> voPage = new Page<>(domainPage.getCurrent(), domainPage.getSize(), domainPage.getTotal());
        
        List<TagVO> voList = domainPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
                
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Result<SuggestVO> searchTag(String keyword, Integer limit) {
        return tagRepository.searchTag(keyword, limit);
    }

    @Override
    public List<TagVO> getRecentTag(Integer limit) {
        if(limit == null || limit <= 0 || limit > 100){
            limit = 10;
        }
        List<Tag> list = tagRepository.getRecentTag(limit);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public CursorPageResult<PostVO> getPostsByHotTag(String tagId, String cursor, Integer limit) {
        Long actualTagId = parseTagId(tagId);
        List<Long> postIds;
        try {
            postIds = tagCacheProvider.getPostsHotTag(actualTagId, cursor, limit);
        } catch (Exception e) {
            log.error("【标签模块】获取标签热门帖子失败,降级db，tagId={}, cursor={}, limit={}", tagId, cursor, limit, e);
            postIds = postRepositoryImpl.getHotPostsByTag(actualTagId, cursor, limit);
        }

        List<PostVO> byPostIds = postQueryService.getByPostIds(postIds);
        CursorPageResult<PostVO> postVOCursorPageResult = new CursorPageResult<>();
        if (byPostIds != null && !byPostIds.isEmpty()) {
            postVOCursorPageResult.setCursor(byPostIds.getLast().getHotScore() + "_" + byPostIds.getLast().getId());
            postVOCursorPageResult.setHasMore(byPostIds.size() >= limit);
            postVOCursorPageResult.setList(byPostIds);
        } else {
            postVOCursorPageResult.setCursor(null);
            postVOCursorPageResult.setHasMore(false);
            postVOCursorPageResult.setList(List.of());
        }
        return postVOCursorPageResult;
    }

    @Override
    public CursorPageResult<PostVO> getPostsByTag(String tagId, String cursor, Integer limit) {
        Long actualTagId = parseTagId(tagId);
        List<Long> postIds;
        try {
            postIds = tagCacheProvider.getPostsTag(actualTagId, cursor, limit);
        } catch (Exception e) {
            log.error("【标签模块】获取标签帖子失败,降级db，tagId={}, cursor={}, limit={}", tagId, cursor, limit, e);
            postIds = postRepositoryImpl.getPostsByTag(actualTagId, cursor, limit);
        }
        List<PostVO> byPostIds = postQueryService.getByPostIds(postIds);
        CursorPageResult<PostVO> postVOCursorPageResult = new CursorPageResult<>();
        if (byPostIds != null && !byPostIds.isEmpty()) {
            postVOCursorPageResult.setCursor(byPostIds.getLast().getId().toString());
            postVOCursorPageResult.setHasMore(byPostIds.size() >= limit);
            postVOCursorPageResult.setList(byPostIds);
        } else {
            postVOCursorPageResult.setCursor(null);
            postVOCursorPageResult.setHasMore(false);
            postVOCursorPageResult.setList(List.of());
        }
        return postVOCursorPageResult;
    }

    private Long parseTagId(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ParameterException("标签标识不能为空");
        }
        try {
            return Long.parseLong(identifier);
        } catch (NumberFormatException e) {
            Tag tag = tagRepository.findByUuid(identifier);
            if (tag == null) {
                tag = tagRepository.findByName(identifier);
            }
            if (tag == null) {
                throw new ParameterException("标签不存在");
            }
            return tag.getId();
        }
    }

    private TagVO convertToVO(Tag tag) {
        return TagVO.builder()
                .id(tag.getUuid())
                .tagName(tag.getTagName())
                .sort(tag.getSort())
                .useCount(tag.getUseCount())
                .status(tag.getStatus())
                .createTime(tag.getCreateTime())
                .build();
    }
}
