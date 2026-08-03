package com.summit.stp.relationship.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.vo.UserSimpleVO;
import com.summit.stp.common.result.Result;
import com.summit.stp.relationship.application.vo.FriendVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/relationship")
public class FriendController {
    @Login
    @GetMapping("/list/{uid}")
    @Operation(summary = "获取用户关系列表")
    public Result<Page<FriendVO>> list(@PathVariable Long uid, Integer page, Integer pageSize){
        Page<FriendVO> pageResult = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        
        List<FriendVO> list = new ArrayList<>();
        
        list.add(FriendVO.builder()
                .user(UserSimpleVO.builder()
                        .id(2L)
                        .nick("小王")
                        .avatar("https://api.dicebear.com/7.x/adventurer/svg?seed=Wang")
                        .vipType("年费会员")
                        .build())
                .lastMsg("你好，最近怎么样？")
                .lastTime("2026-06-24 12:00:00")
                .noDisturb(false)
                .build());

        list.add(FriendVO.builder()
                .user(UserSimpleVO.builder()
                        .id(3L)
                        .nick("小李")
                        .avatar("https://api.dicebear.com/7.x/adventurer/svg?seed=Li")
                        .build())
                .lastMsg("[图片]")
                .lastTime("2026-06-24 11:30:00")
                .noDisturb(false)
                .build());

        list.add(FriendVO.builder()
                .user(UserSimpleVO.builder()
                        .id(4L)
                        .nick("系统管理员")
                        .avatar("https://api.dicebear.com/7.x/adventurer/svg?seed=Admin")
                        .vipType("官方")
                        .build())
                .lastMsg("欢迎使用 STP 聊天系统！")
                .lastTime("2026-06-24 09:00:00")
                .noDisturb(true)
                .build());

        pageResult.setRecords(list);
        pageResult.setTotal(list.size());
        
        return Result.success(pageResult);
    }
}
