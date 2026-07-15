package com.qsd.admin.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.member.entity.MemberUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MemberUserMapper extends BaseMapper<MemberUser> {

    @Select("""
        select id, member_no, username, mobile, password_hash, nickname, real_name, level_code, status, remark, last_login_at, created_at, updated_at, deleted
        from member_user
        where deleted = 0
          and (username = #{keyword} or mobile = #{keyword})
        limit 1
        """)
    MemberUser selectByUsernameOrMobile(String keyword);
}
