package com.lei.springbootpractice4.service;

import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.pojo.UsersPojo;

public interface SubTaskService {
    SubTaskPojo createSubTask(SubTaskPojo subTask, String creatorId);
    SubTaskPojo submitDeliverable(Integer subTaskId, String deliverableContentJson, String currentUserId);
    SubTaskPojo reviewSubTask(Integer subTaskId, boolean approved, String comment, String reviewerId);
    // 后续可以添加 getSubTaskById 等方法
    SubTaskPojo completeSubTask(Integer subTaskId, String currentUserId);
    SubTaskPojo getSubTaskById(Integer subTaskId, UsersPojo currentUser);
}