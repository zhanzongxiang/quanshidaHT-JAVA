package com.qsd.admin.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.payment.entity.PayTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PayTransactionMapper extends BaseMapper<PayTransaction> {

    @Select("""
        select id, pay_order_id, transaction_type, transaction_status, request_payload, response_payload,
               external_transaction_no, external_out_trade_no, success_time, created_at
        from pay_transaction
        where pay_order_id = #{payOrderId}
        order by id desc
        """)
    List<PayTransaction> selectByPayOrderId(Long payOrderId);
}
