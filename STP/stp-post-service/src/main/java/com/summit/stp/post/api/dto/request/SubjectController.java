package com.summit.stp.post.api.dto.request;

import com.summit.stp.post.application.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/subject")
public class SubjectController {
    @GetMapping("/{subjectId}")
    public List<PostVO> list(@PathVariable Long subjectId, @RequestParam String entranceType){
        return null;
    }
}
