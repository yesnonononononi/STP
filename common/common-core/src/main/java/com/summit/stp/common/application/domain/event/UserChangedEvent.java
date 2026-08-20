package com.summit.stp.common.application.domain.event;

import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 统一的用户信息变更事件 - 覆盖 UserDocument 模型与 CRUD 变更类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserChangedEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum EventType {
        CREATE, UPDATE, DELETE
    }
    private EventType eventType;
    private Long userId;
    private String nick;
    private String phone;
    private String ip;
    private Timestamp createTime;
    private Integer gender;
    private Boolean isVip;
    private Integer age;
    private Integer vipLevel;
    private Double scoreDelta; // 热度/榜单积分增量
    private Object data;
}
