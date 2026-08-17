package com.summit.stp.operationlog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Operation {
    private  Long id;
    private Long userId;
    private String username;
    private String operation;
    private OperationType type;
    private Long entityId;
    private Instant createTime;

    public enum OperationType {
        LOGIN, LOGOUT, CREATE, UPDATE, DELETE, OTHER;

        public static OperationType fromString(String type) {
            if (type == null) {
                return OTHER;
            }
            try {
                return OperationType.valueOf(type.toUpperCase());
            } catch (Exception e) {
                return OTHER;
            }
        }
    }
}
