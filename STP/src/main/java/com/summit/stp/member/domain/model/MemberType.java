package com.summit.stp.member.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Builder
public class MemberType {
    private Integer status;
    private final String typeName;
    private final Integer priority;
    private final Long typeId;
    private final String description;

    private MemberType(Integer status, String typeName, Integer priority, Long typeId, String description) {
        this.status = status;
        this.typeName = typeName;
        this.priority = priority;
        this.typeId = typeId;
        this.description = description;
    }

    /**
     * 比较会员等级，入参大于当前会员等级
     * 1: 当前会员等级小于待比较会员等级
     * 0: 当前会员等级等于待比较会员等级
     * -1: 当前会员等级大于待比较会员等级
     * @param memberType 待比较的会员等级
     * @return true/false
     */
    public int  compareTo(MemberType memberType) {
        if (memberType.getPriority() > this.priority) {
            return 1;
        }
        else if (memberType.getPriority() < this.priority){
            return -1;
        }
        return 0;
    }

    /**
     * 禁用
     */
    public void ban() {
        this.status = 0;
    }

    /**
     * 解禁
     */
    public void unban() {
        this.status = 1;
    }
}
