package com.qsd.admin.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.payment.entity.RefundOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface RefundOrderMapper extends BaseMapper<RefundOrder> {

    @Select("""
        select id, tenant_id, refund_no, pay_order_id, amount_refund, status, reason, external_refund_no,
               refunded_at, created_at, updated_at
        from refund_order
        where tenant_id = #{tenantId}
          and pay_order_id = #{payOrderId}
        order by id desc
        """)
    List<RefundOrder> selectByPayOrderId(@Param("tenantId") Long tenantId, @Param("payOrderId") Long payOrderId);

    @Select("""
        select id, tenant_id, refund_no, pay_order_id, amount_refund, status, reason, external_refund_no,
               refunded_at, created_at, updated_at
        from refund_order
        where tenant_id = #{tenantId}
          and refund_no = #{refundNo}
        limit 1
        """)
    RefundOrder selectByRefundNo(@Param("tenantId") Long tenantId, @Param("refundNo") String refundNo);

    @Select("""
        select id, tenant_id, refund_no, pay_order_id, amount_refund, status, reason, external_refund_no,
               refunded_at, created_at, updated_at
        from refund_order
        where tenant_id = #{tenantId}
          and id = #{id}
        limit 1
        """)
    RefundOrder selectByIdValue(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
        select coalesce(sum(amount_refund), 0)
        from refund_order
        where tenant_id = #{tenantId}
          and pay_order_id = #{payOrderId}
          and status = 'succeeded'
        """)
    BigDecimal sumSucceededAmountByPayOrderId(@Param("tenantId") Long tenantId, @Param("payOrderId") Long payOrderId);

    @Select("""
        select count(1)
        from refund_order
        where tenant_id = #{tenantId}
          and pay_order_id = #{payOrderId}
          and status = 'processing'
        """)
    int countProcessingByPayOrderId(@Param("tenantId") Long tenantId, @Param("payOrderId") Long payOrderId);
}
