package com.summit.stp.user.domain.exception;

public class UserExistException extends UserCreateException {
    public UserExistException() {
        super("用户已存在");
    }
}
