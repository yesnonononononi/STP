package com.summit.stp.member.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.member.infrastructure.persistence.po.UserMemberPO;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMemberMapper extends BaseMapper<UserMemberPO> {

    @Select("SELECT * FROM user_member WHERE user_id = #{userId} FOR UPDATE")
    UserMemberPO selectByUserIdForUpdate(@Param("userId") long userId);
}
