package com.xuecheng.base.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @version 1.0
 * @description 分页查询结果模型类
 */
@Data
@ApiModel("分页查询结果模型")
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    //当前页码
    @ApiModelProperty("当前页码")
    private long currentPage;

    //每页记录数
    @ApiModelProperty("每页记录数")
    private long pageSize;

    // 数据列表
    @ApiModelProperty("数据列表")
    private List<T> items;

    //总记录数
    @ApiModelProperty("总记录数")
    private long total;

    //每页记录数
    @ApiModelProperty("总页数")
    private long pageNum;

    /**
     * 根据mybatis分页对象 构造分页结果返回
     *
     * @param page
     * @param <T>
     * @return
     */
    public static <T> PageResult<T> handlerIPage(IPage<T> page) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setCurrentPage(page.getCurrent());
        pageResult.setItems(page.getRecords());
        pageResult.setPageNum(page.getPages());
        pageResult.setTotal(page.getTotal());
        return pageResult;
    }

}