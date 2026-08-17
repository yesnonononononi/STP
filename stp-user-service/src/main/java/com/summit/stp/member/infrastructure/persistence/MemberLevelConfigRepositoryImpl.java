package com.summit.stp.member.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.repository.MemberLevelConfigRepository;
import com.summit.stp.member.infrastructure.persistence.po.MemberLevelConfigPO;
import com.summit.stp.user.infrastructure.constants.UserConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class MemberLevelConfigRepositoryImpl extends AbstractRepository<MemberLevelConfig, MemberLevelConfigPO> implements MemberLevelConfigRepository<MemberLevelConfig> {
    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper objectMapper;

    @Value("${member.level.cache.ttl-days:1}")
    private long cacheTtlDays;

    public MemberLevelConfigRepositoryImpl(BaseMapper<MemberLevelConfigPO> baseMapper, StringRedisTemplate stringRedisTemplate, JsonMapper objectMapper) {
        super(baseMapper);
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(MemberLevelConfig config) {
        if (config == null || config.getLevel() == null) {
            return;
        }
        super.save(config);
        stringRedisTemplate.delete(UserConstants.Cache.LEVEL_CONFIG);
    }

    @Override
    public Optional<MemberLevelConfig> findByLevel(Long level) {
        return findBy(level, MemberLevelConfigPO::getLevel);
    }

    @Override
    public Optional<MemberLevelConfig> findById(Long id) {
        return super.findById(id);
    }

    @Override
    public List<MemberLevelConfig> findAll() {
        String res = stringRedisTemplate.opsForValue().get(UserConstants.Cache.LEVEL_CONFIG);
        if (res != null) {
            try {
                List<MemberLevelConfigPO> list = objectMapper.readValue(res, new TypeReference<>() {});
                return list.stream()
                        .map(this::toModel)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("【会员等级配置】缓存读取会员等级配置列表失败", e);
            }
        }
        List<MemberLevelConfigPO> pos = getBaseMapper().selectList(null);
        if (pos != null && !pos.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(pos);
                stringRedisTemplate.opsForValue().set(UserConstants.Cache.LEVEL_CONFIG, json, cacheTtlDays, TimeUnit.DAYS);
            } catch (Exception e) {
                log.error("【会员等级配置】缓存写入会员等级配置列表失败", e);
            }
        }
        return pos == null ? List.of() : pos.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByLevel(Long level) {
        if (level != null) {
            delete(level, MemberLevelConfigPO::getLevel);
            stringRedisTemplate.delete(UserConstants.Cache.LEVEL_CONFIG);
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            delete(id, MemberLevelConfigPO::getId);
            stringRedisTemplate.delete(UserConstants.Cache.LEVEL_CONFIG);
        }
    }

    @Override
    public Page<MemberLevelConfig> findByPage(Integer p, Integer size) {
        Page<MemberLevelConfigPO> pageConf = new Page<>(p, size);
        Page<MemberLevelConfigPO> page = getBaseMapper().selectPage(pageConf, new QueryWrapper<>());
        List<MemberLevelConfig> records = page.getRecords().stream().map(this::toModel).toList();
        Page<MemberLevelConfig> res = new Page<>(page.getCurrent(), page.getSize());
        res.setTotal(page.getTotal());
        res.setRecords(records);
        return res;
    }

    @Override
    public void update(MemberLevelConfig config) {
        updateById(config);
        stringRedisTemplate.delete(UserConstants.Cache.LEVEL_CONFIG);
    }

    @Override
    protected MemberLevelConfigPO toPO(MemberLevelConfig domain) {
        if (domain == null) {
            return null;
        }
        return MemberLevelConfigPO.builder()
                .id(domain.getId())
                .level(domain.getLevel())
                .levelName(domain.getLevelName())
                .minRecharge(domain.getMinRecharge())
                .privilegesJson(MemberLevelConfig.Privilege.serialize(domain.getPrivileges()))
                .iconUrl(domain.getIconUrl())
                .sortOrder(domain.getSortOrder())
                .build();
    }

    @Override
    protected MemberLevelConfig toModel(MemberLevelConfigPO po) {
        if (po == null) {
            return null;
        }
        return MemberLevelConfig.builder()
                .id(po.getId())
                .level(po.getLevel())
                .levelName(po.getLevelName())
                .minRecharge(po.getMinRecharge())
                .privileges(MemberLevelConfig.Privilege.deserialize(po.getPrivilegesJson(), false))
                .iconUrl(po.getIconUrl())
                .sortOrder(po.getSortOrder())
                .build();
    }
}

