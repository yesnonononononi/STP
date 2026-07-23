package com.summit.stp.member.api;

import com.summit.stp.member.application.service.MemberAppService;
import com.summit.stp.shared.application.vo.MemberTypeVO;
import com.summit.stp.shared.application.vo.MemberVO;
import com.summit.stp.shared.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Api(tags = "会员套餐查询")
public class MemberController {
    private final MemberAppService memberAppService;

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID查询套餐详情", notes = "获取指定套餐的名称、价格、优惠等详细数据")
    public Result<MemberVO> queryMemberById(
            @ApiParam(value = "套餐唯一ID", required = true, example = "2001") @PathVariable Long id) {
        return memberAppService.queryMemberById(id);
    }

    @GetMapping("/get/type/{id}")
    @ApiOperation(value = "根据类型查询套餐列表", notes = "获取某会员大类（如超级会员、普通会员）下的所有具体套餐")
    public Result<List<MemberVO>> queryMemberByType(
            @ApiParam(value = "分类ID", required = true, example = "1") @PathVariable Long id) {
        return memberAppService.queryMemberByType(id, 1);
    }

    @GetMapping("/list")
    @ApiOperation(value = "查询全部会员类型与套餐", notes = "层级式获取系统所有的会员类型及下属具体套餐")
    public Result<List<MemberTypeVO>> list() {
        return memberAppService.list();
    }

    @PostMapping("/get/batch")
    @ApiOperation(value = "批量查询会员套餐")
    public Result<Map<Long, MemberVO>> queryMemberByIds(@RequestBody List<Long> ids) {
        return memberAppService.queryMemberByIds(ids);
    }

    @GetMapping("/type/get/{id}")
    @ApiOperation(value = "根据ID查询会员类型")
    public Result<MemberTypeVO> queryMemberTypeById(@PathVariable Long id) {
        return memberAppService.queryMemberTypeById(id);
    }
}
