package com.summit.stp.common.application.api.vo;

import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class UserSettingVO implements Serializable {
    private final Long userId;
    private final Integer showDelPost;
    private final Integer customizationRecommend;
}
