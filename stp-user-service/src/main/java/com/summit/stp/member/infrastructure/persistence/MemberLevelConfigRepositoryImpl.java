package com.summit.stp.member.infrastructure.persistence;

import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.repository.MemberLevelConfigRepository;
import com.summit.stp.member.infrastructure.persistence.mapper.MemberLevelConfigMapper;
import com.summit.stp.member.infrastructure.persistence.po.MemberLevelConfigPO;
import com.summit.stp.user.infrastructure.constants.UserConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberLevelConfigRepositoryImpl implements MemberLevelConfigRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private final MemberLevelConfigMapper mapper;
    private final JsonMapper objectMapper;

    @Value("${member.level.cache.ttl-days:1}")
    private long cacheTtlDays;

    @Override
    public void save(MemberLevelConfig config) {
        if (config == null || config.getLevel() == null) {
            return;
        }
        MemberLevelConfigPO po = convertToPO(config);
        MemberLevelConfigPO existing = mapper.selectById(config.getLevel());
        if (existing != null) {
            mapper.updateById(po);
        } else {
            mapper.insert(po);
        }
        // 数据变更时清除缓存，保证一致性
        stringRedisTemplate.delete(UserConstants.Cache.LEVEL_CONFIG);
    }

    @Override
    public MemberLevelConfig findByLevel(Long level) {
        if (level == null) {
            return null;
        }
        MemberLevelConfigPO po = mapper.selectById(level);
        return convertToDomain(po);
    }

    @Override
    public List<MemberLevelConfig> findAll() {
        String res = stringRedisTemplate.opsForValue().get(UserConstants.Cache.LEVEL_CONFIG);
        if (res != null) {
            try {
                List<MemberLevelConfigPO> list = objectMapper.readValue(res, new TypeReference<>() {
                });
                return list.stream()
                        .map(this::convertToDomain)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("【会员等级配置】缓存读取会员等级配置列表失败", e);
            }
        }
        List<MemberLevelConfigPO> pos = mapper.selectList(null);
        // 查询后回写缓存，设置过期时间为 1 天
        if (pos != null && !pos.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(pos);
                stringRedisTemplate.opsForValue().set(UserConstants.Cache.LEVEL_CONFIG, json, cacheTtlDays, TimeUnit.DAYS);
            } catch (Exception e) {
                log.error("【会员等级配置】缓存写入会员等级配置列表失败", e);
            }
        }
        return pos == null ? List.of() :pos.stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByLevel(Long level) {
        if (level != null) {
            mapper.deleteById(level);
            // 删除时同步清空缓存
            stringRedisTemplate.delete(UserConstants.Cache.LEVEL_CONFIG);
        }
    }

    private MemberLevelConfigPO convertToPO(MemberLevelConfig domain) {
        if (domain == null) {
            return null;
        }
        return MemberLevelConfigPO.builder()
                .level(domain.getLevel())
                .levelName(domain.getLevelName())
                .minRecharge(domain.getMinRecharge())
                .privilegesJson(domain.getPrivilegesJson())
                .iconUrl(domain.getIconUrl())
                .sortOrder(domain.getSortOrder())
                .build();
    }

    private MemberLevelConfig convertToDomain(MemberLevelConfigPO po) {
        if (po == null) {
            return null;
        }
        return MemberLevelConfig.builder()
                .level(po.getLevel())
                .levelName(po.getLevelName())
                .minRecharge(po.getMinRecharge())
                .privilegesJson(po.getPrivilegesJson())
                .iconUrl(po.getIconUrl())
                .sortOrder(po.getSortOrder())
                .build();
    }
}
