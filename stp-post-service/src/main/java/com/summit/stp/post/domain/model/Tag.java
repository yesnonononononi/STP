package com.summit.stp.post.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
@EqualsAndHashCode
@Getter
public class Tag {
    private final Long id;
    private final String uuid;
    private String tagName;
    private Integer sort;
    private Integer useCount;
    private Integer status;
    private final Timestamp createTime;

    /**
     * 更新标签基本信息
     */
    public void updateInfo(String tagName, Integer sort, Integer status) {
        if (tagName != null) {
            this.tagName = tagName;
        }
        if (sort != null) {
            this.sort = sort;
        }
        if (status != null) {
            this.status = status;
        }
    }

    /**
     * 增加使用次数
     */
    public void incrementUseCount() {
        if (this.useCount == null) {
            this.useCount = 0;
        }
        this.useCount++;
    }
}
