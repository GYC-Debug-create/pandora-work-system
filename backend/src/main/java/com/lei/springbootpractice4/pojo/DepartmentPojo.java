package com.lei.springbootpractice4.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("departments")
@Data
public class DepartmentPojo {
    @TableId(value = "department_id", type = IdType.AUTO)
    private Integer departmentId;
    private String departmentName;
}