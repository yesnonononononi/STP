package com.summit.stp.message.message.application.command;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CreateSessionCommand {
    private Long targetId;
    private String targetNickName;
    private String targetAvatar;
}
