package com.summit.stp.post.application.service.impl;

import com.summit.stp.post.application.service.PostTagRelAppService;
import com.summit.stp.post.application.vo.PostTagRelVO;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return postTagRelRepository.findByPostId(postId);
    }

    @Override
    public List<PostTagRelVO> getRelationsByTagId(Long tagId) {
        return postTagRelRepository.findByTagId(tagId);
    }

    private void checkPostActive(Long postId) {
        Post post = postRepository.findById(postId);
        if (post == null || post.getStatus() != PostStatus.NORMAL) {
            throw new BusinessException("帖子已被删除!");
        }
    }
}
