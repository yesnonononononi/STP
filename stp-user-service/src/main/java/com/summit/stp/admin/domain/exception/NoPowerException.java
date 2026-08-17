package com.summit.stp.admin.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

public class NoPowerException extends BusinessException {
    public NoPowerException() {
        super("无权限操作");
    }
}
