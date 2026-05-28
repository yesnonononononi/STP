package com.summit.stp.post.domain.model;

import com.summit.stp.post.application.vo.PostImageVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Builder
@EqualsAndHashCode
@Getter
public class Post {

    private final Long id;

    private final long creatorId;

    private String title;

    private PostType type;

    private String content;

    private String mediaUrls;

    private List<PostImage> urls;

    private Integer replyCount;

    private PostStatus status;

    private final Timestamp createTime;

    private Timestamp updateTime;
    private static final int MAX_IMAGE_NUM = 6;

    public static boolean isLimited(Serializable imageUrlCount) {
        return ((Number) imageUrlCount).intValue() >= MAX_IMAGE_NUM;
    }


    public void updatePost(Post post){
        this.title = post.title == null ? this.title : post.title;
        this.type = post.type == null ? this.type : post.type;
        this.content = post.content == null ? this.content : post.content;
        this.mediaUrls = post.mediaUrls == null ? this.mediaUrls : post.mediaUrls;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }



    public void ban(){
        this.status = PostStatus.BLOCKED;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }

    public void unban(){
        this.status = PostStatus.NORMAL;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }

    public void draft(){
        this.status = PostStatus.DRAFT;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }

    public boolean isImage(){
        return this.type == PostType.IMAGE;
    }

    public void delete() {
        this.status = PostStatus.DELETED;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }

    public void republish() {
        this.status = PostStatus.NORMAL;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }
}
