package com.summit.stp.common.application.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsPostUpdateEvent implements Serializable {

    public enum EventType {
        CREATE, UPDATE, DELETE, STATUS_CHANGE
    }

    private Long postId;
    private EventType eventType;
    private Integer status;
    private String unpassReason;
    private Long timestamp;
}
