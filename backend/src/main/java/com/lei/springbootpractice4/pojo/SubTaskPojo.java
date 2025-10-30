package com.lei.springbootpractice4.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime; // <--- 确保引入 LocalDateTime
import java.util.List;

@TableName("sub_tasks")
@Data
public class SubTaskPojo {
    @TableId(value = "sub_task_id", type = IdType.AUTO)
    private Integer subTaskId;
    private Integer parentTaskId;
    private Integer parentSubTaskId;
    private String creatorId;
    private String subTaskName;
    private String status;
    private String assignedToId;
    private LocalDateTime startTime; // <--- 新增开始时间
    private LocalDateTime endTime;   // <--- 新增结束时间
    private String deliverableContent;
    private String approvalComment;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt; // <--- 新增更新时间

    // --- 非数据库字段 ---
    @TableField(exist = false)
    private String assignedToName;
    @TableField(exist = false)
    private String creatorName;
    @TableField(exist = false)
    private List<SubTaskPojo> children;

}