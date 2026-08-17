package com.summit.stp.admin.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.summit.stp.admin.domain.model.Admin;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
@Data
@Builder
@TableName("admin_user")
public class AdminPO {
    @TableId(type = IdType.AUTO )
    private  Long id;
    private  Long userId;
    private  String username;
    private Integer status;
    @TableField("`order`")
    private Integer order;
    private Instant createTime;
    private Instant updateTime;

    public static Admin toDomain(AdminPO po){
        if (po == null) return null;
        return Admin.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .username(po.getUsername())
                .status(po.getStatus())
                .order(po.getOrder())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    public static AdminPO toPO(Admin admin){
        if (admin == null) return null;
        return AdminPO.builder()
                .id(admin.getId())
                .userId(admin.getUserId())
                .username(admin.getUsername())
                .status(admin.getStatus())
                .order(admin.getOrder())
                .createTime(admin.getCreateTime())
                .updateTime(admin.getUpdateTime())
                .build();
    }
}
