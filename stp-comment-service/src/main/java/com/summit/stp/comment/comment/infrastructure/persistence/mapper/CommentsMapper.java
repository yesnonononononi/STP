package com.summit.stp.comment.comment.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.comment.comment.application.vo.CommentVO;
import com.summit.stp.comment.comment.infrastructure.persistence.po.CommentsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommentsMapper extends BaseMapper<CommentsPO> {
    List<CommentVO> queryCommentByPostIdWithCursor(@Param("lastCommentIdCursor") String lastCommentIdCursor, Long postId, Integer limit, @Param("lastHotScoreCursor") Double lastHotScoreCursor,Integer status);

    List<CommentVO> queryReplyByCursor(Long cursor, Integer limit, Long postId, Long rootId, Integer status);

    List<Map<String, Object>> countReplyCountByPostIds(@Param("postIds") List<Long> postIds);

    /**
     * 查询活跃的评论
     * @param lastUpdateTime 上次更新时间
     * @param lastId 上次查询的ID
     * @return 评论列表
     */
    List<CommentsPO> queryActiveComment(@Param("deadLine") LocalDateTime deadLine,@Param("lastUpdateTime") Timestamp lastUpdateTime, @Param("lastId") Long lastId);
}
