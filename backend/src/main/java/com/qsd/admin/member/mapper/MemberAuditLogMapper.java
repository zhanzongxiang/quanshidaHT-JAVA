package com.qsd.admin.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.member.entity.MemberAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MemberAuditLogMapper extends BaseMapper<MemberAuditLog> {

    @Select("""
        select id, tenant_id, member_id, action_type, operator_type, operator_id, operator_label, summary, detail_json, created_at
        from member_audit_log
        where tenant_id = #{tenantId}
          and member_id = #{memberId}
        order by created_at desc, id desc
        limit #{limit}
        """)
    List<MemberAuditLog> selectRecentByMemberId(
        @Param("tenantId") Long tenantId,
        @Param("memberId") Long memberId,
        @Param("limit") int limit
    );
}
