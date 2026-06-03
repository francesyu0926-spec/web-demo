package com.guandian.bidding.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页结果。
 */
@Data
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "总条数")
    private long total;

    @Schema(description = "当前页")
    private long pageNum;

    @Schema(description = "每页条数")
    private long pageSize;

    public static <T> PageResult<T> of(List<T> list, long total, long pageNum, long pageSize) {
        PageResult<T> p = new PageResult<>();
        p.list = list == null ? Collections.emptyList() : list;
        p.total = total;
        p.pageNum = pageNum;
        p.pageSize = pageSize;
        return p;
    }
}
