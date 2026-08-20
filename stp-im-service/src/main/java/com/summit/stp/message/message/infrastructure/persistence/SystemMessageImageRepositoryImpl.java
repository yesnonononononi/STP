package com.summit.stp.message.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.message.message.domain.model.SystemMessageImage;
import com.summit.stp.message.message.domain.repository.SystemMessageImageRepository;
import com.summit.stp.message.message.infrastructure.persistence.po.SystemMessageImagePO;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class SystemMessageImageRepositoryImpl extends AbstractRepository<SystemMessageImage, SystemMessageImagePO> implements SystemMessageImageRepository {

    public SystemMessageImageRepositoryImpl(BaseMapper<SystemMessageImagePO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public Map<Long, List<SystemMessageImage>> findByIds(List<Long> list) {
        if (list == null || list.isEmpty()) return Collections.emptyMap();
        LambdaQueryWrapper<SystemMessageImagePO> queryWrapper = new LambdaQueryWrapper<SystemMessageImagePO>().in(SystemMessageImagePO::getMessageId, list);
        return getBaseMapper().selectList(queryWrapper)
                .stream()
                .map(this::toModel)
                .collect(Collectors.groupingBy(SystemMessageImage::getMessageId));
    }

    @Override
    public void saveImages(Long messageId, List<String> imageUrls) {
        if (messageId == null || imageUrls == null || imageUrls.isEmpty()) return;
        String nowStr = String.valueOf(System.currentTimeMillis());
        for (String url : imageUrls) {
            if (cn.hutool.core.util.StrUtil.isNotBlank(url)) {
                SystemMessageImagePO po = SystemMessageImagePO.builder()
                        .messageId(messageId)
                        .image(url)
                        .status("1")
                        .createTime(nowStr)
                        .updateTime(nowStr)
                        .build();
                getBaseMapper().insert(po);
            }
        }
    }

    @Override
    public SystemMessageImagePO toPO(SystemMessageImage systemMessageImage) {
        if (systemMessageImage == null) return null;
        return SystemMessageImagePO.builder()
                .id(systemMessageImage.getId())
                .messageId(systemMessageImage.getMessageId())
                .image(systemMessageImage.getImage())
                .status(systemMessageImage.getStatus())
                .createTime(systemMessageImage.getCreateTime())
                .updateTime(systemMessageImage.getUpdateTime())
                .build();
    }

    @Override
    public SystemMessageImage toModel(SystemMessageImagePO systemMessageImagePO) {
        if (systemMessageImagePO == null) return null;
        return SystemMessageImage.builder()
                .id(systemMessageImagePO.getId())
                .messageId(systemMessageImagePO.getMessageId())
                .image(systemMessageImagePO.getImage())
                .status(systemMessageImagePO.getStatus())
                .createTime(systemMessageImagePO.getCreateTime())
                .updateTime(systemMessageImagePO.getUpdateTime())
                .build();
    }
}

