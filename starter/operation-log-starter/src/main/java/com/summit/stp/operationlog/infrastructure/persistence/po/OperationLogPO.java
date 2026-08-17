package com.summit.stp.operationlog.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("operation_log")
public class OperationLogPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private Long entityId;
    private String operation;
    private String type;
    private Instant createTime;

    public static Operation toDomain(OperationLogPO operationPO) {
        if (operationPO == null) {
            return null;
        }
        return Operation.builder()
                .id(operationPO.getId())
                .entityId(operationPO.getEntityId())
                .userId(operationPO.getUserId())
                .username(operationPO.getUsername())
                .operation(operationPO.getOperation())
                .type(Operation.OperationType.fromString(operationPO.getType()))
                .createTime(operationPO.getCreateTime())
                .build();
    }

    public static OperationLogPO toPO(Operation operation) {
        if (operation == null) {
            return null;
        }
        Operation.OperationType t = operation.getType();
        return OperationLogPO.builder()
                .id(operation.getId())
                .userId(operation.getUserId())
                .username(operation.getUsername())
                .entityId(operation.getEntityId())
                .operation(operation.getOperation())
                .type(t != null ? t.toString() : null)
                .createTime(operation.getCreateTime())
                .build();
    }
}
