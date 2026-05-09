package com.qsd.admin.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.payment.entity.RefundOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RefundOrderMapper extends BaseMapper<RefundOrder> {

    @Select("""
        select id, refund_no, pay_order_id, amount_refund, status, reason, external_refund_no,
               refunded_at, created_at, updated_at
        from refund_order
        where pay_order_id = #{payOrderId}
        order by id desc
        """)
    List<RefundOrder> selectByPayOrderId(Long payOrderId);

    @Select("""
        select id, refund_no, pay_order_id, amount_refund, status, reason, external_refund_no,
               refunded_at, created_at, updated_at
        from refund_order
        where refund_no = #{refundNo}
        limit 1
        """)
    RefundOrder selectByRefundNo(String refundNo);

    @Select("""
        select id, refund_no, pay_order_id, amount_refund, status, reason, external_refund_no,
               refunded_at, created_at, updated_at
        from refund_order
        where id = #{id}
        limit 1
        """)
    RefundOrder selectByIdValue(Long id);
}
