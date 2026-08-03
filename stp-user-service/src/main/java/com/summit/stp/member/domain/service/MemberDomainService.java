package com.summit.stp.member.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDomainService {




    /**
     * 会员信息不存在的降级
     */
    public void handleMemberNoExist(){}
}
