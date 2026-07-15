package com.qsd.admin.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.member.entity.MemberOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MemberOrderMapper extends BaseMapper<MemberOrder> {
    @Select("""
        select id, member_id, shipment_id, order_no, order_status, payment_status, amount, currency_code, remark, created_at, updated_at
        from member_order
        where member_id = #{memberId}
        order by id desc
        """)
    List<MemberOrder> selectByMemberId(Long memberId);

    @Select("""
        <script>
        select id, member_id, shipment_id, order_no, order_status, payment_status, amount, currency_code, remark, created_at, updated_at
        from member_order
        where 1 = 1
          <if test="status != null and status != ''">
            and order_status = #{status}
          </if>
          <if test="keyword != null and keyword != ''">
            and (
              order_no like concat('%', #{keyword}, '%')
              or exists (
                select 1
                from member_user mu
                where mu.id = member_order.member_id
                  and (
                    mu.member_no like concat('%', #{keyword}, '%')
                    or mu.username like concat('%', #{keyword}, '%')
                    or mu.mobile like concat('%', #{keyword}, '%')
                  )
              )
            )
          </if>
        order by id desc
        </script>
        """)
    List<MemberOrder> selectAdminList(@Param("keyword") String keyword, @Param("status") String status);

    @Select("""
        select id, member_id, shipment_id, order_no, order_status, payment_status, amount, currency_code, remark, created_at, updated_at
        from member_order
        where shipment_id = #{shipmentId}
        order by id desc
        limit 1
        """)
    MemberOrder selectByShipmentId(Long shipmentId);
}
