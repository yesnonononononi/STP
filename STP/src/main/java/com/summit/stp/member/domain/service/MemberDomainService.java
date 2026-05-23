package com.summit.stp.member.domain.service;

import com.summit.stp.member.domain.model.Member;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.model.UserMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class MemberDomainService {




    /**
     * 会员信息不存在的降级
     */
    public void handleMemberNoExist(){}
}
