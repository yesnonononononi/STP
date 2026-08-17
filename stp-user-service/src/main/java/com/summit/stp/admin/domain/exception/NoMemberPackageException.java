package com.summit.stp.admin.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

public class NoMemberPackageException extends BusinessException {
    public NoMemberPackageException() {
        super("没有这样的会员套餐");
    }
}
