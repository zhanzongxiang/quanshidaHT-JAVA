package com.qsd.admin.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.auth.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    @Select("""
        select id, tenant_id, username, password_hash, status
        from admin_user
        where username = #{username} and tenant_id = #{tenantId} and deleted = 0
        limit 1
        """)
    AdminUser selectByUsernameAndTenantId(String username, Long tenantId);

    @Select("""
        select id, tenant_id, username, password_hash, status
        from admin_user
        where id = #{id} and tenant_id = #{tenantId} and deleted = 0
        limit 1
        """)
    AdminUser selectActiveByIdAndTenantId(Long id, Long tenantId);

    @Select("""
        select p.perm_code
        from admin_user_role ur
        join admin_role_permission rp on ur.role_id = rp.role_id
        join permission p on rp.permission_id = p.id
        where ur.user_id = #{userId}
        """)
    List<String> selectPermissionCodes(Long userId);

    @Select("""
        select count(1)
        from admin_user
        where tenant_id = #{tenantId}
          and deleted = 0
          and status = 'ENABLED'
        """)
    long countEnabledByTenantId(@Param("tenantId") Long tenantId);

    @Select("""
        select id, tenant_id, username, password_hash, status
        from admin_user
        where username = #{username}
          and deleted = 0
        limit 1
        """)
    AdminUser selectActiveByUsername(@Param("username") String username);

    @Select("""
        select id
        from admin_role
        where role_code = #{roleCode}
        limit 1
        """)
    Long selectRoleIdByRoleCode(@Param("roleCode") String roleCode);

    @Select("""
        select count(1)
        from admin_user_role
        where user_id = #{userId}
          and role_id = #{roleId}
        """)
    long countUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @org.apache.ibatis.annotations.Insert("""
        insert into admin_user_role(user_id, role_id)
        values(#{userId}, #{roleId})
        """)
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
