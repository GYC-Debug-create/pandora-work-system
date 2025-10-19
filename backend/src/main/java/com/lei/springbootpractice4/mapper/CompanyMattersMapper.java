package com.lei.springbootpractice4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lei.springbootpractice4.pojo.CompanyMattersPojo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CompanyMattersMapper extends BaseMapper<CompanyMattersPojo> {
}