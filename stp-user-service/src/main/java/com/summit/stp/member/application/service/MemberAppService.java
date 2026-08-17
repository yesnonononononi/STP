package com.summit.stp.member.application.service;

import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.member.application.command.MemberCreateCommand;
import com.summit.stp.member.application.vo.UserMemberVO;
import com.summit.stp.user.api.vo.MemberTypeVO;
import com.summit.stp.user.api.vo.MemberVO;
import com.summit.stp.common.application.api.result.Result;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface MemberAppService {
    Result<MemberVO> queryMemberById(Long id);

    Result<List<MemberVO>> queryMemberByType(Long typeId, int status);


    Result<List<MemberTypeVO>> list();

    Result<MemberTypeVO> queryMemberTypeById(Long id);

    Result<Map<Long, MemberTypeVO>> queryMemberTypeByIds(List<Long> ids);

    Result<Map<Long, MemberVO>> queryMemberByIds(List<Long> ids);

    Result<Void> save(MemberCreateCommand command);

    Result<Void> deleteById(Long id);


    Result<Void> updateUserLevel(Long uid);

    Result<List<MemberVO>> queryPackageList();


    PageResult<List<UserMemberVO>> queryUserMemberList(Integer page, Integer pageSize);

    Result<Void> extendUserExpire(Long uid, Timestamp expireTime);
}

