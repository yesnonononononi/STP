package com.summit.stp.user.application.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSettingVO {
    private final Long userId;
    private final Integer showDelPost;
    private final Integer customizationRecommend;
}
