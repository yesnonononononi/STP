package com.summit.stp.common.application.api.dto;


import lombok.Data;

@Data
public class RangeDTO<T> {

    private T min;
    private T max;

}
