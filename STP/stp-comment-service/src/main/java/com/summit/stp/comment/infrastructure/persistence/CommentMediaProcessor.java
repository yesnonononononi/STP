package com.summit.stp.comment.infrastructure.persistence;

import cn.hutool.core.util.ObjectUtil;
import com.summit.stp.comment.domain.model.Comment;
import com.summit.stp.comment.domain.model.CommentImage;
import com.summit.stp.comment.infrastructure.persistence.mapper.CommentImageMapper;
import com.summit.stp.comment.infrastructure.persistence.po.CommentImagePO;
import com.summit.stp.comment.infrastructure.persistence.po.CommentsPO;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CommentMediaProcessor {
    private final CommentImageMapper commentImageMapper;
    public void handleExtra(Comment comment, CommentsPO po) {
        Comment.Extra extra = comment.getExtra();
        if(ObjectUtil.isNull(extra))return;
        comment.requireLimitTypeRule();
        Comment.Extra.MediaType mediaType = extra.getMediaType();
        if(Objects.isNull(mediaType)){
            return;
        }
        switch (mediaType){
            case IMAGE -> handleImage(extra,po);
            case AUDIO -> handleAudio(extra);
            case VIDEO -> handleVideo(extra);
            default -> {}
        }
    }

    private void handleVideo(Comment.Extra extra) {
        String mediaUrl = extra.getMediaUrl();
        if(StringUtil.isNullOrEmpty(mediaUrl)){
            return;
        }
    }
    private void handleImage(Comment.Extra extra,CommentsPO po){
        List<CommentImage> imageMoments = extra.getImageMoments();
        if(imageMoments == null)return;
        commentImageMapper.insert(
                imageMoments.stream()
                        .map(imageMoment -> CommentImagePO.builder()
                                .commentId(po.getPublicId())
                                .name(imageMoment.getImageName())
                                .imageUrl(imageMoment.getImageUrl())
                                .width(imageMoment.getWidth())
                                .height(imageMoment.getHeight())
                                .sortOrder(imageMoment.getSortOrder())
                                .size(imageMoment.getSize())
                                .createTime(po.getCreateTime())
                                .status(1)
                                .build()
                        )
                        .toList());
    }
    private void handleAudio(Comment.Extra extra){
        String mediaUrl = extra.getMediaUrl();
        if(StringUtil.isNullOrEmpty(mediaUrl)){
            return;
        }
    }


}
