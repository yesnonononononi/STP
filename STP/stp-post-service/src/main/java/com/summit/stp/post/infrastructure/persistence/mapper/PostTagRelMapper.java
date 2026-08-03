package com.summit.stp.post.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostTagRelPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostTagRelMapper extends BaseMapper<PostTagRelPO> {
}
