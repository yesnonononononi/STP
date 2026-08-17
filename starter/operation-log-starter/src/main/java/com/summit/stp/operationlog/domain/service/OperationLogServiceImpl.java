package com.summit.stp.operationlog.domain.service;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.operationlog.api.vo.OperationLogVO;
import com.summit.stp.operationlog.domain.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationLogServiceImpl implements OperationLogService {
    private final OperationLogRepository operationLogRepository;

    @Override
    public Result<List<OperationLogVO>> listBy(String type, Long uid, Integer page, Integer size) {
        return Result.success(operationLogRepository.listBy(type, uid, page, size).stream().map(OperationLogVO::toVO).toList());
    }

    @Override
    public Result<Void> delete(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success();
        }
        operationLogRepository.delete(ids);
        return Result.success();
    }
}
