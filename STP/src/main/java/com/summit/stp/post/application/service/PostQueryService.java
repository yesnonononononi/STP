package com.summit.stp.post.application.service;

import com.summit.stp.post.application.vo.PostVO;
import java.util.List;

/**
 * 帖子查询服务（CQRS 读模型服务），解耦领域层仓储，专注于面向展现的分页、连表查询
 */
public interface PostQueryService {
    List<PostVO> getPostPage(String cursor, Boolean self, Long creatorId, Integer status);
}
