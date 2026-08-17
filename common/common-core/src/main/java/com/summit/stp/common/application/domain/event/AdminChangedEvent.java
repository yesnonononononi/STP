package com.summit.stp.common.application.domain.event;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class AdminChangedEvent implements Serializable {
    private Long uid;
    private String type;
}
