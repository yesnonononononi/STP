package com.summit.stp.shared.service.SearchSuggest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuggestListVO {
    private String id;
    private String keyword;
    private Object extra;
}

