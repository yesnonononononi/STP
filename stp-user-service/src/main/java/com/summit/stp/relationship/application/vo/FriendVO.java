package com.summit.stp.relationship.application.vo;

import com.summit.stp.user.api.vo.UserSimpleVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendVO {
    private UserSimpleVO user;
    private String lastMsg;
    private String lastTime;
    private Boolean noDisturb;
}

