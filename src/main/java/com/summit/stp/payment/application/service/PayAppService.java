package com.summit.stp.payment.application.service;

import com.summit.stp.payment.application.command.PayCommand;
import com.summit.stp.payment.application.vo.PayVO;

public interface PayAppService {
    PayVO pay(PayCommand payCommand);
}
