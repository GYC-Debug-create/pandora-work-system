package com.lei.springbootpractice4.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("users") // Correct table name mapping
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersPojo {
    @TableId(value="user_id",type= IdType.ASSIGN_UUID)
    String userId;
    String username;
    String passwordHash; // Renamed for clarity
}