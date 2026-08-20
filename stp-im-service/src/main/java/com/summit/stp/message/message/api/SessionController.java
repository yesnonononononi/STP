package com.summit.stp.message.message.api;

import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.message.message.api.dto.request.AddDraftRequest;
import com.summit.stp.message.message.api.dto.request.AddSessionRequest;
import com.summit.stp.message.message.application.command.CreateSessionCommand;
import com.summit.stp.message.message.application.service.SessionService;
import com.summit.stp.message.message.application.vo.SessionVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Login
@RestController
@RequestMapping("/session")
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/list")
    public Result<List<SessionVO>> list(){
        return Result.success(sessionService.list());
    }

    @PostMapping("/draft")
    @Operation(summary = "保存草稿")
    public Result<Void> draft(@RequestBody AddDraftRequest request) {
        sessionService.draft(request.getDraft(),request.getSessionId());
        return Result.success();
    }


    @PostMapping("/draft/del")
    @Operation(summary = "删除草稿")
    public Result<Void> delDraft(@RequestParam  Long sessionId) {
        sessionService.delDraft(sessionId);
        return Result.success();
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody AddSessionRequest addSessionRequest){
        CreateSessionCommand command = CreateSessionCommand.builder()
                .targetNickName(addSessionRequest.getTargetNickName())
                .targetAvatar(addSessionRequest.getTargetAvatar())
                .targetId(addSessionRequest.getTargetId())
                .build();
        return Result.success(sessionService.save(command));
    }
}
