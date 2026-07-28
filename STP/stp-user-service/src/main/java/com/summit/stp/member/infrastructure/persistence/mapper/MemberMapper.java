package com.summit.stp.member.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.member.infrastructure.persistence.po.MemberPackagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会员套餐 Mapper - 属于 user-service 会员域
 */
@Mapper
public interface MemberMapper extends BaseMapper<MemberPackagePO> {

    @Select("SELECT m.* FROM member_package m WHERE m.type_id = #{typeId}")
    List<MemberPackagePO> findMemberByType(@Param("typeId") Long typeId);
}
