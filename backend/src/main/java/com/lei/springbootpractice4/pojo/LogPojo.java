package com.lei.springbootpractice4.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("logs")
@Data
@NoArgsConstructor // 需要一个无参构造函数
public class LogPojo {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Integer logId;

    private String userId; // 执行操作的用户 ID
    private Integer relatedTaskId; // 关联的主任务 ID
    private Integer relatedSubTaskId; // 关联的子任务 ID
    private String activityType; // 活动类型 (枚举值)
    private String description; // 人类可读的描述
    private LocalDateTime createdAt; // 自动生成

    @TableField(exist = false)
    private String userName; // 执行操作的用户名 (或全名)

    @TableField(exist = false)
    private String relatedTaskName; // 关联的主任务名称

    @TableField(exist = false)
    private String relatedSubTaskName; // 关联的子任务名称

    // 方便调用的构造函数
    public LogPojo(String userId, Integer relatedTaskId, Integer relatedSubTaskId, String activityType, String description) {
        this.userId = userId;
        this.relatedTaskId = relatedTaskId;
        this.relatedSubTaskId = relatedSubTaskId;
        this.activityType = activityType;
        this.description = description;
        this.createdAt = LocalDateTime.now(); // 自动设置当前时间
    }
}