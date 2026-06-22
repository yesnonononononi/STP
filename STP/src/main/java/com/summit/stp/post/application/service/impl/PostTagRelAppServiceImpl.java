package com.summit.stp.post.application.service.impl;

import com.summit.stp.post.application.service.PostTagRelAppService;
import com.summit.stp.post.application.vo.PostTagRelVO;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.model.PostTag;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostTagRelAppServiceImpl implements PostTagRelAppService {
    private final PostTagRelRepository postTagRelRepository;
    private final PostRepository postRepository;

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
        return relations.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<PostTagRelVO> getRelationsByTagId(Long tagId) {
        List<PostTag> relations = postTagRelRepository.findByTagId(tagId);
        return relations.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private void checkPostActive(Long postId) {
        Post post = postRepository.findById(postId);
        if (post == null || post.getStatus() != PostStatus.NORMAL) {
            throw new BusinessException("帖子已被删除!");
        }
    }

    private PostTagRelVO convertToVO(PostTag domain) {
        if (domain == null) {
            return null;
        }
        return PostTagRelVO.builder()
                .id(domain.getId())
                .postId(domain.getPostId())
                .tagId(domain.getTagId())
                .build();
    }
}
