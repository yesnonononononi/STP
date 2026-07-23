package com.summit.stp.common.feign;

import com.summit.stp.shared.application.command.PayCommand;
import com.summit.stp.shared.application.vo.PayVO;
import com.summit.stp.shared.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "stp-payment-service", contextId = "payFeignClient")
public interface PayFeignClient {

    @PostMapping("/pay/internal/submit")
    Result<PayVO> pay(@RequestBody PayCommand payCommand);
}
