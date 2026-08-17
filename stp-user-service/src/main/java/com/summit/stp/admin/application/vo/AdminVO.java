package com.summit.stp.admin.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.summit.stp.admin.domain.model.Admin;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
@Builder
@Data
public class AdminVO {
    private  Long id;
    private  Long userId;
    private  String username;
    private Integer status;
    private Integer order;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant updateTime;

    public static AdminVO toVO(Admin admin){
        return AdminVO.builder()
                .id(admin.getId())
                .userId(admin.getUserId())
                .username(admin.getUsername())
                .status(admin.getStatus())
                .order(admin.getOrder())
                .createTime(admin.getCreateTime())
                .updateTime(admin.getUpdateTime())
                .build();
    }
}
