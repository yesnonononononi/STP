package com.summit.stp.operationlog.api.vo;

import com.summit.stp.operationlog.domain.model.Operation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationLogVO {
    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String operationType;
    private Instant createTime;

    public static OperationLogVO toVO(Operation operation) {
        if (operation == null) {
            return null;
        }
        return OperationLogVO.builder()
                .id(operation.getId())
                .userId(operation.getUserId())
                .username(operation.getUsername())
                .operation(operation.getOperation())
                .operationType(operation.getType() != null ? operation.getType().toString() : null)
                .createTime(operation.getCreateTime())
                .build();
    }
}
