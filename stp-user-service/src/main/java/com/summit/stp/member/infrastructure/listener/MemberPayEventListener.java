package com.summit.stp.member.infrastructure.listener;

import com.summit.stp.common.application.domain.event.OrderPaidEvent;
import com.summit.stp.member.domain.model.Member;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.MemberLevelConfigRepository;
import com.summit.stp.member.domain.repository.MemberRepository;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;


/**
 * 会员支付成功事件
 */
@RequiredArgsConstructor
@Deprecated
public class MemberPayEventListener {

    private final UserMemberRepository userMemberRepository;
    private final MemberRepository<Member> memberRepository;
    private final MemberLevelConfigRepository<MemberLevelConfig> memberLevelConfigRepository;

    @Transactional(rollbackFor = Exception.class)
    public void onEvent(OrderPaidEvent event) {
       /* //查询充值会员套餐信息
        long packageId = event.getPackageId();
        Member member = queryMemberInfo(packageId);
        //查询用户会员状态
        UserMember userMember = userMemberRepository.queryUserMemberByUserIdForUpdate(event.getCreatorId());
        //如果用户没有会员信息则初始化
        if (userMember == null) {
            userMember = initUserMember(event.getCreatorId(), member);
        }
        //查询目标等级
        List<MemberLevelConfig> levelList = memberLevelConfigRepository.findAll();

        //更新用户状态
        userMember.recharge(levelList,member,event.getQuantity());


        //保存用户
        userMemberRepository.save(userMember);*/
    }

    private UserMember initUserMember(Long uid, Member member) {
        return UserMember.builder()
                .userId(uid)
                .totalRecharge(0)
                .level(MemberLevelConfig.builder().level(1L).build())
                .memberType(member.getType())
                .packageTypeId(member.getType().getTypeId())
                .expireTime(new Timestamp(System.currentTimeMillis()))
                .dailyRate(BigDecimal.ZERO)
                .levelUpgradeTime(null)
                .updateTime(null)
                .build();
    }




}
