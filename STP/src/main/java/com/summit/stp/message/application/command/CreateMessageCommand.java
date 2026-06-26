package com.summit.stp.message.application.command;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@Setter
@AllArgsConstructor
public class CreateMessageCommand {
    private Long msgId;
    private Long receiverId;
    private String content;
    private String image;
    private String audio;
    private String video;
    private String sendTime;
    private Integer type;
}

