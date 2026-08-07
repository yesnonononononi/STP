package com.summit.stp.toolbox.application.impl;

import com.summit.stp.common.util.DistributedLockUtil;
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
import org.springframework.transaction.support.TransactionTemplate;

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
    private final DistributedLockUtil distributedLockUtil;
    private final TransactionTemplate transactionTemplate;

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
        try {
            List<Integer> dates = getOrBuildCacheDates(userId, month);
            SignInInfoVO signInfo = dailySignInCacheProvider.getSignInfo(userId, dates);
            return calibrateCacheIfNeeded(signInfo, userId, month);
        } catch (Exception e) {
            log.error("【ToolBox-签到】cache获取用户签到信息失败，执行退级查询: userId={}, month={}", userId, month, e);
            return getSignInInfoFallback(userId, month);
        }
    }

    private List<Integer> getOrBuildCacheDates(Long userId, Integer month) {
        List<Integer> dates = dailySignInCacheProvider.getDates(userId, month);
        if (dates.isEmpty()) {
            List<UserSignLog> byUserId = userSignLogRepositoryImpl.findByUserId(userId, month);
            dates = mapDate(byUserId, month);
            dailySignInCacheProvider.cacheBit(dates, userId, month);
        }
        return dates;
    }

    private SignInInfoVO calibrateCacheIfNeeded(SignInInfoVO signInfo, Long userId, Integer month) {
        if (!signInfo.getTodayChecked() && month.equals(LocalDate.now().getMonthValue())) {
            UserSignStats signStat = userSignStatsRepository.findByUserId(userId);
            if (signStat != null && LocalDate.now().equals(signStat.getLastSignDate())) {
                log.warn("【ToolBox】检测到签到缓存不一致，触发缓存自动校准: userId={}", userId);
                List<UserSignLog> byUserId = userSignLogRepositoryImpl.findByUserId(userId, month);
                List<Integer> dates = mapDate(byUserId, month);
                dailySignInCacheProvider.cacheBit(dates, userId, month);
                return dailySignInCacheProvider.getSignInfo(userId, dates);
            }
        }
        return signInfo;
    }

    private SignInInfoVO getSignInInfoFallback(Long userId, Integer month) {
        List<UserSignLog> logList = userSignLogService.getSignLogsByUserId(userId, month);
        UserSignStats signStat = userSignStatsRepository.findByUserId(userId);
        if (signStat == null) {
            signStat = userSignStatsRepository.initSignStat(userId, 0, 0, 0, null);
        }
        int totalDays = signStat.getTotalDays();
        List<Integer> dates = mapDate(logList, month);
        Integer consecutiveDays = signStat.getCurrentContinuousDays();
        int monthCheckedCount = dates.stream().filter(date -> date == 1).toList().size();
        boolean todayChecked = LocalDate.now().equals(signStat.getLastSignDate());
        if (!todayChecked) {
            boolean yesterdayChecked = false;
            LocalDate lastSignDate = signStat.getLastSignDate();
            if (lastSignDate != null && lastSignDate.plusDays(1).equals(LocalDate.now())) {
                yesterdayChecked = true;
            }
            if (!yesterdayChecked) {
                consecutiveDays = 0;
            }
        }
        return SignInInfoVO.builder()
                .checkedDays(totalDays)
                .consecutiveDays(consecutiveDays)
                .todayChecked(todayChecked)
                .monthCheckedCount(monthCheckedCount)
                .checkedDates(dates)
                .build();
    }

    @Override
    public void doSignIn(Long userId) {
        distributedLockUtil.executeWithLock("lock:signin:" + userId, () -> {
            transactionTemplate.execute(status -> {
                try {
                    UserSignStats signStats = userSignStatsRepository.findByUserId(userId);
                    if (signStats == null) {
                        userSignLogService.createSignLog(UserSignLog.builder().signDate(LocalDate.now()).signSource(1).userId(userId).build());
                    } else {
                        checkDuplicateSignIn(signStats);
                        signStats.sign();
                        userSignStatsRepository.save(signStats);
                        userSignLogService.createSignLog(UserSignLog.builder().signDate(LocalDate.now()).signSource(1).userId(userId).continuousDaysSnapshot(signStats.getCurrentContinuousDays()).build());
                    }
                    return true;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    throw new SignInDuplicateException();
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw e;
                }
            });
            try {
                UserSignStats currentStats = userSignStatsRepository.findByUserId(userId);
                dailySignInCacheProvider.doSignIn(userId, currentStats);
            } catch (Exception e) {
                log.error("【ToolBox-签到】更新用户签到缓存失败: userId={}", userId, e);
            }
            log.info("【ToolBox】用户[{}]执行签到打卡成功", userId);
        });
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
