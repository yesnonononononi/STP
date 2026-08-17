package com.summit.stp.payment.api.client;

import com.summit.stp.payment.api.dto.PayCommand;
import com.summit.stp.payment.api.vo.PayVO;
import com.summit.stp.common.application.api.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "stp-payment-service", contextId = "payFeignClient")
public interface PayFeignClient {

    @PostMapping("/pay/internal/submit")
    Result<PayVO> pay(@RequestBody PayCommand payCommand);
}



