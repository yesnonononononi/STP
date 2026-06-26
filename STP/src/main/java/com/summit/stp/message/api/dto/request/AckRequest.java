package com.summit.stp.message.api.dto.request;

import lombok.Data;

@Data
public class AckRequest {
    private Long messageId;
    private Long sessionId;
}
