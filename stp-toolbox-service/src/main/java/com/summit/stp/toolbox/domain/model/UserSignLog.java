package com.summit.stp.toolbox.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserSignLog {
    private Long id;
    private Long userId;
    private LocalDate signDate;
    private LocalDateTime signTime;
    private Integer signSource;
    private Integer rewardPoints;
    private Integer continuousDaysSnapshot;
    private String extra;
}
