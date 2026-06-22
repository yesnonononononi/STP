package com.summit.stp.shared.service.SearchSuggest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AbstractSuggest<T> implements SuggestService<T> {

    protected abstract BaseMapper<T> getBaseMapper();

    @Override
    public SuggestVO getSuggestList(SuggestDto dto, SFunction<T, String> column, SFunction<T, Long> id, SFunction<T, Object> extra) {
        try {
            Integer limit = dto.getLimit();
            String keyword = dto.getKeyword().trim();
            LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<T> likeOfWrapper = wrapper.like(StringUtils.isNotBlank(keyword), column, keyword).last("limit " + limit);
            BaseMapper<T> mapper = getBaseMapper();
            List<T> res = mapper.selectList(likeOfWrapper);
            return SuggestVO
                    .builder()
                    .limit(limit)
                    .suggestList(
                            res
                                    .stream()
                                    .map(item -> new SuggestListVO(id.apply(item), column.apply(item), extra.apply(item))).toList()
                    )
                    .keyword(keyword)
                    .total(res.size())
                    .build();
        } catch (Exception e) {
            log.error("【建议】查询建议列表失败，dto:{}", dto, e);
            throw e;
        }
    }
}

