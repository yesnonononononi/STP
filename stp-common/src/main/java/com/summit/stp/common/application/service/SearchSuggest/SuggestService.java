package com.summit.stp.common.application.service.SearchSuggest;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

public interface SuggestService<T> {

    SuggestVO getSuggestList(SuggestDto dto, SFunction<T, String> column, SFunction<T, Long> id, SFunction<T, Object> extra);
}
