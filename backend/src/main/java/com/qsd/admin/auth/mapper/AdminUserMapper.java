package com.qsd.admin.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.auth.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    @Select("select id, username, password_hash, status from admin_user where username = #{username} and deleted = 0 limit 1")
    AdminUser selectByUsername(String username);

    @Select("""
        select p.perm_code
        from admin_user_role ur
        join admin_role_permission rp on ur.role_id = rp.role_id
        join permission p on rp.permission_id = p.id
        where ur.user_id = #{userId}
        """)
    List<String> selectPermissionCodes(Long userId);
}
