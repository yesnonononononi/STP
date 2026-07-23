package com.summit.stp.payment.api;

import com.summit.stp.shared.application.command.PayCommand;
import com.summit.stp.payment.application.service.PayAppService;
import com.summit.stp.shared.application.vo.PayVO;
import com.summit.stp.shared.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
