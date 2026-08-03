package com.summit.stp.member.application.service.impl;

import com.summit.stp.common.result.Result;
import com.summit.stp.member.api.dto.LevelConfigSaveRequest;
import com.summit.stp.member.application.service.MemberLevelConfigAppService;
import com.summit.stp.member.application.vo.MemberLevelConfigVO;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.repository.MemberLevelConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberLevelConfigAppServiceImpl implements MemberLevelConfigAppService {

    private final MemberLevelConfigRepository repository;

    @Override
    @Transactional
    public Result<Void> saveOrUpdate(LevelConfigSaveRequest request) {
        if (request.getLevel() == null || request.getLevel() <= 0) {
            return Result.error("等级数值必须大于0");
        }
        if (request.getLevelName() == null || request.getLevelName().trim().isEmpty()) {
            return Result.error("等级名称不能为空");
        }
        if (request.getMinRecharge() < 0) {
            return Result.error("最小充值金额不能为负数");
        }

        MemberLevelConfig config = repository.findByLevel(request.getLevel());
        if (config != null) {
            config.updateConfig(
                    request.getLevelName(),
                    request.getMinRecharge(),
                    request.getPrivilegesJson(),
                    request.getIconUrl(),
                    request.getSortOrder()
            );
        } else {
            config = MemberLevelConfig.builder()
                    .level(request.getLevel())
                    .levelName(request.getLevelName())
                    .minRecharge(request.getMinRecharge())
                    .privilegesJson(request.getPrivilegesJson())
                    .iconUrl(request.getIconUrl())
                    .sortOrder(request.getSortOrder())
                    .build();
        }
        repository.save(config);
        return Result.success();
    }

    @Override
    public Result<MemberLevelConfigVO> queryByLevel(Long level) {
        if (level == null) {
            return Result.error("等级数值不能为空");
        }
        MemberLevelConfig config = repository.findByLevel(level);
        if (config == null) {
            return Result.error("等级配置不存在");
        }
        return Result.success(convertToVO(config));
    }

    @Override
    public Result<List<MemberLevelConfigVO>> listAll() {
        List<MemberLevelConfig> configs = repository.findAll();
        List<MemberLevelConfigVO> vos = configs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    @Override
    @Transactional
    public Result<Void> deleteByLevel(Long level) {
        if (level == null) {
            return Result.error("等级数值不能为空");
        }
        repository.deleteByLevel(level);
        return Result.success();
    }

    private MemberLevelConfigVO convertToVO(MemberLevelConfig config) {
        if (config == null) {
            return null;
        }
        return MemberLevelConfigVO.builder()
                .level(config.getLevel())
                .levelName(config.getLevelName())
                .minRecharge(config.getMinRecharge())
                .privilegesJson(config.getPrivilegesJson())
                .iconUrl(config.getIconUrl())
                .sortOrder(config.getSortOrder())
                .build();
    }
}
