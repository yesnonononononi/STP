package com.summit.stp.admin.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SystemActivity {
    private Long id;
    private SystemActivityTypeEnum type;
    private String module;
    private String title;
    private String content;
    private String targetUrl;
    private LocalDateTime createTime;
}
