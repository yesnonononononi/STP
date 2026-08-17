package com.summit.stp.common.application.api.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class ESPageVO<T> {
    private Long total;
    private Integer page;
    private List<T> data;
}
