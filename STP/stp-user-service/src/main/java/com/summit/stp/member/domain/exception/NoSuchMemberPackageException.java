package com.summit.stp.member.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

public class NoSuchMemberPackageException extends BusinessException {
    public NoSuchMemberPackageException(String message) {
        super(message);
    }
}
