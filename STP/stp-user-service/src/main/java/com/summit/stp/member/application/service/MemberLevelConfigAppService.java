package com.summit.stp.member.application.service;

import com.summit.stp.member.api.dto.LevelConfigSaveRequest;
import com.summit.stp.member.application.vo.MemberLevelConfigVO;
import com.summit.stp.common.result.Result;

import java.util.List;

public interface MemberLevelConfigAppService {
    Result<Void> saveOrUpdate(LevelConfigSaveRequest request);

    Result<MemberLevelConfigVO> queryByLevel(Long level);

    Result<List<MemberLevelConfigVO>> listAll();

    Result<Void> deleteByLevel(Long level);
}
