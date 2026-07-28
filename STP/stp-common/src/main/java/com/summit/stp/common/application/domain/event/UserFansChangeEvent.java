package com.summit.stp.common.application.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户粉丝数变更事件 - 跨服务共享的 MQ 事件契约
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFansChangeEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private Integer fansDelta;
}
