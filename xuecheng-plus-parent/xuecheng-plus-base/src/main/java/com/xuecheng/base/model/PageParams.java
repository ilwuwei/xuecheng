package com.xuecheng.base.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @version 1.0
 * @description 分页查询通用参数
 */
@Data
@ToString
@ApiModel("分页查询通用参数")
public class PageParams {

    //当前页码默认值
    @ApiModelProperty("当前页码默认值")
    public static final long DEFAULT_PAGE_CURRENT = 1L;
    //每页记录数默认值
    @ApiModelProperty("每页记录数默认值")
    public static final long DEFAULT_PAGE_SIZE = 10L;

    //当前页码
    @ApiModelProperty("当前页码")
    private Long pageNo = DEFAULT_PAGE_CURRENT;

    //每页记录数默认值
    @ApiModelProperty("每页记录数默认值")
    private Long pageSize = DEFAULT_PAGE_SIZE;

    public PageParams() {

    }

    public PageParams(long pageNo, long pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }


}
