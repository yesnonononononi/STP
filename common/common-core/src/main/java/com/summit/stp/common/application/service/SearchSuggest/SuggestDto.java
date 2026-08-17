package com.summit.stp.common.application.service.SearchSuggest;


import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class SuggestDto {
    /**
     * 返回建议数量
     */
    private Integer limit;
    /**
     * 关键词
     */
    private String keyword;
}
