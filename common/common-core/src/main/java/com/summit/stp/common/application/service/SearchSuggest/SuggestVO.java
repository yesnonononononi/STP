package com.summit.stp.common.application.service.SearchSuggest;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SuggestVO {
    private Integer limit;
    private Integer total;
    private String  keyword;
    private List<SuggestListVO> suggestList;
}
