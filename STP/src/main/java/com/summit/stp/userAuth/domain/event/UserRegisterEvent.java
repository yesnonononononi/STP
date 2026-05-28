package com.summit.stp.userAuth.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterEvent {
    private String phoneNumber;
    private String username;
    private String passwordHash; // 传递加密后的哈希值，确保安全
}
