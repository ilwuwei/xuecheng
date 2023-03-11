package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    /**
     *  查询某课程的课程计划，组成树型结构
     * @param courseId
     * @return
     */
    List<TeachplanDto> selectTreeNodes(long courseId);

    /**
     * 查询同级别的课程计划数
     * @param courseId
     * @param parentId
     * @return
     */
    Integer selectTeachplanCount(@Param("courseId") Long courseId, @Param("parentId") Long parentId);

}
