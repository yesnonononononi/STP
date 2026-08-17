package com.summit.stp.common.application.domain.event;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@ToString
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@EqualsAndHashCode
public class PostChangeEvent implements Serializable {
    public enum EventType {
        CREATE, UPDATE, DELETE
    }
    private EventType eventType;
    private Object data;
    private Long uid;
    private Long postId;
    private List<Long> tagIds;
}




