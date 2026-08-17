package com.summit.stp.operationlog.domain.repository;

import com.summit.stp.operationlog.domain.model.Operation;

import java.util.Collection;
import java.util.List;

public interface OperationLogRepository {
    void save(Operation operation);

    List<Operation> listBy(String type, Long uid, Integer page, Integer size);

    void delete(Collection<Long> ids);
}
