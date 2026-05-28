package com.summit.stp.post.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.post.application.command.CreateTagCommand;
import com.summit.stp.post.application.command.UpdateTagCommand;
import com.summit.stp.post.application.vo.TagVO;

public interface TagAppService {
    TagVO getTagById(Long id);
    TagVO getTagByName(String name);
    void createTag(CreateTagCommand command);
    void updateTag(UpdateTagCommand command);
    void deleteTag(Long id);
    Page<TagVO> getTagPage(long page, long pageSize);
}
