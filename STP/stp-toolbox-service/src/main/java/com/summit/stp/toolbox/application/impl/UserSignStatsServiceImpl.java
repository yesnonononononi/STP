package com.summit.stp.toolbox.application.impl;

import com.summit.stp.toolbox.api.dto.SignInInfoVO;
import com.summit.stp.toolbox.application.DailySignInCacheProvider;
import com.summit.stp.toolbox.application.UserSignLogService;
import com.summit.stp.toolbox.application.UserSignStatsService;
import com.summit.stp.toolbox.domain.exception.SignInDuplicateException;
import com.summit.stp.toolbox.domain.model.UserSignLog;
import com.summit.stp.toolbox.domain.model.UserSignStats;
import com.summit.stp.toolbox.domain.repository.UserSignStatsRepository;
import com.summit.stp.toolbox.infrastructure.persistence.UserSignLogRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSignStatsServiceImpl implements UserSignStatsService {
    private final UserSignStatsRepository userSignStatsRepository;
    private final UserSignLogService userSignLogService;
    private final DailySignInCacheProvider dailySignInCacheProvider;
    private final UserSignLogRepositoryImpl userSignLogRepositoryImpl;

    @Override
    public UserSignStats getStatsByUserId(Long userId) {
        return userSignStatsRepository.findByUserId(userId);
    }

    @Override
    public void updateStats(UserSignStats stats) {
        userSignStatsRepository.save(stats);
    }

    @Override
    public SignInInfoVO getSignInInfoVO(Long userId, Integer month) {
        validateMonth(month);
        int totalDays, monthCheckedCount;
        boolean todayChecked = false;
        Integer consecutiveDays;
        List<Integer> dates;
        try {
            dates = dailySignInCacheProvider.getDates(userId, month);
            if (dates.isEmpty()) {
                List<UserSignLog> byUserId = userSignLogRepositoryImpl.findByUserId(userId, month);
                dates = mapDate(byUserId, month);
                dailySignInCacheProvider.cacheBit(dates, userId, month);
            }
            return dailySignInCacheProvider.getSignInfo(userId, dates);
        } catch (Exception e) {
            log.error("【获取用户签到信息-fallback】cache获取用户签到信息失败: userId={}, month={}", userId, month, e);
            List<UserSignLog> log = userSignLogService.getSignLogsByUserId(userId, month);
            UserSignStats signStat = userSignStatsRepository.findByUserId(userId);
            if (signStat == null) {
                signStat = userSignStatsRepository.initSignStat(userId, 0, 0, 0);
            }
            totalDays = signStat.getTotalDays();
            dates = mapDate(log, month);
            consecutiveDays = signStat.getCurrentContinuousDays();
            monthCheckedCount = dates.stream().filter(date -> date == 1).toList().size();
            todayChecked = LocalDate.now().equals(signStat.getLastSignDate());
            return SignInInfoVO.builder()
                    .checkedDays(totalDays)
                    .consecutiveDays(consecutiveDays)
                    .todayChecked(todayChecked)
                    .monthCheckedCount(monthCheckedCount)
                    .checkedDates(dates)
                    .build();
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doSignIn(Long userId) {
        try {
            UserSignStats signStats = userSignStatsRepository.findByUserId(userId);
            checkDuplicateSignIn(signStats);
            if (signStats == null) {
                signStats = userSignStatsRepository.initSignStat(userId, 1, 1, 1);
                userSignLogService.createSignLog(UserSignLog.builder().signDate(LocalDate.now()).signSource(1).userId(userId).build());
            }else {
                signStats.sign();
                userSignStatsRepository.save(signStats);
                userSignLogService.createSignLog(UserSignLog.builder().signDate(LocalDate.now()).signSource(1).userId(userId).continuousDaysSnapshot(signStats.getCurrentContinuousDays()).build());
            }
            final UserSignStats finalSignStats = signStats;
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            dailySignInCacheProvider.doSignIn(userId, finalSignStats);
                        } catch (Exception e) {
                            log.error("【用户签到】事务提交后更新用户签到缓存失败: userId={}", userId, e);
                        }
                    }
                });
            } else {
                try {
                    dailySignInCacheProvider.doSignIn(userId, finalSignStats);
                } catch (Exception e) {
                    log.error("【用户签到】更新用户签到缓存失败 跳过更新: userId={}", userId, e);
                }
            }
            log.info("【ToolBox】用户[{}]执行签到打卡成功", userId);
        } catch (DuplicateKeyException e) {
            throw new SignInDuplicateException();
        }
    }




    private List<Integer> mapDate(List<UserSignLog> dates, Integer month) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        LocalDate targetMonthDate = LocalDate.of(year, month, 1);
        int lengthOfMonth = targetMonthDate.lengthOfMonth();

        ArrayList<LocalDate> dayList = new ArrayList<>();
        List<LocalDate> list = dates.stream().sorted((o1, o2) -> o2.getSignDate().compareTo(o1.getSignDate())).map(UserSignLog::getSignDate).toList();
        for (int i = 0; i < lengthOfMonth; i++) {
            dayList.add(LocalDate.of(year, month, i + 1));
        }
        return dayList.stream().map(list::contains).map(i -> i ? 1 : 0).toList();
    }

    /**
     * 校验月份参数是否合法
     */
    private void validateMonth(Integer month) {
        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("查询月份参数不合法");
        }
    }

    /**
     * 校验是否重复打卡
     */
    private void checkDuplicateSignIn(UserSignStats signStats) {
        if (signStats != null && LocalDate.now().equals(signStats.getLastSignDate())) {
            throw new SignInDuplicateException();
        }
    }

}
