package com.summit.stp.order.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.member.infrastructure.persistence.po.MemberPackagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MemberMapper extends BaseMapper<MemberPackagePO> {

    @Select("SELECT m.*, mt.name as type_name, mt.status as type_status " +
            "FROM member_package m " +
            "INNER JOIN member_type mt ON m.type_id = mt.id " +
            "WHERE mt.id = #{typeId} AND mt.status = #{status}")
    List<MemberPackagePO> findMemberByType(@Param("typeId") Long typeId,
                                           @Param("status") int status);


}
