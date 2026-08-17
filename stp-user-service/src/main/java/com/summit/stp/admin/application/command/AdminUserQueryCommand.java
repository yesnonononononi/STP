package com.summit.stp.admin.application.command;

import com.summit.stp.admin.api.dto.AdminUserQueryDTO;
import com.summit.stp.common.application.api.dto.RangeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserQueryCommand {
    private Integer page;                        //分页指针
    private Integer size;
    private String keyword;                     // 昵称关键词匹配
    private RangeDTO<Timestamp> createTime;  // 通过注册时间筛选
    private Boolean enabledVIP;                 // 是否启用VIP筛选
    private RangeDTO<Integer> VIPLevel;                   // VIP等级筛选
    private RangeDTO<Number> fans;                // 粉丝数筛选
    private RangeDTO<Number> topic;               // 主题数筛选
    private RangeDTO<Number> follow;             // 关注数筛选
    private RangeDTO<Integer> age;           // 年龄筛选
    private String gender;        // 性别筛选
    private String phone;         // 手机号筛选
    private String ip;          //ip地址文本筛选
    private Integer statusCode;    // 状态码筛选

    public static AdminUserQueryCommand fromDTO(AdminUserQueryDTO dto) {
        if (dto == null) return null;
        return AdminUserQueryCommand.builder()
                .page(dto.getPage())
                .size(dto.getSize())
                .keyword(dto.getKeyword())
                .createTime(dto.getCreateTime())
                .enabledVIP(dto.getEnabledVIP())
                .VIPLevel(dto.getVIPLevel())
                .fans(dto.getFans())
                .topic(dto.getTopic())
                .follow(dto.getFollow())
                .age(dto.getAge())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .ip(dto.getIp())
                .statusCode(dto.getStatusCode())
                .build();
    }
}
