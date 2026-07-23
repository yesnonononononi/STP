package com.summit.stp.post.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLikedChangeEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private Integer likedDelta; // 增量，可为正负值
}
