package com.summit.stp.admin.api.dto;


import com.summit.stp.admin.application.command.AdminUserQueryCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 管理端用户查询 DTO（api 层）。
 *
 * <p>字段定义已统一收敛到 {@link AdminUserQueryCommand}（application 层），本类仅保留 api 层边界类型，
 * 消除原先 15 字段逐字重复（#31）。Controller 请使用 {@link #toCommand()} 完成 DTO→Command 转换，
 * 避免手写 builder 字段漂移（不使用 MapStruct）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminUserQueryDTO extends AdminUserQueryCommand {

    /**
     * 转换为 Service 层命令对象（可复用转换方法，替代 Controller 内手写 15 字段 builder）。
     */
    public AdminUserQueryCommand toCommand() {
        return AdminUserQueryCommand.builder()
                .page(getPage())
                .size(getSize())
                .keyword(getKeyword())
                .queryWithoutFilter(isQueryWithoutFilter())
                .createTime(getCreateTime())
                .enabledVIP(getEnabledVIP())
                .VIPLevel(getVIPLevel())
                .fans(getFans())
                .topic(getTopic())
                .follow(getFollow())
                .age(getAge())
                .gender(getGender())
                .phone(getPhone())
                .ip(getIp())
                .statusCode(getStatusCode())
                .build();
    }
}
