package com.summit.stp.post.domain.model;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;

import com.summit.stp.shared.exception.ParameterException;

@EqualsAndHashCode
@Getter
@Builder
public class PostImage {

    private final Long id;

    private final Long postId;

    private String imageUrl;

    private Integer width;

    private Integer height;

    private Integer size;

    private Integer sortOrder;

     //"状态：1-正常，2-违规"
    private PostStatus status;

    private final Timestamp createTime;
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateSortOrder(Integer sortOrder) {
        if(sortOrder == null || sortOrder.equals(this.sortOrder))return;
        if (sortOrder < 0) {
            throw new ParameterException("排序序号不能小于0");
        }
        this.sortOrder = sortOrder;
    }

    public void ban() {
        this.status = PostStatus.BLOCKED;
    }


    public void unban() {
        this.status = PostStatus.NORMAL;
    }

    public void updateImage(String imageUrl, @Nullable Integer width, @Nullable Integer height, @Nullable Integer size){
        updateImageUrl(imageUrl);
        check(width, height, size);
        this.width = width == null ? this.width : Math.max(width, 0);
        this.height = height == null ? this.height : Math.max(height, 0);
        this.size = size == null ? this.size : Math.max(size, 0);
    }


    private void check(Integer width, Integer height, Integer size) {
        if (width != null && width <= 0) {
            throw new ParameterException("图片宽度不能小于等于0");
        }
        if (height != null && height <= 0) {
            throw new ParameterException("图片高度不能小于等于0");
        }
        if (size != null && size <= 0) {
            throw new ParameterException("图片大小不能小于等于0");
        }
    }
}
