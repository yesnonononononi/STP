package com.summit.stp.admin.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemActivityVO {
    private Long id;
    private String type;
    private String typeDesc;
    private String module;
    private String title;
    private String content;
    private String targetUrl;
    private LocalDateTime createTime;
}
