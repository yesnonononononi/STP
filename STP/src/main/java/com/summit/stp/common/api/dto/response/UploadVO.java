package com.summit.stp.common.api.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UploadVO", description = "文件上传成功返回结果")
public class UploadVO {
    @ApiModelProperty("文件可访问的URL")
    private String url;
    @ApiModelProperty("文件原始名称")
    private String fileName;

}
