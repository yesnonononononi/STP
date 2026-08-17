package com.summit.stp.admin.application.service.impl;

import com.summit.stp.admin.application.command.AdminPostQueryCommand;
import com.summit.stp.admin.application.service.AdminPostService;
import com.summit.stp.admin.application.vo.AdminPostVO;
import com.summit.stp.common.application.api.result.ESPageVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.elasticsearch.service.PostQuerySupport;
import com.summit.stp.post.domain.exception.NoSuchPostException;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPostServiceImpl implements AdminPostService {


    private final PostQuerySupport postQuerySupport;
    private final PostRepository postRepository;

    @Override
    public Result<PageResult<List<AdminPostVO>>> list(AdminPostQueryCommand command) {
        ESPageVO<Long> res = postQuerySupport.listBy(command);
        List<Long> ids = res.getData();
        List<Post> list = postRepository.findByIds(ids);
        return Result.success(new PageResult<>(res.getPage(), res.getTotal(), list.stream().map(this::toVO).toList()));
    }


    @Override
    public void bypass(Long id) {
        Post post = postRepository.findById(id).orElseThrow(NoSuchPostException::new);
        post.pass();
        postRepository.update(post);
    }

    @Override
    public void bypassNot(Long id, String reason) {
        Post post = postRepository.findById(id).orElseThrow(NoSuchPostException::new);
        post.unpass(reason);
        postRepository.update(post);
    }

    @Override
    public void toggleBan(Long id, boolean attemptBan) {
        Post post = postRepository.findById(id).orElseThrow(NoSuchPostException::new);
        if (attemptBan) {
            post.ban();
        } else {
            post.unban();
        }
        postRepository.update(post);
    }

    private AdminPostVO toVO(Post post) {
        return AdminPostVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .type(post.getType())
                .status(post.getStatus())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .creatorId(post.getCreatorId())
                .content(post.getContent())
                .mediaUrls(post.getMediaUrls())
                .urls(post.getUrls())
                .replyCount(post.getReplyCount())
                .status(post.getStatus())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .creatorId(post.getCreatorId())
                .isTop(post.getIsTop())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .collectCount(post.getCollectCount())
                .hotScore(post.getHotScore())
                .visibleScope(post.getVisibleScope())
                .isTop(post.getIsTop())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .collectCount(post.getCollectCount())
                .hotScore(post.getHotScore())
                .visibleScope(post.getVisibleScope())
                .build();
    }
}
