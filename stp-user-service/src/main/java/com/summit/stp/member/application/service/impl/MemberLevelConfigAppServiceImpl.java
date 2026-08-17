package com.summit.stp.member.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.member.api.dto.LevelConfigSaveRequest;
import com.summit.stp.member.api.dto.LevelConfigUpdateRequest;
import com.summit.stp.member.application.service.MemberLevelConfigAppService;
import com.summit.stp.member.application.vo.MemberLevelConfigVO;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.repository.MemberLevelConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberLevelConfigAppServiceImpl implements MemberLevelConfigAppService {

    private final MemberLevelConfigRepository<MemberLevelConfig> repository;

    @Override
    @Transactional
    public Result<Void> save(LevelConfigSaveRequest request) {
        if (request.getLevel() == null || request.getLevel() <= 0) {
            return Result.error("等级数值必须大于0");
        }
        if (request.getLevelName() == null || request.getLevelName().trim().isEmpty()) {
            return Result.error("等级名称不能为空");
        }
        if (request.getMinRecharge() < 0) {
            return Result.error("最小充值金额不能为负数");
        }
        if (repository.findByLevel(request.getLevel()).isPresent()) {
            return Result.error("等级数值 " + request.getLevel() + " 已存在");
        }

        MemberLevelConfig config = MemberLevelConfig.builder()
                .level(request.getLevel())
                .levelName(request.getLevelName())
                .minRecharge(request.getMinRecharge())
                .privileges(Objects.requireNonNullElse(MemberLevelConfig.Privilege.deserialize(request.getPrivilegesJson(), true), null))
                .iconUrl(request.getIconUrl())
                .sortOrder(request.getSortOrder())
                .build();
        repository.save(config);

        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> update(LevelConfigUpdateRequest request) {
        if (request.getId() == null) {
            return Result.error("配置ID不能为空");
        }
        if (request.getLevelName() == null || request.getLevelName().trim().isEmpty()) {
            return Result.error("等级名称不能为空");
        }
        if (request.getMinRecharge() < 0) {
            return Result.error("最小充值金额不能为负数");
        }

        MemberLevelConfig config = repository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("等级配置不存在"));

        config.updateConfig(
                request.getLevelName(),
                request.getMinRecharge(),
                request.getPrivilegesJson(),
                request.getIconUrl(),
                request.getSortOrder()
        );
        repository.update(config);

        return Result.success();
    }

    @Override
    public Result<MemberLevelConfigVO> queryByLevel(Long level) {
        if (level == null) {
            return Result.error("等级数值不能为空");
        }
        MemberLevelConfig config = repository.findByLevel(level).orElse(null);
        if (config == null) {
            return Result.error("等级配置不存在");
        }
        return Result.success(convertToVO(config));
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

    @Override
    public PageResult<List<MemberLevelConfigVO>> list(Integer page, Integer pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MemberLevelConfig> p = repository.findByPage(Objects.requireNonNullElse(page, 1), Objects.requireNonNullElse(pageSize, 10));
        List<MemberLevelConfigVO> list = p.getRecords().stream().map(this::convertToVO).toList();
        return new PageResult<>((int) p.getCurrent(), p.getTotal(), list);
    }


    private MemberLevelConfigVO convertToVO(MemberLevelConfig config) {
        if (config == null) {
            return null;
        }
        return MemberLevelConfigVO.builder()
                .id(config.getId())
                .level(config.getLevel())
                .levelName(config.getLevelName())
                .minRecharge(config.getMinRecharge())
                .privilegesJson(config.getPrivileges())
                .iconUrl(config.getIconUrl())
                .sortOrder(config.getSortOrder())
                .build();
    }
}
