package com.summit.stp.member.domain.model;

import lombok.Getter;

@Getter
public enum MemberType {
    REGULAR(1L, 1, "普通会员", "尊享五大特权", 1),
    SUPER(2L, 2, "超级会员", "畅享高级特权", 1);

    private final Long typeId;
    private final Integer priority;
    private final String typeName;
    private final String description;
    private Integer status;

    MemberType(Long typeId, Integer priority, String typeName, String description, Integer status) {
        this.typeId = typeId;
        this.priority = priority;
        this.typeName = typeName;
        this.description = description;
        this.status = status;
    }

    /**
     * 比较会员等级，入参大干当前会员等级
     * 1: 当前会员等级小于待比较会员等级
     * 0: 当前会员等级等于待比较会员等级
     * -1: 当前会员等级大干待比较会员等级
     * @param memberType 待比较的会员等级
     * @return 比较结果
     */
    public int comparePriority(MemberType memberType) {
        if (memberType.getPriority() > this.priority) {
            return 1;
        } else if (memberType.getPriority() < this.priority) {
            return -1;
        }
        return 0;
    }

    /**
     * 根据 ID 获取会员类型
     * @param id 类型 ID
     * @return 会员类型
     */
    public static MemberType getById(Long id) {
        if (id == null) {
            return null;
        }
        for (MemberType type : values()) {
            if (type.typeId.equals(id)) {
                return type;
            }
        }
        return null;
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
