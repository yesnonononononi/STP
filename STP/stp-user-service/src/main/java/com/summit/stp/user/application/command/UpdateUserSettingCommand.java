package com.summit.stp.user.application.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateUserSettingCommand {
    private final Long userId;
    private final Integer showDelPost;
    private final Integer customizationRecommend;
}
