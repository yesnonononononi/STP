package com.summit.stp.post.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.infrastructure.persistence.dto.PostTrendStatDTO;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
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


 List<Long> getHotPostsByTag(Long tagId, String cursor, Integer limit);

    List<Long> getPostsByTag(Long tagId, String cursor, Integer limit);

    List<PostTrendStatDTO> selectContentStats(@Param("startTimestamp") Timestamp startTimestamp);

}
