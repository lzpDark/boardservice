package com.github.lzpdark.boardservice.model;

import lombok.Data;

import java.sql.Date;

/**
 * @author lzp
 */
@Data
public class TaskModel {

    public static TaskModel create(int boardId, int positionId, String description) {
        TaskModel taskModel = new TaskModel();
        taskModel.setBoardId(boardId);
        taskModel.setPositionId(positionId);
        taskModel.setDescription(description);
        taskModel.setCreatedTime(new Date(System.currentTimeMillis()));
        taskModel.setUpdatedTime(taskModel.getCreatedTime());
        taskModel.setState(0);
        return taskModel;
    }

    private int id;
    private int positionId;
    private int boardId;
    private int state; // 0 to-be-done, 1 in-progress, 2 done
    private String description;
    private Date createdTime;
    private Date updatedTime;
}
