package com.summit.stp.message.message.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.RepositoryTemplate;
import com.summit.stp.message.admin.application.command.AdminNotificationQueryCommand;
import com.summit.stp.message.message.domain.model.SystemMessage;
import com.summit.stp.message.message.infrastructure.persistence.po.SystemMessagePO;

import java.util.List;

public interface SystemMessageRepository extends RepositoryTemplate<SystemMessage, SystemMessagePO> {
    List<SystemMessage> list(Long userId, Integer page, Integer pageSize);
    void save(SystemMessage message);
    void updateById(SystemMessage message);

    Page<SystemMessage> findPage(AdminNotificationQueryCommand command);
}
