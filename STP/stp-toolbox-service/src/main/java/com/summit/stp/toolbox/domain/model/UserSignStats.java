package com.summit.stp.toolbox.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserSignStats {
    private final Long userId;
    private Integer totalDays;
    private Integer currentContinuousDays;
    private Integer maxContinuousDays;
    private LocalDate lastSignDate;
    private LocalDateTime updateTime;


    public void updateStat(int totalDays,int currentContinuousDays,LocalDate lastSignDate){
        this.totalDays = totalDays;
        this.currentContinuousDays = currentContinuousDays;
        this.lastSignDate = lastSignDate;
        if(currentContinuousDays > maxContinuousDays){
            this.maxContinuousDays = currentContinuousDays;
        }
        this.updateTime = LocalDateTime.now();
    }

    public void sign(){
        this.totalDays ++;
        LocalDate today = LocalDate.now();
        if (lastSignDate != null && lastSignDate.plusDays(1).equals(today)) {
            this.currentContinuousDays ++;
        } else {
            this.currentContinuousDays = 1;
        }
        this.lastSignDate = today;
        if(currentContinuousDays > maxContinuousDays){
            this.maxContinuousDays = currentContinuousDays;
        }
        this.updateTime = LocalDateTime.now();
    }
}
