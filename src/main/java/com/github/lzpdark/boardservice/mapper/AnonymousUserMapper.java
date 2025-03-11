package com.github.lzpdark.boardservice.mapper;

import com.github.lzpdark.boardservice.model.AnonymousUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * @author lzp
 */
@Mapper
public interface AnonymousUserMapper {

    @Insert("""
        insert into anonymous_user (createdTime) values (#{createdTime})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createAnonymousUser(AnonymousUser anonymousUser);
}
