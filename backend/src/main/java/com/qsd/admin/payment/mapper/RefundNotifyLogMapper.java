package com.qsd.admin.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.payment.dto.NotifyFailureStatResponse;
import com.qsd.admin.payment.entity.RefundNotifyLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RefundNotifyLogMapper extends BaseMapper<RefundNotifyLog> {

    @Select("""
        select id, refund_order_id, notify_type, resource_id, notify_status, raw_payload, process_result,
               notified_at, created_at
        from refund_notify_log
        where refund_order_id = #{refundOrderId}
        order by id desc
        """)
    List<RefundNotifyLog> selectByRefundOrderId(Long refundOrderId);

    @Select("""
        select notify_status as category, count(*) as count, max(created_at) as latestCreatedAt
        from refund_notify_log
        where notify_status not in ('succeeded', 'SUCCESS', 'received')
        group by notify_status
        order by count(*) desc, max(created_at) desc
        limit 10
        """)
    List<NotifyFailureStatResponse> selectFailureStats();
}
