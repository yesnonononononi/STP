package com.summit.stp.shared.ThreadContext;

import com.summit.stp.shared.exception.NoFoundUserInfoException;
import com.summit.stp.userAuth.domain.model.UserSession;

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
