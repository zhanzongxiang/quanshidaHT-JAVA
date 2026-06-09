package com.qsd.admin.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.tenant.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {

    @Select("""
        select id, tenant_code, tenant_name, status, timezone, locale, remark, deleted, created_at, updated_at
        from tenant
        where deleted = 0
        order by id asc
        """)
    List<Tenant> selectActiveList();

    @Select("""
        select id, tenant_code, tenant_name, status, timezone, locale, remark, deleted, created_at, updated_at
        from tenant
        where tenant_code = #{tenantCode} and deleted = 0
        limit 1
        """)
    Tenant selectByTenantCode(String tenantCode);

    @Select("""
        select id, tenant_code, tenant_name, status, timezone, locale, remark, deleted, created_at, updated_at
        from tenant
        where id = #{id} and deleted = 0
        limit 1
        """)
    Tenant selectActiveById(Long id);

    @Select("""
        select id, tenant_code, tenant_name, status, timezone, locale, remark, deleted, created_at, updated_at
        from tenant
        where tenant_code = 'default' and deleted = 0
        limit 1
        """)
    Tenant selectDefaultTenant();
}
