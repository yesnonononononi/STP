package com.summit.stp.activity.domain.model;

import com.summit.stp.common.application.domain.exception.BusinessException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@Builder
public class CouponActivity {
    private final Long id;
    private final Long couponId;
    private final String name;
    private Integer stock;
    private  LocalDateTime activityStartTime;
    private  LocalDateTime activityEndTime;
    private Integer status;
    private final Type type;
    private final LocalDateTime createTime;

    public void start() {
        this.status = 1;
    }

    public void stop() {
        this.status = 0;
    }

    @Getter
    public enum Type {
        NORMAL(1),
        SECKILL(2);
        private final Integer code;
        Type(Integer code) {
            this.code = code;
        }
        public static Type fromCode(Integer code) {
            for (Type value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    public void deductStock() {
        if (this.stock == null || this.stock <= 0) {
            throw new BusinessException("优惠券活动库存不足，无法领取！");
        }
        this.stock -= 1;
    }

    public long getDuration(LocalDateTime now) {
        return Duration.between(now, this.activityEndTime).toMillis();
    }

    public boolean isAvailable(LocalDateTime now) {
        return this.status != null && this.status == 1 
                && hasStarted(now) && !hasEnded(now);
    }

    public boolean hasStarted(LocalDateTime now) {
        return this.activityStartTime == null || now.isAfter(this.activityStartTime) || now.isEqual(this.activityStartTime);
    }

    public boolean hasEnded(LocalDateTime now) {
        return this.activityEndTime != null && now.isAfter(this.activityEndTime);
    }

    public boolean canDelete(LocalDateTime now) {
        if (this.activityStartTime != null && now.isBefore(this.activityStartTime)) {
            return true;
        }
        if (this.activityEndTime != null && now.isAfter(this.activityEndTime)) {
            return true;
        }
        return false;
    }
}
