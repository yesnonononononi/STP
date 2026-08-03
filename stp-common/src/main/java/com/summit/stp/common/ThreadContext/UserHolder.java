package com.summit.stp.common.ThreadContext;

import com.summit.stp.common.application.domain.exception.NoFoundUserInfoException;
import com.summit.stp.common.application.domain.model.UserSession;

public class UserHolder {
    private  static final ThreadLocal<UserSession> threadLocal = new ThreadLocal<>();
    public static UserSession getUser(){
        UserSession user = threadLocal.get();
        if(user == null){
            throw new NoFoundUserInfoException();
        }
        return user;
    }

    public static void setUser(UserSession user){
        threadLocal.set(user);
    }

    public static void clear(){
        threadLocal.remove();
    }
}
