package com.summit.stp.post.infrastructure.persistence.mapper;

import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostsMapper extends BaseMapper<PostsPO> {

    List<PostVO> queryByPage(
            @Param("cursorId") Long cursorId,
            @Param("creatorId") Long creatorId,
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("limit") Integer limit

    );

    List<PostVO> queryByPostIds(@Param("posts")List<Long> posts,@Param("status") Integer status,@Param("userId") Long userId);


}
