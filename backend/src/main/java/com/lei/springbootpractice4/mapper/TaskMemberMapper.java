package com.lei.springbootpractice4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lei.springbootpractice4.pojo.TaskMemberPojo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMemberMapper extends BaseMapper<TaskMemberPojo> {
}