package com.qsd.admin.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.tenant.entity.TenantDomain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TenantDomainMapper extends BaseMapper<TenantDomain> {

    @Select("""
        select id, tenant_id, domain, domain_type, enabled, created_at
        from tenant_domain
        where domain = #{domain} and enabled = 1
        limit 1
        """)
    TenantDomain selectEnabledByDomain(String domain);

    @Select("""
        select id, tenant_id, domain, domain_type, enabled, created_at
        from tenant_domain
        where tenant_id = #{tenantId}
        order by enabled desc, id asc
        """)
    List<TenantDomain> selectByTenantId(@Param("tenantId") Long tenantId);

    @Select("""
        select id, tenant_id, domain, domain_type, enabled, created_at
        from tenant_domain
        where domain = #{domain}
        limit 1
        """)
    TenantDomain selectByDomain(@Param("domain") String domain);

    @Delete("delete from tenant_domain where tenant_id = #{tenantId}")
    int deleteByTenantId(@Param("tenantId") Long tenantId);

    @Select("""
        select count(1)
        from tenant_domain
        where tenant_id = #{tenantId}
          and enabled = 1
        """)
    long countEnabledByTenantId(@Param("tenantId") Long tenantId);
}
