package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.message.domain.model.SystemMessage;
import com.summit.stp.message.domain.model.SystemMessageImage;
import com.summit.stp.message.domain.repository.SystemMessageImageRepository;
import com.summit.stp.message.infrastructure.persistence.mapper.SystemMessageImageMapper;
import com.summit.stp.message.infrastructure.persistence.po.SystemMessageImagePO;
import com.summit.stp.message.infrastructure.persistence.po.SystemMessagePO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class SystemMessageImageRepositoryImpl implements SystemMessageImageRepository {
    private final SystemMessageImageMapper systemMessageImageMapper;

    public SystemMessageImagePO toPO(SystemMessageImage systemMessageImage){
        return SystemMessageImagePO.builder()
                .id(systemMessageImage.getId())
                .messageId(systemMessageImage.getMessageId())
                .image(systemMessageImage.getImage())
                .status(systemMessageImage.getStatus())
                .createTime(systemMessageImage.getCreateTime())
                .updateTime(systemMessageImage.getUpdateTime())
                .build();
    }
    public SystemMessageImage toDomain(SystemMessageImagePO systemMessageImagePO){
        return SystemMessageImage.builder()
                .id(systemMessageImagePO.getId())
                .messageId(systemMessageImagePO.getMessageId())
                .image(systemMessageImagePO.getImage())
                .status(systemMessageImagePO.getStatus())
                .createTime(systemMessageImagePO.getCreateTime())
                .updateTime(systemMessageImagePO.getUpdateTime())
                .build();
    }


    @Override
    public Map<Long, List<SystemMessageImage>> findByIds(List<Long> list) {
        LambdaQueryWrapper<SystemMessageImagePO> queryWrapper = new LambdaQueryWrapper<SystemMessageImagePO>().in(SystemMessageImagePO::getId, list);
        return systemMessageImageMapper.selectList(queryWrapper)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toMap(SystemMessageImage::getMessageId, List::of));
    }
}
