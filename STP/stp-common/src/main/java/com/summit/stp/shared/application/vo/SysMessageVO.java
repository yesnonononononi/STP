package com.summit.stp.shared.application.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 系统消息展示信息 - 跨服务共享的 VO 契约
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "SysMessageVO", description = "系统消息展示信息")
public class SysMessageVO {
    @ApiModelProperty("消息ID")
    private Long id;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("发布时间")
    private String publicTime;

    @ApiModelProperty("图片列表")
    private List<String> images;
}
