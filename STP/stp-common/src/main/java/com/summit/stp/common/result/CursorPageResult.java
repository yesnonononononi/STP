package com.summit.stp.common.result;

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
}
