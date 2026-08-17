package com.summit.stp.common.application.api.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResult<T> {
    private List<T> list;
    private String cursor;
    private Boolean hasMore;

    public static <T>CursorPageResult<T > empty(){
        return new CursorPageResult<>(List.of(),null,false);
    }
}
