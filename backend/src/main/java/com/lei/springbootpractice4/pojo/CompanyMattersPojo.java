package com.lei.springbootpractice4.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("company_matters")
@Data
public class CompanyMattersPojo {
    @TableId(value = "matter_id", type = IdType.AUTO)
    private Integer matterId;
    private String title;
    private String content;
    private String creatorId; // Assuming creator is a user ID (String)
    private Integer importanceLevel;
    private LocalDateTime createdAt;
}