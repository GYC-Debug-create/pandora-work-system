package com.lei.springbootpractice4.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime; // 引入时间类型

@TableName("users")
@Data
public class UsersPojo {
    @TableId(value="user_id", type= IdType.ASSIGN_UUID)
    private String userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String mbtiType;
    private Integer departmentId;
    private Integer roleId; // 确保是 Integer
    private LocalDateTime createdAt; // 添加创建时间字段

    // --- 新增用于前端展示的字段 ---
    @TableField(exist = false) // 告诉MyBatis Plus这个字段不在数据库表中
    private String roleName;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private String status = "活跃"; // 暂时硬编码为 "活跃"
}