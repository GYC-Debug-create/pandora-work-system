package com.lei.springbootpractice4.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate; // <--- 引入 LocalDate
import java.time.LocalDateTime;
import java.util.List;

@TableName("tasks")
@Data
public class TaskPojo {
    @TableId(value = "task_id", type = IdType.AUTO)
    private Integer taskId;
    private String taskName;
    private String description;
    private String creatorId;
    private String responsiblePersonId;
    private String taskType;
    private String priority;
    private String status;
    private LocalDate startDate; // <--- 新增开始日期
    private LocalDate endDate;   // <--- 新增结束日期 (替代 dueDate)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- 非数据库字段 ---
    @TableField(exist = false)
    private String creatorName;
    @TableField(exist = false)
    private String responsiblePersonName;
    @TableField(exist = false)
    private Integer memberCount;
    @TableField(exist = false)
    private Integer completedSubTaskCount;
    @TableField(exist = false)
    private Integer totalSubTaskCount;
    @TableField(exist = false)
    private List<SubTaskPojo> subTasks;
    @TableField(exist = false)
    private List<UsersPojo> members;
}