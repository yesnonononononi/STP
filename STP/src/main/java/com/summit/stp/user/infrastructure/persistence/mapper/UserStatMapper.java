package com.summit.stp.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.user.infrastructure.persistence.po.UserStatPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserStatMapper extends BaseMapper<UserStatPO> {
    @Update("UPDATE user_stat SET liked = liked + #{delta} WHERE user_id = #{userId}")
    int incrLiked(@Param("userId") Long userId, @Param("delta") Integer delta);

    @Update("UPDATE user_stat SET fans = fans + #{delta} WHERE user_id = #{userId}")
    int incrFans(@Param("userId") Long userId, @Param("delta") Integer delta);
}
