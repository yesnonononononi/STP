package com.summit.stp.message.entertainment.api;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.message.entertainment.application.service.EmojiAppService;
import com.summit.stp.message.entertainment.application.vo.EmojiPackageVO;
import com.summit.stp.message.entertainment.application.vo.EmojiVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/entertainment/emoji")
@RequiredArgsConstructor
public class EmojiController {

    private final EmojiAppService emojiAppService;

    @GetMapping("/list/{packageId}")
    public Result<List<EmojiVO>> queryList(@PathVariable Long packageId) {
        return Result.success(emojiAppService.queryList(packageId));
    }

    @GetMapping("/packages")
    public Result<List<EmojiPackageVO>> queryValidPackages() {
        return Result.success(emojiAppService.queryPackages(1));
    }
}
