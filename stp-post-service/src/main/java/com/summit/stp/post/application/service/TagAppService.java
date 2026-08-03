package com.summit.stp.post.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.service.SearchSuggest.SuggestVO;
import com.summit.stp.common.result.CursorPageResult;
import com.summit.stp.common.result.Result;
import com.summit.stp.post.application.command.UpdateTagCommand;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.application.vo.TagVO;

import java.util.List;

public interface TagAppService {
    TagVO getTagById(Long id);
    TagVO getTagByUuid(String uuid);
    TagVO getTagByName(String name);
    void createTag(String content);
    void updateTag(UpdateTagCommand command);
    void deleteTag(Long id);
    void deleteTagByUuid(String uuid);
    Page<TagVO> getTagPage(long page, long pageSize);

    Result<SuggestVO> searchTag(String keyword, Integer limit);

    List<TagVO> getRecentTag(Integer limit);


    CursorPageResult<PostVO> getPostsByHotTag(String tagId, String cursor, Integer limit);

    CursorPageResult<PostVO> getPostsByTag(String tagId, String cursor, Integer limit);
}
