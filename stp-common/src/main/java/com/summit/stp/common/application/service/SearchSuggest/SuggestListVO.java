package com.summit.stp.common.application.service.SearchSuggest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuggestListVO {
    private Long id;
    private String keyword;
    private Object extra;
}

