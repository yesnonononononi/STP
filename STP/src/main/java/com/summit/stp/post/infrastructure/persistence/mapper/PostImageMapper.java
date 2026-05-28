package com.summit.stp.post.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.post.application.vo.PostImageVO;
import com.summit.stp.post.infrastructure.persistence.po.PostImagePO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostImageMapper extends BaseMapper<PostImagePO> {
    List<PostImageVO> findByIds(@Param("list") List<Long> postIds);
}
