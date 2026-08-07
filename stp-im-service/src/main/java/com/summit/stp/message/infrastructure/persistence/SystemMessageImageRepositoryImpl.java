package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.message.domain.model.SystemMessageImage;
import com.summit.stp.message.domain.repository.SystemMessageImageRepository;
import com.summit.stp.message.infrastructure.persistence.mapper.SystemMessageImageMapper;
import com.summit.stp.message.infrastructure.persistence.po.SystemMessageImagePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SystemMessageImageRepositoryImpl implements SystemMessageImageRepository {
    private final SystemMessageImageMapper systemMessageImageMapper;

    public SystemMessageImagePO toPO(SystemMessageImage systemMessageImage){
        return SystemMessageImagePO.builder()
                .publicId(systemMessageImage.getId())
                .messageId(systemMessageImage.getMessageId())
                .image(systemMessageImage.getImage())
                .status(systemMessageImage.getStatus())
                .createTime(systemMessageImage.getCreateTime())
                .updateTime(systemMessageImage.getUpdateTime())
                .build();
    }
    public SystemMessageImage toDomain(SystemMessageImagePO systemMessageImagePO){
        return SystemMessageImage.builder()
                .id(systemMessageImagePO.getPublicId())
                .messageId(systemMessageImagePO.getMessageId())
                .image(systemMessageImagePO.getImage())
                .status(systemMessageImagePO.getStatus())
                .createTime(systemMessageImagePO.getCreateTime())
                .updateTime(systemMessageImagePO.getUpdateTime())
                .build();
    }
//// SELECT  id,public_id,message_id,image,status,create_time,update_time  FROM system_message_image      WHERE  (id IN ())

    @Override
    public Map<Long, List<SystemMessageImage>> findByIds(List<Long> list) {
        LambdaQueryWrapper<SystemMessageImagePO> queryWrapper = new LambdaQueryWrapper<SystemMessageImagePO>().in(SystemMessageImagePO::getMessageId, list);
        return systemMessageImageMapper.selectList(queryWrapper)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.groupingBy(SystemMessageImage::getMessageId));
    }
}
