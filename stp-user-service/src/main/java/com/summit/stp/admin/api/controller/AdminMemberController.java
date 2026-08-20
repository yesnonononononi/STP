package com.summit.stp.admin.api.controller;

import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
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
        return Result.success(memberLevelConfigAppService.list(page, pageSize));
    }

    @PostMapping("/level-config/save")
    public Result<Void> saveConfig(@RequestBody LevelConfigSaveRequest request) {
        return memberLevelConfigAppService.save(request);
    }

    @PostMapping("/level-config/update")
    public Result<Void> updateConfig(@RequestBody LevelConfigUpdateRequest request) {
        return memberLevelConfigAppService.update(request);
    }



    @PostMapping("/level-config/delete/{level}")
    public Result<Void> deleteLevelConfig(@PathVariable Long level) {
        memberLevelConfigAppService.deleteByLevel(level);
        return Result.success();
    }

    @GetMapping("/package/list")
    public Result<List<MemberVO>> queryPackageList() {
        return memberAppService.queryPackageList();
    }

    @PostMapping("/package/save")
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
    public Result<Void> deletePackage(@PathVariable Long id) {
        return memberAppService.deleteById(id);
    }

    @GetMapping("/user/list")
    public Result<PageResult<List<UserMemberVO>>> queryUserMemberList( Integer page, Integer pageSize) {
        return Result.success(memberAppService.queryUserMemberList(page, pageSize));
    }

    @PostMapping("/user/update-level")
    public Result<Void> updateUserLevel(Long uid) {
       return memberAppService.updateUserLevel(uid);
    }

    @PostMapping("/user/extend-expire")
    public Result<Void> extendUserExpire(Long uid, Timestamp expireTime) {
       return memberAppService.extendUserExpire(uid, expireTime);
    }


}
