package com.summit.stp.user.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFansChangeEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId; // 被关注者用户ID
    private Integer fansDelta; // 增量，+1(关注) 或 -1(取关)
}
