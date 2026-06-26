package com.summit.stp.message.api;

import com.summit.stp.message.api.dto.request.AddSessionRequest;
import com.summit.stp.message.application.command.CreateSessionCommand;
import com.summit.stp.message.application.service.SessionService;
import com.summit.stp.message.application.vo.SessionVO;
import com.summit.stp.shared.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
