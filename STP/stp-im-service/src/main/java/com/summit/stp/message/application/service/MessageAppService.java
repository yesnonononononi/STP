package com.summit.stp.message.application.service;

import com.summit.stp.message.application.command.CreateMessageCommand;
import com.summit.stp.message.application.vo.MessageListVO;
import com.summit.stp.common.application.vo.MessageVO;
import com.summit.stp.common.application.vo.SysMessageVO;

import java.util.List;

public interface MessageAppService {
    List<SysMessageVO> getSysMessageList(Integer page,Integer pageSize);

    MessageVO sendMessage(CreateMessageCommand command);

    void deleteMessage(Long messageId);

    MessageListVO getMessageHistory(Long friendId, Long cursorId, Integer limit);

    Long readSession(Long sessionId);

    void readAll();


}
