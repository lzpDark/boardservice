package com.github.lzpdark.boardservice.controller;


import com.github.lzpdark.boardservice.exception.CustomAuthenticationException;
import com.github.lzpdark.boardservice.mapper.BoardMapper;
import com.github.lzpdark.boardservice.mapper.TaskMapper;
import com.github.lzpdark.boardservice.model.BoardModel;
import com.github.lzpdark.boardservice.model.TaskModel;
import com.github.lzpdark.boardservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author lzp
 */
@CrossOrigin
@RestController
@RequestMapping("/task")
@Slf4j
public class TaskController {

    @Autowired
    TaskMapper taskMapper;
    @Autowired
    UserService userService;
    @Autowired
    BoardMapper boardMapper;

    private int getBoardId(HttpServletRequest request) throws AuthenticationException {
        String username = userService.getCurrentUsername();
        if (StringUtils.hasLength(username)) {
            // this is authenticated user, get board from db
            BoardModel board = boardMapper.getBoardByUsername(username);
            if (board == null) {
                // ?
                log.error("no board for user: {}", username);
                throw new RuntimeException("no board error");
            }
            return board.getId();
        } else {
            // not authenticated
            log.error("not authenticated but pass to taskController, refuse api.");
            throw new CustomAuthenticationException("no session");
        }
    }

    @PostMapping("")
    public TaskModel addTask(HttpServletRequest request,
                             @RequestBody TaskModel taskModel) throws AuthenticationException {

        int boardId = getBoardId(request);
        int latestPosition = taskMapper.getLatestPosition(boardId, taskModel.getState());
        TaskModel model = TaskModel.create(boardId, latestPosition + 1, taskModel.getDescription());
        taskMapper.createModel(model);
        return taskMapper.getTaskModel(model.getId(), boardId);
    }

    @GetMapping("")
    public List<TaskModel> listTask(HttpServletRequest request) throws AuthenticationException {
        int boardId = getBoardId(request);
        return taskMapper.getAllTaskModels(boardId);
    }

    @PutMapping("")
    public Object updateTask(HttpServletRequest request, @RequestBody TaskModel taskModel) throws AuthenticationException {
        int boardId = getBoardId(request);
        TaskModel oldModel = taskMapper.getTaskModel(taskModel.getId(), boardId);
        if (oldModel == null) {
            throw new IllegalArgumentException("no such data");
        }
        oldModel.setDescription(taskModel.getDescription());
        oldModel.setUpdatedTime(new Date(System.currentTimeMillis()));
        taskMapper.updateTaskModel(oldModel);
        return taskMapper.getTaskModel(oldModel.getId(), oldModel.getBoardId());
    }

    @DeleteMapping("/{id}")
    public Object deleteTask(HttpServletRequest request, @PathVariable int id) throws AuthenticationException {
        int boardId = getBoardId(request);
        TaskModel taskModel = taskMapper.getTaskModel(id, boardId);
        taskMapper.removeTaskModel(id);
        return taskModel;
    }

    @Data
    public static class ReOrderPayload {
        private Integer state;
        private Integer fromId;
        private Integer toId;
    }

    @PostMapping("/reorder")
    public Object reorder(HttpServletRequest request, @RequestBody ReOrderPayload payload) throws AuthenticationException {
        Assert.notNull(payload.getState(), "invalid state");
        Assert.notNull(payload.getFromId(), "invalid fromId");
        Assert.notNull(payload.getToId(), "invalid toId");

        int boardId = getBoardId(request);
        TaskModel fromTask = taskMapper.getTaskModel(payload.getFromId(), boardId);
        TaskModel toTask = taskMapper.getTaskModel(payload.getToId(), boardId);
        if (fromTask == null || toTask == null) {
            throw new IllegalArgumentException("invalid param");
        }
        if (fromTask.getState() != toTask.getState()) {
            throw new IllegalArgumentException("not in same state");
        }
        int tmp = fromTask.getPositionId();
        fromTask.setPositionId(toTask.getPositionId());
        toTask.setPositionId(tmp);
        Date date = new Date(System.currentTimeMillis());
        fromTask.setUpdatedTime(date);
        toTask.setUpdatedTime(date);
        taskMapper.updateTaskModel(fromTask);
        taskMapper.updateTaskModel(toTask);
        return true;
    }

    @Data
    public static class MoveItemPayload {
        private Integer fromState;
        private Integer toState;

        // task in fromState move to toState, and set position exact behind toId task,
        // if toId is null, means move to bottom.(there might be none task, so noe always exists toId)
        private Integer fromId;
        private Integer toId;
    }

    @PostMapping("/moveitem")
    public Object moveItem(HttpServletRequest request, @RequestBody MoveItemPayload payload) throws AuthenticationException {
        Assert.notNull(payload.getFromState(), "invalid fromState");
        Assert.notNull(payload.getToState(), "invalid toState");
        Assert.notNull(payload.getFromId(), "invalid fromId");
        int boardId = getBoardId(request);
        TaskModel fromTask = taskMapper.getTaskModel(payload.getFromId(), boardId);
        if (fromTask == null) {
            throw new IllegalArgumentException("invalid param");
        }
        Date date = new Date(System.currentTimeMillis());
        if (payload.getToId() == null) {
            // move to bottom of toState
            int latestPosition = taskMapper.getLatestPosition(boardId, payload.getToState());
            fromTask.setState(payload.getToState());
            fromTask.setPositionId(latestPosition + 1);
            fromTask.setUpdatedTime(date);
            taskMapper.updateTaskModel(fromTask);
        } else {
            TaskModel toTask = taskMapper.getTaskModel(payload.getToId(), boardId);
            if (fromTask.getState() == toTask.getState()) {
                throw new IllegalArgumentException("not in different state");
            }
            // update a list of task behind toTask position
            List<TaskModel> taskModels = taskMapper.getAllTaskModelsWithState(boardId, toTask.getState());
            List<TaskModel> toUpdateModels = new ArrayList<>();
            fromTask.setState(payload.getToState());
            toUpdateModels.add(fromTask);
            if (taskModels != null) {
                toUpdateModels.addAll(taskModels.stream()
                        .filter(i -> i.getPositionId() >= toTask.getPositionId())
                        .sorted(Comparator.comparingInt(TaskModel::getPositionId))
                        .toList());
            }
            int destinationPositionId = toTask.getPositionId();
            for (TaskModel taskModel : toUpdateModels) {
                taskModel.setPositionId(destinationPositionId++);
                taskModel.setUpdatedTime(date);
                taskMapper.updateTaskModel(taskModel);
            }

        }
        return true;
    }
}
