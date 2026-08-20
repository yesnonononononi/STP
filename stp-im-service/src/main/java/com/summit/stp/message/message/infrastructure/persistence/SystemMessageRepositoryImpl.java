package com.summit.stp.message.message.infrastructure.persistence;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.message.admin.application.command.AdminNotificationQueryCommand;
import com.summit.stp.message.message.domain.model.SystemMessage;
import com.summit.stp.message.message.domain.model.SystemMessageImage;
import com.summit.stp.message.message.domain.repository.SystemMessageImageRepository;
import com.summit.stp.message.message.domain.repository.SystemMessageRepository;
import com.summit.stp.message.message.infrastructure.persistence.po.SystemMessagePO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class SystemMessageRepositoryImpl extends AbstractRepository<SystemMessage, SystemMessagePO> implements SystemMessageRepository {

    @Autowired
    private SystemMessageImageRepository systemMessageImageRepository;

    public SystemMessageRepositoryImpl(BaseMapper<SystemMessagePO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public List<SystemMessage> list(Long userId, Integer page, Integer pageSize) {
        Page<SystemMessagePO> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<SystemMessagePO> queryWrapper = new LambdaQueryWrapper<SystemMessagePO>()
                .eq(SystemMessagePO::getStatus, SystemMessage.SystemMessageStatus.PUBLISHED.getCode())
                .and(wrapper -> wrapper.eq(SystemMessagePO::getType, SystemMessage.SystemMessageType.BROADCAST.getCode())
                        .or(w -> w.eq(SystemMessagePO::getAssociateUser, userId)))
                .orderByDesc(SystemMessagePO::getCreateTime);
        Page<SystemMessagePO> pages = getBaseMapper().selectPage(p, queryWrapper);
        List<SystemMessagePO> records = pages.getRecords();
        if (records.isEmpty()) return List.of();

        List<Long> msgIds = records.stream().map(SystemMessagePO::getId).toList();
        Map<Long, List<SystemMessageImage>> imageMap = systemMessageImageRepository.findByIds(msgIds);

        return records.stream().map(po -> toModel(po, imageMap.get(po.getId()))).toList();
    }

    @Override
    public void save(SystemMessage message) {
        if (message == null) return;
        SystemMessagePO po = toPO(message);
        getBaseMapper().insert(po);
        if (message.getImages() != null && !message.getImages().isEmpty()) {
            systemMessageImageRepository.saveImages(po.getId(), message.getImages());
        }
    }

    @Override
    public void updateById(SystemMessage message) {
        if (message == null) return;
        super.updateById(message);
    }

    @Override
    public Page<SystemMessage> findPage(AdminNotificationQueryCommand command) {
        String keyword = command.getKeyword();
        Integer noticeType = command.getNoticeType();
        Page<SystemMessage> res = new Page<>();
        Page<SystemMessagePO> p = new Page<>(Objects.requireNonNullElse(command.getPage(), 1), Objects.requireNonNullElse(command.getSize(), 10));
        LambdaQueryWrapper<SystemMessagePO> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) wrapper.eq(SystemMessagePO::getContent, keyword);
        if (noticeType != null) wrapper.eq(SystemMessagePO::getType, noticeType);
        if (Boolean.TRUE.equals(command.getExcludeDeleted())) {
            wrapper.ne(SystemMessagePO::getStatus, SystemMessage.SystemMessageStatus.DELETED.getCode());
        }
        p = getBaseMapper().selectPage(p, wrapper);
        List<SystemMessagePO> records = p.getRecords();
        if (records.isEmpty()) {
            return res.setCurrent(p.getCurrent()).setTotal(p.getTotal()).setRecords(List.of());
        }

        List<Long> msgIds = records.stream().map(SystemMessagePO::getId).toList();
        Map<Long, List<SystemMessageImage>> imageMap = systemMessageImageRepository.findByIds(msgIds);

        List<SystemMessage> models = records.stream()
                .map(po -> toModel(po, imageMap.get(po.getId())))
                .toList();

        return res.setCurrent(p.getCurrent()).setTotal(p.getTotal()).setRecords(models);
    }

    @Override
    public SystemMessagePO toPO(SystemMessage systemMessage) {
        if (systemMessage == null) return null;
        return SystemMessagePO.builder()
                .id(systemMessage.getId())
                .fromUserId(systemMessage.getFromUserId())
                .content(systemMessage.getContent())
                .status(systemMessage.getStatus().getCode())
                .associateUser(systemMessage.getAssociateUser())
                .type(systemMessage.getType().getCode())
                .publicTime(systemMessage.getPublicTime())
                .createTime(systemMessage.getCreateTime())
                .updateTime(systemMessage.getUpdateTime())
                .build();
    }

    @Override
    public SystemMessage toModel(SystemMessagePO po) {
        return toModel(po, null);
    }

    public SystemMessage toModel(SystemMessagePO po, List<SystemMessageImage> images) {
        if (po == null) return null;
        List<String> imageUrls = images != null ? images.stream().map(SystemMessageImage::getImage).toList() : List.of();
        return SystemMessage.builder()
                .id(po.getId())
                .fromUserId(po.getFromUserId())
                .content(po.getContent())
                .images(imageUrls)
                .associateUser(po.getAssociateUser())
                .type(SystemMessage.SystemMessageType.fromCode(po.getType()))
                .status(SystemMessage.SystemMessageStatus.fromCode(po.getStatus()))
                .publicTime(po.getPublicTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}

