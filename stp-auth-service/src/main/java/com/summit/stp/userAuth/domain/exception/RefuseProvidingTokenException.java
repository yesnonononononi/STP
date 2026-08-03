package com.summit.stp.userAuth.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

public class RefuseProvidingTokenException extends BusinessException {
    public RefuseProvidingTokenException(String uName,String errMsg) {
        super(String.format("拒绝用户%s登录,原因:%s", uName,errMsg));
    }
}
