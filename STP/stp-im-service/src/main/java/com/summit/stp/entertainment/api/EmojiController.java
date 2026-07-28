package com.summit.stp.entertainment.api;

import com.summit.stp.entertainment.application.service.EmojiAppService;
import com.summit.stp.entertainment.application.vo.EmojiPackageVO;
import com.summit.stp.entertainment.application.vo.EmojiVO;
import com.summit.stp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
