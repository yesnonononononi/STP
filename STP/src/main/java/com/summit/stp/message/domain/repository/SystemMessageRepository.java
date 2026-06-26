package com.summit.stp.message.domain.repository;

import com.summit.stp.message.domain.model.SystemMessage;

import java.util.List;

public interface SystemMessageRepository {
    List<SystemMessage> list(Integer page, Integer pageSize);
}
