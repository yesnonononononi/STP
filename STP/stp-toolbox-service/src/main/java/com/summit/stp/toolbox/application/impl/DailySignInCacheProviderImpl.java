package com.summit.stp.toolbox.application.impl;

import com.summit.stp.toolbox.infrastructure.constants.ToolboxConstants;
import com.summit.stp.toolbox.api.dto.SignInInfoVO;
import com.summit.stp.toolbox.application.DailySignInCacheProvider;
import com.summit.stp.toolbox.domain.model.UserSignStats;
import com.summit.stp.toolbox.infrastructure.persistence.UserSignStatsRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailySignInCacheProviderImpl implements DailySignInCacheProvider {

    private final RedisTemplate redisTemplate;
    private final UserSignStatsRepositoryImpl userSignStatsRepositoryImpl;


    private String buildKey(int year, int month, Long userId) {
        return String.format(ToolboxConstants.Cache.USER + "%s:year:%s:month:%s", userId, year, month);
    }

    @Override
    public SignInInfoVO getSignInfo(Long userId, List<Integer> dates) {
        LocalDate now = LocalDate.now();
        int[] extraDayInfo = getExtraDayInfo(userId);

        Integer consecutiveDays = extraDayInfo[0];
        Integer totalDays = extraDayInfo[1];

        String currentMonthKey = buildKey(now.getYear(), now.getMonthValue(), userId);
        Boolean bit = redisTemplate.opsForValue().getBit(currentMonthKey, now.getDayOfMonth() - 1);
        boolean todayChecked = bit != null && bit;

        Integer monthCheckedCount = dates.stream().filter(i -> i == 1).toList().size();

        return SignInInfoVO.builder()
                .monthCheckedCount(monthCheckedCount)
                .checkedDates(dates)
                .todayChecked(todayChecked)
                .consecutiveDays(consecutiveDays)
                .checkedDays(totalDays)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doSignIn(Long userId, UserSignStats userSignStats) {

        LocalDate now = LocalDate.now();
        List<Object> list = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                String key = buildKey(now.getYear(), now.getMonthValue(), userId);
                operations.opsForValue().setBit(key, now.getDayOfMonth() - 1, true);
                operations.opsForValue().getBit(key, now.getDayOfMonth() - 2);
                operations.expire(key, ToolboxConstants.Business.TTL, TimeUnit.DAYS);
                return null;
            }
        });
        cacheHash(userId, list.get(1).toString().equals("true"), userSignStats);
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<Integer> getDates(Long userId, Integer month) {
        LocalDate now = LocalDate.now();
        LocalDate localDate = LocalDate.of(now.getYear(), month, 1);
        String key = buildKey(now.getYear(), month, userId);
        byte[] bytes = (byte[]) redisTemplate.execute((RedisCallback<byte[]>) connection ->
                connection.stringCommands().get(redisTemplate.getKeySerializer().serialize(key))
        );
        if (bytes == null || bytes.length == 0) {
            return List.of();
        }
        return parseByteArray(bytes, localDate.lengthOfMonth());
    }

    /**
     * 将 Redis Bitmap 的原始字节解析为 0/1 列表
     *
     * @param bytes       Redis 返回的字节数组（可能被截断，尾部全0的字节不会存储）
     * @param daysInMonth 当月实际天数（28/29/30/31）
     * @return 长度为 daysInMonth 的列表，索引0代表1号，值1=已签，0=未签
     */
    private List<Integer> parseByteArray(byte[] bytes, int daysInMonth) {
        List<Integer> result = new ArrayList<>(daysInMonth);

        for (int day = 1; day <= daysInMonth; day++) {
            int offset = day - 1;           // 1号 -> 偏移0，17号 -> 偏移16
            int byteIndex = offset / 8;     // 位于第几个字节
            int bitIndex = 7 - (offset % 8); // 位于该字节的第几位（Redis 高位在前）

            // 关键：如果 byteIndex 超出实际字节长度，说明该位及后续所有位全是 0
            if (byteIndex < bytes.length) {
                // 右移 bitIndex 位，然后与 1 按位与，取出该位的值（0 或 1）
                int bitValue = (bytes[byteIndex] >> bitIndex) & 1;
                result.add(bitValue);
            } else {
                result.add(0); // 超出范围的全补 0
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private int[] getExtraDayInfo(Long userId) {
        Integer totalDays, consecutiveDays;
        Map entries = redisTemplate.opsForHash().entries(ToolboxConstants.Cache.SIGN_INFO + userId);
        if (entries.size() != 2) {
            UserSignStats userSignStats = userSignStatsRepositoryImpl.findByUserId(userId);
            if(userSignStats == null){
                userSignStats = userSignStatsRepositoryImpl.initSignStat(userId, 0, 0, 0);
            }
            totalDays = userSignStats.getTotalDays();
            consecutiveDays = userSignStats.getCurrentContinuousDays();
            resetHash(totalDays,consecutiveDays, userId);
        } else {
            consecutiveDays = (Integer) entries.get(ToolboxConstants.Cache.CONSECUTIVE_DAYS);
            totalDays = (Integer) entries.get(ToolboxConstants.Cache.TOTAL_DAYS);
        }
        return new int[]{consecutiveDays, totalDays};
    }

    /**
     * 缓存重建 + 签到自增
     */
    @SuppressWarnings("unchecked")
    private void cacheHash(Long userId, boolean isContinue, UserSignStats userSignStats) {
        String key = ToolboxConstants.Cache.SIGN_INFO + userId;
        HashOperations hashOperations = redisTemplate.opsForHash();
        if (hashOperations.hasKey(key, ToolboxConstants.Cache.CONSECUTIVE_DAYS) && hashOperations.hasKey(key, ToolboxConstants.Cache.TOTAL_DAYS)) {
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                    HashOperations hashOperations = operations.opsForHash();
                    if (isContinue) {
                        hashOperations.increment(key, ToolboxConstants.Cache.CONSECUTIVE_DAYS, 1);

                    } else {
                        hashOperations.put(key, ToolboxConstants.Cache.CONSECUTIVE_DAYS, 1);
                    }
                    hashOperations.increment(key, ToolboxConstants.Cache.TOTAL_DAYS, 1);
                    return null;
                }
            });
        } else {
            Integer totalDays = userSignStats.getTotalDays();
            Integer currentContinuousDays = userSignStats.getCurrentContinuousDays();
            resetHash(++totalDays, ++currentContinuousDays, userId);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cacheBit(List<Integer> list, Long userId, Integer month) {
        LocalDate now = LocalDate.now();
        LocalDate targetMonth = LocalDate.of(now.getYear(), month, 1);
        int length = targetMonth.lengthOfMonth();
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                ValueOperations valueOperations = operations.opsForValue();
                for (int i = 0; i < length; i++) {
                    valueOperations.setBit(buildKey(now.getYear(), month, userId), i, i < list.size() && (list.get(i) == 1));
                }
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void resetHash(Integer totalDays, Integer currentContinuousDays, Long userId) {
        Map<String, Integer> map = Map.of(ToolboxConstants.Cache.TOTAL_DAYS, totalDays, ToolboxConstants.Cache.CONSECUTIVE_DAYS, currentContinuousDays);
        redisTemplate.opsForHash().putAndExpire(ToolboxConstants.Cache.SIGN_INFO + userId, map, RedisHashCommands.HashFieldSetOption.ifNoneExist(), Expiration.from(Duration.of(ToolboxConstants.Business.TTL, ChronoUnit.DAYS)));

    }

}
