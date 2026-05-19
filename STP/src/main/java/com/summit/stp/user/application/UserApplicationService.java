package com.summit.stp.user.application;

import com.summit.stp.user.application.command.UserPutCommand;

public interface UserApplicationService {
    void put(UserPutCommand command);
}
