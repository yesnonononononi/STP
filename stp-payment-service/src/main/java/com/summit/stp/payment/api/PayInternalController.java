package com.summit.stp.payment.api;

import com.summit.stp.payment.api.dto.PayCommand;
import com.summit.stp.payment.api.vo.PayVO;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.payment.application.service.PayAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/internal")
@RequiredArgsConstructor
public class PayInternalController {

    private final PayAppService payAppService;

    @PostMapping("/submit")
    public Result<PayVO> pay(@RequestBody PayCommand payCommand) {
        return Result.success(payAppService.pay(payCommand));
    }
}

