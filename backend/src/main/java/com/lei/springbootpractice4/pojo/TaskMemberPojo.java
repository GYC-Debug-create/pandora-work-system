package com.lei.springbootpractice4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.AllArgsConstructor; // 方便创建对象
import lombok.NoArgsConstructor; // 需要无参构造
import java.time.LocalDateTime;

@TableName("task_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskMemberPojo {
    private Integer taskId;
    private String userId;
    private LocalDateTime assignedAt;
}