package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CourseMarketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
@Transactional
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private CourseMarketService courseMarketService;


    @Override
    public PageResult<CourseBase> queryCourseBaseList(
            PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        // 构造分页条件
        IPage<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 条件查询
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 课程名称模糊查询
        queryWrapper.like(StringUtils.isNotBlank(queryCourseParamsDto.getCourseName()), CourseBase::getCompanyName, queryCourseParamsDto.getCourseName());
        // 课程审核状态
        queryWrapper.eq(StringUtils.isNotBlank(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        // 课程发布状态
        queryWrapper.eq(StringUtils.isNotBlank(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        // 查询数据
        IPage<CourseBase> courseBaseIPage = courseBaseMapper.selectPage(page, queryWrapper);
        // 返回分页结果
        return PageResult.handlerIPage(courseBaseIPage);
    }


    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //新增对象
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给新增对象
        BeanUtils.copyProperties(dto, courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //插入课程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);
        Long courseId = courseBaseNew.getId();
        //课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarketNew);
        courseMarketNew.setId(courseId);

        //插入课程营销信息
        boolean flag = courseMarketService.saveCourseMarket(courseMarketNew);

        if (insert == 0 || !flag) {
            throw new RuntimeException("新增课程基本信息失败");
        }
        //添加成功
        //返回添加的课程信息
        return getCourseBaseInfo(courseId);

    }

    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        if (courseBase == null || courseMarket == null) {
            return null;
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();

        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;

    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        // 判断该课程此时是否还存在
        CourseBase courseInfo = courseBaseMapper.selectById(editCourseDto.getId());
        CourseMarket courseMarketInfo = courseMarketMapper.selectById(editCourseDto.getId());
        if (Objects.isNull(courseInfo) || Objects.isNull(courseMarketInfo)) {
            XueChengPlusException.cast("该课程信息已不存在");
        }
        // 判断该课程的教育机构是否属于当前用户
        if (!companyId.equals(courseInfo.getCompanyId())) {
            XueChengPlusException.cast("不能修改其他机构的id");
        }
        // 修改课程基本信息
        BeanUtils.copyProperties(editCourseDto, courseInfo);
        int i = courseBaseMapper.updateById(courseInfo);
        // 修改营销信息
        BeanUtils.copyProperties(editCourseDto, courseMarketInfo);
        boolean flag = courseMarketService.saveCourseMarket(courseMarketInfo);
        if (i == 0 || !flag) {
            XueChengPlusException.cast("修改失败");
        }
        return getCourseBaseInfo(editCourseDto.getId());
    }
}
