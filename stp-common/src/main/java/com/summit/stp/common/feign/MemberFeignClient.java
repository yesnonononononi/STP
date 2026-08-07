package com.summit.stp.common.feign;

import com.summit.stp.common.application.api.vo.MemberTypeVO;
import com.summit.stp.common.application.api.vo.MemberVO;
import com.summit.stp.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Member 微服务 Feign 客户端 - 供其他微服务调用会员查询能力
 * 实际路由到 stp-user-service（会员子域归属 user-service）
 */
@FeignClient(name = "stp-user-service", contextId = "memberFeignClient")
public interface MemberFeignClient {

    @GetMapping("/member/get/{id}")
    Result<MemberVO> queryMemberById(@PathVariable("id") Long id);

    @PostMapping("/member/get/batch")
    Result<Map<Long, MemberVO>> queryMemberByIds(@RequestBody List<Long> ids);

    @GetMapping("/member/type/get/{id}")
    Result<MemberTypeVO> queryMemberTypeById(@PathVariable("id") Long id);

    @PostMapping("/member/type/get/batch")
    Result<Map<Long, MemberTypeVO>> queryMemberTypeByIds(@RequestBody List<Long> ids);
}
