package com.lei.springbootpractice4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lei.springbootpractice4.pojo.RolePojo; // 需要创建RolePojo
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<RolePojo> {
}