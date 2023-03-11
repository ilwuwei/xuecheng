package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-02-28
 */
public interface TeachplanService extends IService<Teachplan> {

    /**
     * 查询课程计划树型结构
     * @param courseId
     * @return
     */
    List<TeachplanDto> findTeachplayTree(long courseId);

    /**
     * 添加或修改课程计划
     * @param teachplanDto
     */
    void saveTeachplan(Teachplan teachplanDto);


}
