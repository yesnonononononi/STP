package com.summit.stp.user.application.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserFollowCommand {
    private final Long followerId;
    private final Long followeeId;
    private final String source;
}
