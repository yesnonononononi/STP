package com.summit.stp.message.api;

import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.vo.MessageVO;
import com.summit.stp.common.application.api.vo.SysMessageVO;
import com.summit.stp.common.infrastructure.websocket.Event;
import com.summit.stp.common.result.Result;
import com.summit.stp.message.api.dto.request.SendMessageRequest;
import com.summit.stp.message.application.command.CreateMessageCommand;
import com.summit.stp.message.application.service.InteractionMessageService;
import com.summit.stp.message.application.service.MessageAppService;
import com.summit.stp.message.application.service.MessageSender;
import com.summit.stp.message.application.vo.InteractionMessageVO;
import com.summit.stp.message.application.vo.MessageListVO;
import com.summit.stp.message.domain.model.EventName;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Login
@RestController
@RequestMapping("/message")
@Slf4j
@RequiredArgsConstructor
public class MessageController {

    private final MessageAppService messageAppService;
    private final MessageSender messageSender;
    private final InteractionMessageService interactionMessageService;

    @GetMapping("/interaction/list")
    @Operation(summary = "获取互动消息列表")
    public Result<List<InteractionMessageVO>> interactionList(
            @RequestParam(required = false) Long lastPublicId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(interactionMessageService.getInteractionList(lastPublicId, limit));
    }

    @DeleteMapping("/interaction/delete/{publicId}")
    @Operation(summary = "删除单个互动消息")
    public Result<Void> deleteInteraction(@PathVariable Long publicId) {
        interactionMessageService.deleteInteractionMessage(publicId);
        return Result.success();
    }

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
        messageAppService.sendMessage(command);

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
                receiverId,
                null
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
        messageSender.send(MessageVO.builder().id(sessionId).receiverId(receiverId).build(),Event.builder().eventName(EventName.READ_MESSAGE.getName()).build(),receiverId,null);
        return Result.success();
    }

    @GetMapping("/read/all")
    @Operation(summary = "标记所有已读")
    public Result<Void> readAll() {
        messageAppService.readAll();
        return Result.success();
    }
}
