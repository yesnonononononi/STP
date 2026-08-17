package com.summit.stp.operationlog.domain.service;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.operationlog.api.vo.OperationLogVO;

import java.util.Collection;
import java.util.List;

public interface OperationLogService {

    Result<List<OperationLogVO>> listBy(String type, Long uid, Integer page, Integer size);

    Result<Void> delete(Collection<Long> ids);
}
