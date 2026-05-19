package com.summit.stp.payment.api;

import com.summit.stp.payment.api.dto.PayRequest;
import com.summit.stp.payment.application.command.PayCommand;
import com.summit.stp.payment.application.vo.PayVO;
import com.summit.stp.payment.application.service.PayAppService;
import com.summit.stp.payment.infrastructure.Enum.PayType;
import com.summit.stp.shared.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {
    private final PayAppService payAppService;

    @PostMapping("/pay")
    public Result<PayVO> pay(@RequestBody PayRequest payRequest){
        PayCommand payCommand = PayCommand
                .builder()
                .payType(PayType.valueOf(payRequest.getPayType()))
                .username(payRequest.getUname())
                .build();

        PayVO result = payAppService.pay(payCommand);
        return Result.success(result);
    }
}
