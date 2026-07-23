package com.summit.stp.user.application.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserFollowCommand {
    private final Long id;
    private final Integer status;
    private final String source;
}
