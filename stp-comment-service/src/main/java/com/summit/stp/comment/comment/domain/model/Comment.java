package com.summit.stp.comment.comment.domain.model;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.*;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.util.ScoreUtil;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class Comment {

    private Long id;

    private Long rootId;

    private Long publisherId;

    private Long parentId;

    private Long postId;

    private Integer postStatus;

    private CommentType type;

    private String content;

    private Timestamp createTime;

    private Long replyCount;

    private Integer isTop;

    private Status status;

    private Item item;

    private Extra extra;

    private String ipLocation;

    private String clientType;

    private Long likeCount;

    private Timestamp updateTime;

    private String reportReason;

    /**
     * 置顶
     *
     * @return true 置顶 false 取消置顶
     */
    public boolean toggleTop() {
        if (this.isTop == 1) {
            this.isTop = 0;
            return false;
        }
        this.isTop = 1;
        this.updateTime = new Timestamp(System.currentTimeMillis());
        return true;
    }

    public void requireLimitTypeRule() {
        if (!StringUtil.isNullOrEmpty(this.extra.getMediaUrl()) && this.extra.mediaType != Extra.MediaType.IMAGE && (this.parentId != null || this.rootId != null)) {
            throw new BusinessException("回复评论无法携带非照片类型媒体数据");
        }
    }

    public String getExtraJsonString() {
        if (this.type == CommentType.IMAGE) {
            return null;
        }
        return this.extra == null ? null : this.extra.toString();
    }

    public static Comment.Extra deserializeExtra(String extraStr) {
        if (extraStr == null || extraStr.isEmpty()) {
            return null;
        }
        try {
            return JSONUtil.toBean(extraStr, Comment.Extra.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void ban() {
        this.status = Status.BAN;
        this.updateTime = Timestamp.from(Instant.now());
    }

    public void unban() {
        this.status = Status.NORMAL;
        this.updateTime = Timestamp.from(Instant.now());
    }

    public void ignoreReport() {
        this.status = Status.NORMAL;
        this.reportReason = "";
        this.updateTime = Timestamp.from(Instant.now());
    }

    public void report(String reason) {
        this.status = Status.REPORT;
        this.reportReason = reason;
        this.updateTime = Timestamp.from(Instant.now());
    }

    public boolean canAccess() {
        return this.status == Status.NORMAL;
    }

    @Getter
    public enum Status {
        NORMAL(1),
        REPORT(2),
        BAN(0);
        private final int code;

        Status(int code) {
            this.code = code;
        }

        public static Status fromCode(int code) {
            for (Status value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Invalid code: " + code);
        }
    }


    /**
     * 媒体信息 <br>
     * { <br>
     * "mediaType":1, <br>
     * "mediaUrl":"", <br>
     * "audioMoment":{ <br>
     * "audioUrl":"", <br>
     * "audioName":"", <br>
     * "duration":0 <br>
     * "typeCode":1 <br>
     * } <br>
     * } <br>
     */
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Extra {
        @ApiModelProperty("媒体类型")
        @JsonProperty("mediaType")
        @JsonAlias({"mediatype", "media_type", "type"})
        private MediaType mediaType;

        @ApiModelProperty("图片信息")
        @JsonProperty("imageMoments")
        @JsonAlias({"imagemoments", "image_moments"})
        private List<CommentImage> imageMoments;

        @ApiModelProperty("媒体URL")
        @JsonProperty("mediaUrl")
        @JsonAlias({"mediaurl", "media_url"})
        private String mediaUrl;

        @ApiModelProperty("音频信息")
        @JsonProperty("audioMoment")
        @JsonAlias({"audiomoment", "audio_moment"})
        private AudioMoment audioMoment;

        @Builder
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class AudioMoment {
            @ApiModelProperty("音频URL")
            @JsonProperty("audioUrl")
            @JsonAlias({"audiourl", "audio_url"})
            private String audioUrl;

            @ApiModelProperty("音频名称")
            @JsonProperty("audioName")
            @JsonAlias({"audioname", "audio_name"})
            private String audioName;

            @ApiModelProperty("音频时长")
            @JsonProperty("duration")
            @JsonAlias({"duration"})
            private Integer duration;

            @ApiModelProperty("音频类型")
            @JsonProperty("typeCode")
            @JsonAlias({"typecode", "type_code"})
            private Integer typeCode;
        }

        public enum MediaType {
            UNKNOWN(0),
            IMAGE(1),
            AUDIO(2),
            VIDEO(3),
            TEXT(4);
            private final int code;

            MediaType(int code) {
                this.code = code;
            }

            @JsonValue
            public int getCode() {
                return code;
            }

            @JsonCreator
            public static MediaType getByCodeOrName(Object val) {
                if (val == null) {
                    return UNKNOWN;
                }
                if (val instanceof Number number) {
                    int c = number.intValue();
                    for (MediaType value : values()) {
                        if (value.code == c) {
                            return value;
                        }
                    }
                } else if (val instanceof String str) {
                    for (MediaType value : values()) {
                        if (value.name().equalsIgnoreCase(str)) {
                            return value;
                        }
                    }
                    try {
                        int c = Integer.parseInt(str);
                        for (MediaType value : values()) {
                            if (value.code == c) {
                                return value;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                return UNKNOWN;
            }

            public static MediaType getByCode(int code) {
                for (MediaType value : values()) {
                    if (value.code == code) {
                        return value;
                    }
                }
                return UNKNOWN;
            }
        }

        public String toString() {
            JSONConfig jsonConfig = JSONConfig.create();
            jsonConfig.setIgnoreCase(true);
            jsonConfig.setIgnoreNullValue(true);
            return JSONUtil.parse(this, jsonConfig).toStringPretty();
        }
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        @ApiModelProperty("点赞数")
        private Long likeCount;
        @ApiModelProperty("当前用户是否点赞")
        private Boolean isLike;
        @ApiModelProperty("作者是否点赞")
        private Boolean authorIsPraised;
        @ApiModelProperty("作者是否回复")
        private Boolean authorIsReplied;
        @ApiModelProperty("回复数")
        private Long replyCount;
        @ApiModelProperty("热度")
        private Double hotScore;
    }




    public void updateScore() {
        this.item.hotScore = ScoreUtil.calculateHotScore(this.item.replyCount, this.item.likeCount, this.createTime);
    }


}
