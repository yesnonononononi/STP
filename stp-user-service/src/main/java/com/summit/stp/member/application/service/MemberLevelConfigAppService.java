package com.summit.stp.member.application.service;

import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.member.api.dto.LevelConfigSaveRequest;
import com.summit.stp.member.api.dto.LevelConfigUpdateRequest;
import com.summit.stp.member.application.vo.MemberLevelConfigVO;

import java.util.List;

public interface MemberLevelConfigAppService {
    Result<Void> save(LevelConfigSaveRequest request);

    Result<Void> update(LevelConfigUpdateRequest request);

    Result<MemberLevelConfigVO> queryByLevel(Long level);


    Result<Void> deleteByLevel(Long level);

    PageResult<List<MemberLevelConfigVO>> list(Integer page,Integer pageSize);


}
