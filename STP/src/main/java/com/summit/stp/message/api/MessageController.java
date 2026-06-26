package com.summit.stp.message.api;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.BroadcastAckCallback;
import com.summit.stp.common.infrastructure.websocket.Event;
import com.summit.stp.common.infrastructure.websocket.EventType;
import com.summit.stp.message.api.dto.request.SendMessageRequest;
import com.summit.stp.message.application.command.CreateMessageCommand;
import com.summit.stp.message.application.service.MessageAppService;
import com.summit.stp.message.application.service.MessageSender;
import com.summit.stp.message.application.service.impl.NormalMsgAck;
import com.summit.stp.message.application.vo.MessageListVO;
import com.summit.stp.message.application.vo.MessageVO;
import com.summit.stp.message.application.vo.SendMessageVO;
import com.summit.stp.message.application.vo.SysMessageVO;
import com.summit.stp.message.domain.model.EventName;
import com.summit.stp.message.domain.model.PrivateMessage;
import com.summit.stp.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
@Slf4j
@RequiredArgsConstructor
public class MessageController {

    private final MessageAppService messageAppService;
    private final MessageSender messageSender;
    private final NormalMsgAck normalMsgAck;

    @GetMapping("/sys/list")
    @Operation(summary = "获取系统消息列表")
    public Result<List<SysMessageVO>> sysList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(messageAppService.getSysMessageList(page, pageSize));
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息")
    public Result<Void> send(@RequestBody SendMessageRequest request) {
        CreateMessageCommand command = CreateMessageCommand.builder()
                .msgId(request.getMsgId())
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .image(request.getImage())
                .audio(request.getAudio())
                .video(request.getVideo())
                .sendTime(request.getSendTime())
                .type(request.getType())
                .build();
        MessageVO messageVO = messageAppService.sendMessage(command);
        messageSender.send(messageVO, Event.builder().eventName(EventName.NORMAL_MESSAGE.getName()).build(),messageVO.getReceiverId());
        return Result.success();
    }

    @GetMapping("/del/{messageId}")
    @Operation(summary = "删除消息")
    public Result<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long receiverId) {
        messageAppService.deleteMessage(messageId);
        messageSender.send(
                MessageVO.builder().id(messageId).receiverId(receiverId).build(),
                Event.builder().eventName(EventName.WITHDRAWN_MESSAGE.getName()).build(),
                receiverId
        );
        return Result.success();
    }

    @GetMapping("/history/{friendId}")
    @Operation(summary = "获取消息历史")
    public Result<MessageListVO> messageHistory(
            @PathVariable Long friendId,
            Long cursorId,
            Integer limit
            ) {
        return Result.success(messageAppService.getMessageHistory(friendId, cursorId,limit));
    }

    @GetMapping("/read/{sessionId}")
    @Operation(summary = "标记已读")
    public Result<Void> read(@PathVariable Long sessionId) {
        Long receiverId =messageAppService.readSession(sessionId);
        messageSender.send(MessageVO.builder().id(sessionId).receiverId(receiverId).build(),Event.builder().eventName(EventName.READ_MESSAGE.getName()).build(),receiverId);
        return Result.success();
    }

    @GetMapping("/read/all")
    @Operation(summary = "标记所有已读")
    public Result<Void> readAll() {
        messageAppService.readAll();
        return Result.success();
    }
}
