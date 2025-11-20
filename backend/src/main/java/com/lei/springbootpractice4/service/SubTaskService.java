package com.lei.springbootpractice4.service;

import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.pojo.UsersPojo;

public interface SubTaskService {
    SubTaskPojo createSubTask(SubTaskPojo subTask, String creatorId,String location);
    SubTaskPojo submitDeliverable(Integer subTaskId, String deliverableContentJson, String currentUserId,String location);
    SubTaskPojo reviewSubTask(Integer subTaskId, boolean approved, String comment, String reviewerId,String location);
    // 后续可以添加 getSubTaskById 等方法
    SubTaskPojo completeSubTask(Integer subTaskId, String currentUserId,String location);
    SubTaskPojo getSubTaskById(Integer subTaskId, UsersPojo currentUser);
}