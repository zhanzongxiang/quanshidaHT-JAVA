package com.qsd.admin.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.payment.entity.PayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {

    @Select("""
        <script>
        select po.id, po.order_no, po.member_id, po.waybill_id, po.business_type, po.scene_type, po.channel,
               po.merchant_config_id, po.merchant_name, po.merchant_mch_id, po.merchant_app_id,
               po.currency, po.amount_total, po.amount_paid, po.status, po.description, po.external_transaction_no,
               po.paid_at, po.expired_at, po.closed_at, po.refunded_at, po.remark, po.deleted, po.created_at, po.updated_at
        from pay_order po
        left join member_user mu on mu.id = po.member_id
        left join waybill_order wo on wo.id = po.waybill_id
        where po.deleted = 0
          <if test="keyword != null and keyword != ''">
            and (
              po.order_no like concat('%', #{keyword}, '%')
              or coalesce(po.external_transaction_no, '') like concat('%', #{keyword}, '%')
              or coalesce(mu.phone, '') like concat('%', #{keyword}, '%')
              or coalesce(wo.main_tracking_no, '') like concat('%', #{keyword}, '%')
            )
          </if>
          <if test="status != null and status != ''">
            and po.status = #{status}
          </if>
          <if test="channel != null and channel != ''">
            and po.channel = #{channel}
          </if>
        order by po.updated_at desc, po.id desc
        </script>
        """)
    List<PayOrder> selectAdminList(
        @Param("keyword") String keyword,
        @Param("status") String status,
        @Param("channel") String channel
    );

    @Select("""
        select id, order_no, member_id, waybill_id, business_type, scene_type, channel,
               merchant_config_id, merchant_name, merchant_mch_id, merchant_app_id,
               currency, amount_total, amount_paid, status, description, external_transaction_no,
               paid_at, expired_at, closed_at, refunded_at, remark, deleted, created_at, updated_at
        from pay_order
        where id = #{id}
          and deleted = 0
        limit 1
        """)
    PayOrder selectActiveById(Long id);

    @Select("""
        select id, order_no, member_id, waybill_id, business_type, scene_type, channel,
               merchant_config_id, merchant_name, merchant_mch_id, merchant_app_id,
               currency, amount_total, amount_paid, status, description, external_transaction_no,
               paid_at, expired_at, closed_at, refunded_at, remark, deleted, created_at, updated_at
        from pay_order
        where member_id = #{memberId}
          and deleted = 0
        order by updated_at desc, id desc
        """)
    List<PayOrder> selectByMemberId(Long memberId);

    @Select("""
        select id, order_no, member_id, waybill_id, business_type, scene_type, channel,
               merchant_config_id, merchant_name, merchant_mch_id, merchant_app_id,
               currency, amount_total, amount_paid, status, description, external_transaction_no,
               paid_at, expired_at, closed_at, refunded_at, remark, deleted, created_at, updated_at
        from pay_order
        where order_no = #{orderNo}
          and deleted = 0
        limit 1
        """)
    PayOrder selectByOrderNo(String orderNo);

    @Select("""
        select id, order_no, member_id, waybill_id, business_type, scene_type, channel,
               merchant_config_id, merchant_name, merchant_mch_id, merchant_app_id,
               currency, amount_total, amount_paid, status, description, external_transaction_no,
               paid_at, expired_at, closed_at, refunded_at, remark, deleted, created_at, updated_at
        from pay_order
        where deleted = 0
          and date(created_at) = #{billDate}
        order by id asc
        """)
    List<PayOrder> selectByBillDate(@Param("billDate") LocalDate billDate);
}
