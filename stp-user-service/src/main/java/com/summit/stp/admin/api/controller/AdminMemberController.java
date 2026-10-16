package com.summit.stp.admin.api.controller;

import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;
import com.summit.stp.member.api.dto.LevelConfigSaveRequest;
import com.summit.stp.member.api.dto.LevelConfigUpdateRequest;
import com.summit.stp.member.api.dto.MemberCreateRequest;
import com.summit.stp.member.application.command.MemberCreateCommand;
import com.summit.stp.member.application.service.MemberAppService;
import com.summit.stp.member.application.service.MemberLevelConfigAppService;
import com.summit.stp.member.application.vo.MemberLevelConfigVO;
import com.summit.stp.member.application.vo.UserMemberVO;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.user.api.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@Admin
@Login
@RestController
@RequestMapping("/a/member")
@RequiredArgsConstructor
public class AdminMemberController {
    private final MemberLevelConfigAppService memberLevelConfigAppService;
    private final MemberAppService memberAppService;

    @GetMapping("/level-config/list")
    public Result<PageResult<List<MemberLevelConfigVO>>> queryLevelConfigList(Integer page, Integer pageSize) {
        return Result.success(memberLevelConfigAppService.list(clampPage(page), clampSize(pageSize)));
    }

    @PostMapping("/level-config/save")
    @OperationLog(description = "保存会员等级配置", type = Operation.OperationType.CREATE, entityId = "#request.getLevel()")
    public Result<Void> saveConfig(@RequestBody LevelConfigSaveRequest request) {
        return memberLevelConfigAppService.save(request);
    }

    @PostMapping("/level-config/update")
    @OperationLog(description = "更新会员等级配置", type = Operation.OperationType.UPDATE, entityId = "#request.getId()")
    public Result<Void> updateConfig(@RequestBody LevelConfigUpdateRequest request) {
        return memberLevelConfigAppService.update(request);
    }



    @PostMapping("/level-config/delete/{level}")
    @OperationLog(description = "删除会员等级配置", type = Operation.OperationType.DELETE, entityId = "#level")
    public Result<Void> deleteLevelConfig(@PathVariable Long level) {
        memberLevelConfigAppService.deleteByLevel(level);
        return Result.success();
    }

    @GetMapping("/package/list")
    public Result<List<MemberVO>> queryPackageList() {
        return memberAppService.queryPackageList();
    }

    @PostMapping("/package/save")
    @OperationLog(description = "保存会员套餐", type = Operation.OperationType.CREATE)
    public Result<Void> savePackage(@RequestBody MemberCreateRequest request) {
        MemberCreateCommand command = MemberCreateCommand
                .builder()
                .id(request.getId())
                .name(request.getName())
                .type(MemberType.fromName(request.getType()))
                .price(request.getPrice())
                .duration(request.getDuration())
                .discount(request.getDiscount())
                .description(request.getDescription())
                .dailyRate(request.getDailyRate())
                .priority(request.getPriority())
                .typeId(request.getTypeId())
                .isSuper(request.getIsSuper())
                .stock(request.getStock())
                .status(request.getStatus())
                .build();
        return memberAppService.save(command);
    }



    @PostMapping("/package/delete/{id}")
    @OperationLog(description = "删除会员套餐", type = Operation.OperationType.DELETE, entityId = "#id")
    public Result<Void> deletePackage(@PathVariable Long id) {
        return memberAppService.deleteById(id);
    }

    @GetMapping("/user/list")
    public Result<PageResult<List<UserMemberVO>>> queryUserMemberList( Integer page, Integer pageSize) {
        return Result.success(memberAppService.queryUserMemberList(clampPage(page), clampSize(pageSize)));
    }

    @PostMapping("/user/update-level")
    @OperationLog(description = "更新用户会员等级", type = Operation.OperationType.UPDATE, entityId = "#uid")
    public Result<Void> updateUserLevel(Long uid) {
       return memberAppService.updateUserLevel(uid);
    }

    @PostMapping("/user/extend-expire")
    @OperationLog(description = "延长用户会员有效期", type = Operation.OperationType.UPDATE, entityId = "#uid")
    public Result<Void> extendUserExpire(Long uid, Timestamp expireTime) {
       return memberAppService.extendUserExpire(uid, expireTime);
    }

    private static Integer clampPage(Integer page) {
        return (page == null || page < 1) ? 1 : page;
    }

    private static Integer clampSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }


}
