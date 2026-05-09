package com.qsd.admin.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.payment.dto.NotifyFailureStatResponse;
import com.qsd.admin.payment.entity.PayNotifyLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PayNotifyLogMapper extends BaseMapper<PayNotifyLog> {

    @Select("""
        select id, pay_order_id, notify_type, resource_id, notify_status, raw_payload, process_result,
               notified_at, created_at
        from pay_notify_log
        where pay_order_id = #{payOrderId}
        order by id desc
        """)
    List<PayNotifyLog> selectByPayOrderId(Long payOrderId);

    @Select("""
        select notify_status as category, count(*) as count, max(created_at) as latestCreatedAt
        from pay_notify_log
        where notify_status not in ('paid', 'closed', 'SUCCESS', 'received')
        group by notify_status
        order by count(*) desc, max(created_at) desc
        limit 10
        """)
    List<NotifyFailureStatResponse> selectFailureStats();
}
