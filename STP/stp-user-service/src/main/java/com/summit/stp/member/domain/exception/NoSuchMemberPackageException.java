package com.summit.stp.member.domain.exception;

import com.summit.stp.shared.exception.BusinessException;

public class NoSuchMemberPackageException extends BusinessException {
    public NoSuchMemberPackageException(String message) {
        super(message);
    }
}
