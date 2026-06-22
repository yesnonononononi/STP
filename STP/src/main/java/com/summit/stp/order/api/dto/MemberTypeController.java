package com.summit.stp.order.api.dto;

import com.summit.stp.member.application.service.MemberTypeService;
import com.summit.stp.member.application.vo.MemberTypeVO;
import com.summit.stp.shared.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member/type")
@RequiredArgsConstructor
@Api(tags = "会员类型管理")
public class MemberTypeController {
    private final MemberTypeService memberTypeService;

    @GetMapping
    @ApiOperation(value = "查询会员类型列表", notes = "获取系统内所有定义的会员大类列表")
    public Result<List<MemberTypeVO>> queryMemberType() {
        return memberTypeService.queryMemberTypes();
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改/更新会员类型信息", notes = "更新特定会员类型的名字、描述和启用状态")
    public Result<Void> updateMemberType(
            @ApiParam(value = "更新会员类型请求体", required = true) @RequestBody MemberTypeUpdateRequest request) {
        return memberTypeService.updateMemberType(request);
    }
}
