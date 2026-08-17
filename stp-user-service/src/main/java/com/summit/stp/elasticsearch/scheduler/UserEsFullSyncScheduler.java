package com.summit.stp.elasticsearch.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.elasticsearch.document.UserDocument;
import com.summit.stp.elasticsearch.repo.AdminUserQueryRepository;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.user.infrastructure.persistence.mapper.UserMapper;
import com.summit.stp.user.infrastructure.persistence.po.UserPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEsFullSyncScheduler {

    private static final int BATCH_SIZE = 500;

    private final UserMapper userMapper;
    private final UserMemberRepository userMemberRepository;
    private final AdminUserQueryRepository adminUserQueryRepository;

    /**
     * 每天凌晨 3 点全量同步 DB 到 ES（Keyset Pagination 分批 + 批量写入 saveAll）
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void executeFullSync() {
        log.info("【用户ES全量同步】定时任务开始执行 (每天 03:00)");
        long lastId = 0L;
        int totalProcessed = 0;

        try {
            while (true) {
                // Keyset Pagination: WHERE id > lastId ORDER BY id ASC LIMIT 500
                List<UserPO> userPOList = userMapper.selectList(
                        new LambdaQueryWrapper<UserPO>()
                                .gt(UserPO::getId, lastId)
                                .orderByAsc(UserPO::getId)
                                .last("LIMIT " + BATCH_SIZE)
                );

                if (userPOList == null || userPOList.isEmpty()) {
                    break;
                }

                List<Long> batchUserIds = userPOList.stream().map(UserPO::getId).toList();
                Map<Long, UserMember> memberMap = userMemberRepository.queryUserMemberByUserIds(batchUserIds);

                List<UserDocument> documents = new ArrayList<>();
                for (UserPO po : userPOList) {
                    UserMember userMember = memberMap.get(po.getId());
                    boolean isVip = userMember != null && userMember.isMemberActive();
                    long vipLevel = (userMember != null && userMember.getLevel() != null && userMember.getLevel().getLevel() != null)
                            ? userMember.getLevel().getLevel() : 0L;

                    UserDocument doc = UserDocument.builder()
                            .id(po.getId())
                            .userId(po.getId())
                            .nick(po.getNick() != null ? po.getNick() : "")
                            .phone(po.getPhone() != null ? po.getPhone() : "")
                            .ip(po.getIp() != null ? po.getIp() : "")
                            .createTime(Timestamp.from(po.getCreateTime()))
                            .isVip(isVip)
                            .gender(po.getGender())
                            .age(po.getAge() != null ? po.getAge() : 0)
                            .vipLevel((int) vipLevel)
                            .build();
                    documents.add(doc);
                }

                // 批量写入 saveAll
                adminUserQueryRepository.saveAll(documents);
                totalProcessed += documents.size();
                lastId = userPOList.get(userPOList.size() - 1).getId();

                log.info("【用户ES全量同步】成功刷写批次数据到 ES, 批次大小={}, 当前 lastId={}, 累计处理={}",
                        documents.size(), lastId, totalProcessed);

                if (userPOList.size() < BATCH_SIZE) {
                    break;
                }
            }
            log.info("【用户ES全量同步】定时任务同步完成，共刷写 {} 条记录到 ES", totalProcessed);
        } catch (Exception e) {
            log.error("【用户ES全量同步】定时任务处理过程发生异常, 已处理 totalProcessed={}, 当前 lastId={}", totalProcessed, lastId, e);
        }
    }
}
