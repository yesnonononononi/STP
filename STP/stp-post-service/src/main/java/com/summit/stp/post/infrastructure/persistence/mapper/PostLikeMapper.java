package com.summit.stp.post.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLikePO> {
}
