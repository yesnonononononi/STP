package com.summit.stp.post.application.service.impl;

import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.post.application.service.PostTagRelAppService;
import com.summit.stp.post.application.vo.PostTagRelVO;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.model.PostTag;
import com.summit.stp.post.domain.model.Tag;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.post.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostTagRelAppServiceImpl implements PostTagRelAppService {
    private final PostTagRelRepository postTagRelRepository;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    @Override
    public void bindTag(Long postId, List<Long> tags) {
        checkPostActive(postId);
        postTagRelRepository.batchSave(postId, tags);
    }

    @Override
    public void unbindTag(Long id) {
        postTagRelRepository.delete(id);
    }

    @Override
    public void clearPostTags(Long postId) {
        checkPostActive(postId);
        postTagRelRepository.deleteByPostId(postId);
    }

    @Override
    public List<PostTagRelVO> getRelationsByPostId(Long postId) {
        checkPostActive(postId);
        List<PostTag> relations = postTagRelRepository.findByPostId(postId);
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> tagIds = relations.stream().map(PostTag::getTagId).distinct().toList();
        List<Tag> tags = tagRepository.findByIds(tagIds);
        java.util.Map<Long, String> tagIdToUuidMap = tags.stream().collect(Collectors.toMap(Tag::getId, Tag::getUuid));
        return relations.stream().map(r -> this.convertToVO(r, tagIdToUuidMap.get(r.getTagId()))).collect(Collectors.toList());
    }

    @Override
    public List<PostTagRelVO> getRelationsByTagId(String tagUuid) {
        Tag tag = tagRepository.findByUuid(tagUuid);
        if (tag == null) {
            return List.of();
        }
        List<PostTag> relations = postTagRelRepository.findByTagId(tag.getId());
        return relations.stream().map(r -> this.convertToVO(r, tagUuid)).collect(Collectors.toList());
    }

    private void checkPostActive(Long postId) {
        Post post = postRepository.findById(postId);
        if (post == null || post.getStatus() != PostStatus.NORMAL) {
            throw new BusinessException("帖子已被删除!");
        }
    }

    private PostTagRelVO convertToVO(PostTag domain, String tagUuid) {
        if (domain == null) {
            return null;
        }
        return PostTagRelVO.builder()
                .id(domain.getId())
                .postId(domain.getPostId())
                .tagId(tagUuid)
                .build();
    }
}
