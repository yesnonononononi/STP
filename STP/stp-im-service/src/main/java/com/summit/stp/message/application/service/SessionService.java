package com.summit.stp.message.application.service;

import com.summit.stp.message.application.command.CreateSessionCommand;
import com.summit.stp.message.application.vo.SessionVO;

import java.util.List;

public interface SessionService {
    List<SessionVO> list();

    Void save(CreateSessionCommand command);

    void draft(String draft, Long sessionId);

    void delDraft(Long sessionId);

}
