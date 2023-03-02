package com.xuecheng.content.service;

import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.model.po.CourseBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseBaseServiceTest {

    @Autowired
    CourseBaseService courseBaseService;

    @Test
    void getCourseBase(){
        CourseBase courseBase = courseBaseService.getById(1);
        System.out.println(courseBase);
    }
}
