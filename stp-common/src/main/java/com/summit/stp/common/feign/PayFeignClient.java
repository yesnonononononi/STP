package com.summit.stp.common.feign;

import com.summit.stp.common.application.service.command.PayCommand;
import com.summit.stp.common.application.api.vo.PayVO;
import com.summit.stp.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "stp-payment-service", contextId = "payFeignClient")
public interface PayFeignClient {

    @PostMapping("/pay/internal/submit")
    Result<PayVO> pay(@RequestBody PayCommand payCommand);
}
