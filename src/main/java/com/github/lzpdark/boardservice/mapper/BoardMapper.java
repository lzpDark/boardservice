package com.github.lzpdark.boardservice.mapper;

import com.github.lzpdark.boardservice.model.BoardModel;
import org.apache.ibatis.annotations.*;

/**
 * @author lzp
 */
@Mapper
public interface BoardMapper {

    @Insert("""
        insert into board (username, anonymousId, description, createdTime)
            values (#{username}, #{anonymousId}, #{description}, #{createdTime})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createBoard(BoardModel model);

    @Update("""
        update board set username=#{username}, anonymousId=#{anonymousId}, description=#{description}
        where id = #{id}
    """)
    int updateBoard(BoardModel model);

    @Select("""
        select * from board where username = #{username} limit 1
    """)
    BoardModel getBoardByUsername(String username);
}
