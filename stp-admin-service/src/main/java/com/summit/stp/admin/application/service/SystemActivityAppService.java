package com.summit.stp.admin.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.admin.application.command.CreateSystemActivityCommand;
import com.summit.stp.admin.application.command.SystemActivityQueryCommand;
import com.summit.stp.admin.application.vo.SystemActivityVO;
import com.summit.stp.admin.domain.model.SystemActivity;
import com.summit.stp.admin.domain.model.SystemActivityRepository;
import com.summit.stp.admin.domain.model.SystemActivityTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemActivityAppService {

    private final SystemActivityRepository<SystemActivity> systemActivityRepository;

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createActivity(CreateSystemActivityCommand command) {
        log.info("【系统管理】[预警日志] 创建系统活动/预警: {}", command.getTitle());
        SystemActivity activity = SystemActivity.builder()
                .type(SystemActivityTypeEnum.fromCode(command.getType()))
                .module(command.getModule())
                .title(command.getTitle())
                .content(command.getContent())
                .targetUrl(command.getTargetUrl())
                .createTime(LocalDateTime.now())
                .build();
        systemActivityRepository.save(activity);
        return Result.success();
    }

    public Result<List<SystemActivityVO>> getRecentActivities(int limit) {
        List<SystemActivity> activities = systemActivityRepository.findRecentActivities(limit);
        List<SystemActivityVO> voList = activities.stream().map(a -> SystemActivityVO.builder()
                .id(a.getId())
                .type(a.getType() != null ? a.getType().getCode() : "INFO")
                .typeDesc(a.getType() != null ? a.getType().getDescription() : "系统日志")
                .module(a.getModule())
                .title(a.getTitle())
                .content(a.getContent())
                .targetUrl(a.getTargetUrl())
                .createTime(a.getCreateTime())
                .build()
        ).toList();
        return Result.success(voList);
    }

    public Result<PageResult<List<SystemActivityVO>>> queryActivitiesPage(SystemActivityQueryCommand command) {
        Page<SystemActivity> pageData = systemActivityRepository.queryActivitiesPage(
                command.getType(),
                command.getModule(),
                command.getPage(),
                command.getPageSize()
        );

        List<SystemActivityVO> voList = pageData.getRecords().stream().map(a -> SystemActivityVO.builder()
                .id(a.getId())
                .type(a.getType() != null ? a.getType().getCode() : "INFO")
                .typeDesc(a.getType() != null ? a.getType().getDescription() : "系统日志")
                .module(a.getModule())
                .title(a.getTitle())
                .content(a.getContent())
                .targetUrl(a.getTargetUrl())
                .createTime(a.getCreateTime())
                .build()
        ).toList();

        PageResult<List<SystemActivityVO>> pageResult = new PageResult<>(
                pageData.getCurrent(),
                pageData.getTotal(),
                voList
        );
        return Result.success(pageResult);
    }
}
