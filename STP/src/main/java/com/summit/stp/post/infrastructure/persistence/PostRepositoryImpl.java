package com.summit.stp.post.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.model.PostType;
import com.summit.stp.post.domain.repository.PostImageRepository;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {
    private final PostsMapper postsMapper;
    private final PostImageRepository postImageRepository;

    @Override
    public Post findById(Long id) {
        PostsPO postsPO = postsMapper.selectById(id);
        return postsPO == null ? null : convertToDomain(postsPO);
    }

    @Override
    public Post save(Post post) {
        PostsPO po = convertToPo(post);
        if (post.getId() == null || post.getId() == 0) {
            postsMapper.insert(po);
            return Post.builder()
                    .id(po.getId())
                    .creatorId(post.getCreatorId())
                    .title(post.getTitle())
                    .type(post.getType())
                    .content(post.getContent())
                    .mediaUrls(post.getMediaUrls())
                    .status(post.getStatus())
                    .createTime(po.getCreateTime())
                    .updateTime(po.getUpdateTime())
                    .build();
        } else {
            postsMapper.updateById(po);
            return post;
        }
    }

    @Override
    public void update(Post post) {
      postsMapper.updateById(convertToPo(post));
    }
    public Post convertToDomain(PostsPO po) {
        return Post.builder()
                .id(po.getId())
                .creatorId(po.getCreatorId())
                .title(po.getTitle())
                .type(PostType.fromCode(po.getType()))
                .content(po.getContent())
                .mediaUrls(po.getMediaUrls())
                .status(PostStatus.fromCode(po.getStatus()))
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
    public PostsPO convertToPo(Post post) {
        return PostsPO.builder()
                .id(post.getId())
                .creatorId(post.getCreatorId())
                .title(post.getTitle())
                .type(post.getType().getCode())
                .content(post.getContent())
                .mediaUrls(post.getMediaUrls())
                .status(post.getStatus().getCode())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .build();
    }


}
