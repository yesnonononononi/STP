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

    public int comparePriority(MemberType memberType) {
        if (memberType.getPriority() > this.priority) {
            return 1;
        } else if (memberType.getPriority() < this.priority) {
            return -1;
        }
        return 0;
    }

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
    public static MemberType fromName(String name){
        for (MemberType type : values()) {
            if (type.typeName.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public void ban() {
        this.status = 0;
    }

    public void unban() {
        this.status = 1;
    }
}
