package com.summit.stp.message.entertainment.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.message.entertainment.infrastructure.persistence.po.EmojiPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmojiMapper extends BaseMapper<EmojiPO> {
}
