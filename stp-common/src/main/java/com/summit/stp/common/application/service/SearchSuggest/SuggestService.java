package com.summit.stp.common.application.service.SearchSuggest;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

public interface SuggestService<T> {
    public SuggestVO getSuggestList(SuggestDto dto, SFunction<T, String> column, SFunction<T, String> id,SFunction<T,Object> extra);
}
