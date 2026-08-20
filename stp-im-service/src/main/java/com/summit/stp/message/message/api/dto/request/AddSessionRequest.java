package com.summit.stp.message.message.api.dto.request;

import lombok.Data;

@Data
public class AddSessionRequest {
    private Long targetId;
    private String targetNickName;
    private String targetAvatar;
}
