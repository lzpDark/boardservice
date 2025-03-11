package com.github.lzpdark.boardservice.mapper;

import com.github.lzpdark.boardservice.model.TaskModel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author lzp
 */
@Mapper
public interface TaskMapper {

    @Select("select ifnull(max(positionId), 1) from task where boardId = #{boardId} and state = #{state} ")
    int getLatestPosition(int boardId, int state);

    @Insert("""
        insert into task (boardId, state, positionId, description, createdTime, updatedTime)
            values (#{boardId}, #{state}, #{positionId}, #{description}, #{createdTime}, #{updatedTime})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createModel(TaskModel taskModel);

    @Select("select * from task where boardId = #{boardId}")
    List<TaskModel> getAllTaskModels(int boardId);

    @Select("select * from task where boardId = #{boardId} and state = #{state}")
    List<TaskModel> getAllTaskModelsWithState(int boardId, int state);

    @Select("select * from task where id = #{id} and boardId = #{boardId} limit 1")
    TaskModel getTaskModel(int id, int boardId);

    @Delete("delete from task where id = #{id}")
    int removeTaskModel(int id);

    @Update("update task set description=#{description}, state=#{state}, positionId=#{positionId}, updatedTime=#{updatedTime} where id = #{id}")
    int updateTaskModel(TaskModel taskModel);
}
