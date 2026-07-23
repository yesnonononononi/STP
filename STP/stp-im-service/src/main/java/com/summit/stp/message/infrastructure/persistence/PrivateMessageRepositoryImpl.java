package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.summit.stp.message.domain.exception.MessageNoExistException;
import com.summit.stp.message.domain.model.PrivateMessage;
import com.summit.stp.message.domain.repository.PrivateMessageRepository;
import com.summit.stp.message.infrastructure.persistence.mapper.PrivateMessageMapper;
import com.summit.stp.message.infrastructure.persistence.po.PrivateMessagePO;
import com.summit.stp.shared.exception.BusinessException;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class PrivateMessageRepositoryImpl implements PrivateMessageRepository {
    private final PrivateMessageMapper privateMessageMapper;

    public PrivateMessagePO toPO(PrivateMessage privateMessage){
        return PrivateMessagePO.builder()
                .id(privateMessage.getId())
                .userId(privateMessage.getUserId())
                .receiverId(privateMessage.getReceiverId())
                .content(privateMessage.getContent())
                .image(privateMessage.getImage())
                .audio(privateMessage.getAudio())
                .video(privateMessage.getVideo())
                .type(privateMessage.getType().getValue())
                .sessionId(privateMessage.getSessionId())
                .status(privateMessage.getStatus().getValue())
                .sendTime(privateMessage.getSendTime())
                .build();
    }
    public PrivateMessage toDomain(PrivateMessagePO privateMessagePO){
        return PrivateMessage.builder()
                .id(privateMessagePO.getId())
                .userId(privateMessagePO.getUserId())
                .receiverId(privateMessagePO.getReceiverId())
                .content(privateMessagePO.getContent())
                .image(privateMessagePO.getImage())
                .audio(privateMessagePO.getAudio())
                .video(privateMessagePO.getVideo())
                .sessionId(privateMessagePO.getSessionId())
                .status(PrivateMessage.Status.fromValue(privateMessagePO.getStatus()))
                .sendTime(privateMessagePO.getSendTime())
                .type(PrivateMessage.Type.fromValue(privateMessagePO.getType()))
                .build();
    }



    @Override
    public PrivateMessage findById(Long messageId) {
        LambdaQueryWrapper<PrivateMessagePO> queryWrapper = new LambdaQueryWrapper<PrivateMessagePO>().eq(PrivateMessagePO::getId, messageId);
        PrivateMessagePO privateMessagePO = privateMessageMapper.selectOne(queryWrapper);
        if(privateMessagePO == null){
            throw new MessageNoExistException();
        }
        return toDomain(privateMessagePO);
    }

    @Override
    public void save(PrivateMessage domain) {
        privateMessageMapper.insertOrUpdate(toPO(domain));
    }

    @Override
    public List<PrivateMessage> findMessagesBySessionId(Long sessionId) {
        LambdaQueryWrapper<PrivateMessagePO> queryWrapper = new LambdaQueryWrapper<>();
         queryWrapper.eq(PrivateMessagePO::getSessionId, sessionId);
         return privateMessageMapper.selectList(queryWrapper).stream().map(this::toDomain).toList();
    }


    public void update(PrivateMessage privateMessage) {
        privateMessageMapper.updateById(toPO(privateMessage));
    }

    public List<PrivateMessage> findHistory(Long sessionId, Long cursorId, Integer limit) {
        LambdaQueryWrapper<PrivateMessagePO>  queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(PrivateMessagePO::getSessionId, sessionId);
        queryWrapper.ne(PrivateMessagePO::getStatus,PrivateMessage.Status.WITHDRAWN.getValue());

        if(cursorId != null){
            queryWrapper.lt(PrivateMessagePO::getId, cursorId);
        }
        queryWrapper.orderByDesc(PrivateMessagePO::getId).last("limit " + limit);

        List<PrivateMessagePO> privateMessagePOS = privateMessageMapper.selectList(queryWrapper);
        return privateMessagePOS.stream().map(this::toDomain).toList();
    }

    public void batchUpdate(List<PrivateMessage> lists) {
        privateMessageMapper.updateById(lists.stream().map(this::toPO).toList());
    }
}
