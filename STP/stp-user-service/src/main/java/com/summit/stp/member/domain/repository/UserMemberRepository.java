package com.summit.stp.member.domain.repository;

import com.summit.stp.member.domain.model.UserMember;

import java.util.Collection;
import java.util.Map;

/**
 * 用户-会员映射表
 */
public interface UserMemberRepository {
    UserMember queryUserMemberByUserId(long creatorId);

    void save(UserMember userMember);


    UserMember queryUserMemberByUserIdForUpdate(long creatorId);

    Map<Long, UserMember> queryUserMemberByUserIds(Collection<Long> userIds);
}
