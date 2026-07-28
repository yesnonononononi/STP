package com.summit.stp.user.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@EqualsAndHashCode
@Builder
public class UserSetting {
    private final Long id;
    private final Long userId;
    private Integer showDelPost;
    private Integer customizationRecommend;
    private Timestamp createTime;
    private Timestamp updateTime;

    /**
     * 更新配置行为
     */
    public void update(Integer showDelPost, Integer customizationRecommend) {
        if (showDelPost != null) {
            if (showDelPost != 0 && showDelPost != 1) {
                throw new IllegalArgumentException("无效的显示已删除帖子配置值");
            }
            this.showDelPost = showDelPost;
        }
        if (customizationRecommend != null) {
            if (customizationRecommend != 0 && customizationRecommend != 1) {
                throw new IllegalArgumentException("无效的个性化推荐配置值");
            }
            this.customizationRecommend = customizationRecommend;
        }
    }
}
