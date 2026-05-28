package com.summit.stp.comment.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.comment.infrastructure.persistence.po.CommentImagePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentImageMapper extends BaseMapper<CommentImagePO> {
}
