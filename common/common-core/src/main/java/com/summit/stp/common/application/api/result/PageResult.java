package com.summit.stp.common.application.api.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    private Number page;
    private Long total;
    private T data;

    public static <V>PageResult<V> empty(){
        return new PageResult<>(null,null,null);
    }
}
