package com.summit.stp.post.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

@Mapper
public interface PostsMapper extends BaseMapper<PostsPO> {

    List<PostVO> queryByPage(
            @Param("cursorTime") String cursorTime,
            @Param("creatorId") Long creatorId,
            @Param("userId") Long userId,
            @Param("status") Integer status
    );
}
