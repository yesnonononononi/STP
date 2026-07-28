package com.summit.stp.userAuth.domain.service;

import com.summit.stp.userAuth.domain.model.ResetType;

public interface ResetPasswordStrategy {
    public ResetType getStrategy();
    public void verify(String number,String verifyCode);
}
