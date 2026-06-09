package com.qsd.admin.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.payment.dto.NotifyFailureStatResponse;
import com.qsd.admin.payment.entity.RefundNotifyLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RefundNotifyLogMapper extends BaseMapper<RefundNotifyLog> {

    @Select("""
        select id, tenant_id, refund_order_id, notify_type, resource_id, notify_status, raw_payload, process_result,
               notified_at, created_at
        from refund_notify_log
        where tenant_id = #{tenantId}
          and refund_order_id = #{refundOrderId}
        order by id desc
        """)
    List<RefundNotifyLog> selectByRefundOrderId(@Param("tenantId") Long tenantId, @Param("refundOrderId") Long refundOrderId);

    @Select("""
        select id, tenant_id, refund_order_id, notify_type, resource_id, notify_status, raw_payload, process_result,
               notified_at, created_at
        from refund_notify_log
        where tenant_id = #{tenantId}
          and id = #{id}
        limit 1
        """)
    RefundNotifyLog selectActiveById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
        select notify_status as category, count(*) as count, max(created_at) as latestCreatedAt
        from refund_notify_log
        where tenant_id = #{tenantId}
          and notify_status not in ('succeeded', 'SUCCESS', 'received')
        group by notify_status
        order by count(*) desc, max(created_at) desc
        limit 10
        """)
    List<NotifyFailureStatResponse> selectFailureStats(@Param("tenantId") Long tenantId);
}
