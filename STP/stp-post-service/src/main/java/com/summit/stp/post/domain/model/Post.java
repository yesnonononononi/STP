package com.summit.stp.post.domain.model;

import com.summit.stp.common.application.domain.exception.BusinessException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import com.summit.stp.post.infrastructure.constants.PostConstants;

@Builder
@EqualsAndHashCode
@Getter
public class Post {

    private final Long id;

    private final Long creatorId;

    private String title;

    private PostType type;

    private String content;

    private String mediaUrls;

    private List<PostImage> urls;

    private Long replyCount;

    private PostStatus status;

    private final Timestamp createTime;

    private Timestamp updateTime;

    private Integer isTop;

    private Long viewCount;

    private Long likeCount;

    private Long collectCount;

    private Double hotScore;

    private VisibleScope visibleScope;

    public void updateScope(Integer visible) {
        this.visibleScope = VisibleScope.fromCode(visible);
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }

    @Getter
    public enum VisibleScope{
        PUBLIC(1),
        PRIVATE(2),
        PROTECTED(3);
        private final int code;
        VisibleScope(int code) {
            this.code = code;
        }
        public static VisibleScope fromCode(int code) {
            for (VisibleScope scope : values()) {
                if (scope.code == code) {
                    return scope;
                }
            }
           throw new BusinessException("无效的可见范围");
        }
    }

    public static boolean isLimited(Serializable imageUrlCount) {
        return ((Number) imageUrlCount).intValue() > PostConstants.Business.MAX_IMAGE_NUM;
    }


    public void updatePost(Post post){
        if (post.title != null && post.title.length() > PostConstants.Business.MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("帖子标题长度不能超过" + PostConstants.Business.MAX_TITLE_LENGTH + "字");
        }
        if (post.content != null && post.content.length() > PostConstants.Business.MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("帖子内容长度不能超过" + PostConstants.Business.MAX_CONTENT_LENGTH + "字");
        }
        if (post.urls != null && isLimited(post.urls.size())) {
            throw new IllegalArgumentException("图片数量超过上限!");
        }
        this.title = post.title == null ? this.title : post.title;
        this.type = post.type == null ? this.type : post.type;
        this.content = post.content == null ? this.content : post.content;
        this.mediaUrls = post.mediaUrls == null ? this.mediaUrls : post.mediaUrls;
        this.isTop = post.isTop == null ? this.isTop : post.isTop;
        this.viewCount = post.viewCount == null ? this.viewCount : post.viewCount;
        this.urls = post.urls == null ? this.urls : post.urls;
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

    public void top(Integer isTop) {
        this.isTop = isTop;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }

    public boolean isActive(){
        return this.status == PostStatus.NORMAL;
    }

    public void changeSate(Long replyCount, Long likeCount, Long viewCount) {
        this.replyCount += replyCount == null ? 0: replyCount;
        this.likeCount += likeCount == null ? 0: likeCount;
        this.viewCount += viewCount == null ? 0 : viewCount;
    }

    public double calculateHotScore() {
        if (this.createTime == null) {
            return 0.0;
        }
        long diffMillis = System.currentTimeMillis() - this.createTime.getTime();
        double hours = diffMillis / (1000.0 * 3600);  // 转换为小时
        long like = this.likeCount != null ? this.likeCount : 0L;
        long reply = this.replyCount != null ? this.replyCount : 0L;
        double score = (like + reply * 3.0) / Math.pow(hours + 2, 1.5);
        this.hotScore = Math.ceil(score);
        return this.hotScore;
    }
}
