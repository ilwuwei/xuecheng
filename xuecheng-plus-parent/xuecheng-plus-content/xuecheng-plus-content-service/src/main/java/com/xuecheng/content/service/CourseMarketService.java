package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.po.CourseMarket;

/**
 * <p>
 * 课程营销信息 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-02-28
 */
public interface CourseMarketService extends IService<CourseMarket> {


    /**
     * 添加课程营销信息
     * @param courseMarket
     * @return
     */
    boolean saveCourseMarket(CourseMarket courseMarket);

}
