package com.summit.stp.post.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.post.infrastructure.persistence.po.TagPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TagMapper extends BaseMapper<TagPO> {
    @Update("UPDATE tag SET use_count = use_count + #{delta} WHERE id = #{id}")
    int incrUseCount(@Param("id") Long id, @Param("delta") Integer delta);
}
