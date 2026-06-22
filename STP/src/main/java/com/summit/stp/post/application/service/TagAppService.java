package com.summit.stp.post.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.post.application.command.UpdateTagCommand;
import com.summit.stp.post.application.vo.TagVO;
import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.service.SearchSuggest.SuggestVO;

import java.util.List;

public interface TagAppService {
    TagVO getTagById(Long id);
    TagVO getTagByName(String name);
    void createTag(String content);
    void updateTag(UpdateTagCommand command);
    void deleteTag(Long id);
    Page<TagVO> getTagPage(long page, long pageSize);

    Result<SuggestVO> searchTag(String keyword, Integer limit);

    List<TagVO> getRecentTag(Integer limit);
}
