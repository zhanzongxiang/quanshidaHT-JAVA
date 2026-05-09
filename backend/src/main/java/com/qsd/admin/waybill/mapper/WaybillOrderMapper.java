package com.qsd.admin.waybill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.waybill.entity.WaybillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WaybillOrderMapper extends BaseMapper<WaybillOrder> {

    @Select("""
        <script>
        select id, main_tracking_no, reference_no, customer_name, customer_phone, origin_warehouse,
               destination_country, destination_city, member_id, route_type, current_status, current_node,
               cargo_description, package_count, weight_kg, remark, deleted, created_at, updated_at
        from waybill_order
        where deleted = 0
          <if test="keyword != null and keyword != ''">
            and (
                main_tracking_no like concat('%', #{keyword}, '%')
                or customer_name like concat('%', #{keyword}, '%')
                or coalesce(reference_no, '') like concat('%', #{keyword}, '%')
            )
          </if>
          <if test="status != null and status != ''">
            and current_status = #{status}
          </if>
        order by updated_at desc, id desc
        </script>
        """)
    List<WaybillOrder> selectActiveList(@Param("keyword") String keyword, @Param("status") String status);

    @Select("""
        select id, main_tracking_no, reference_no, customer_name, customer_phone, origin_warehouse,
               destination_country, destination_city, member_id, route_type, current_status, current_node,
               cargo_description, package_count, weight_kg, remark, deleted, created_at, updated_at
        from waybill_order
        where id = #{id}
          and deleted = 0
        limit 1
        """)
    WaybillOrder selectActiveById(Long id);

    @Select("""
        select id, main_tracking_no, reference_no, customer_name, customer_phone, origin_warehouse,
               destination_country, destination_city, member_id, route_type, current_status, current_node,
               cargo_description, package_count, weight_kg, remark, deleted, created_at, updated_at
        from waybill_order
        where main_tracking_no = #{mainTrackingNo}
          and deleted = 0
        limit 1
        """)
    WaybillOrder selectActiveByMainTrackingNo(String mainTrackingNo);

    @Select("""
        <script>
        select id, main_tracking_no, reference_no, customer_name, customer_phone, origin_warehouse,
               destination_country, destination_city, member_id, route_type, current_status, current_node,
               cargo_description, package_count, weight_kg, remark, deleted, created_at, updated_at
        from waybill_order
        where deleted = 0
          and id in
          <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
          </foreach>
        </script>
        """)
    List<WaybillOrder> selectActiveByIds(@Param("ids") List<Long> ids);

    @Select("""
        <script>
        select distinct wo.id, wo.main_tracking_no, wo.reference_no, wo.customer_name, wo.customer_phone, wo.origin_warehouse,
               wo.destination_country, wo.destination_city, wo.member_id, wo.route_type, wo.current_status, wo.current_node,
               wo.cargo_description, wo.package_count, wo.weight_kg, wo.remark, wo.deleted, wo.created_at, wo.updated_at
        from waybill_order wo
        left join member_waybill_relation mwr on mwr.waybill_id = wo.id and mwr.member_id = #{memberId}
        where wo.deleted = 0
          and (
            mwr.id is not null
            <if test="phone != null and phone != ''">
              or wo.customer_phone = #{phone}
              or wo.member_id = #{memberId}
            </if>
          )
        order by wo.updated_at desc, wo.id desc
        </script>
        """)
    List<WaybillOrder> selectAccessibleByMember(@Param("memberId") Long memberId, @Param("phone") String phone);

    @Select("""
        <script>
        select count(distinct wo.id)
        from waybill_order wo
        left join member_waybill_relation mwr on mwr.waybill_id = wo.id and mwr.member_id = #{memberId}
        where wo.deleted = 0
          and (
            mwr.id is not null
            <if test="phone != null and phone != ''">
              or wo.customer_phone = #{phone}
              or wo.member_id = #{memberId}
            </if>
          )
        </script>
        """)
    long countAccessibleByMember(@Param("memberId") Long memberId, @Param("phone") String phone);

    @Select("""
        <script>
        select distinct wo.id, wo.main_tracking_no, wo.reference_no, wo.customer_name, wo.customer_phone, wo.origin_warehouse,
               wo.destination_country, wo.destination_city, wo.member_id, wo.route_type, wo.current_status, wo.current_node,
               wo.cargo_description, wo.package_count, wo.weight_kg, wo.remark, wo.deleted, wo.created_at, wo.updated_at
        from waybill_order wo
        left join member_waybill_relation mwr on mwr.waybill_id = wo.id and mwr.member_id = #{memberId}
        where wo.id = #{id}
          and wo.deleted = 0
          and (
            mwr.id is not null
            <if test="phone != null and phone != ''">
              or wo.customer_phone = #{phone}
              or wo.member_id = #{memberId}
            </if>
          )
        limit 1
        </script>
        """)
    WaybillOrder selectAccessibleDetailByMember(
        @Param("id") Long id,
        @Param("memberId") Long memberId,
        @Param("phone") String phone
    );

    @Select("""
        select count(1)
        from waybill_order
        where deleted = 0
        """)
    long countActive();

    @Select("""
        select count(1)
        from waybill_order
        where deleted = 0
          and current_status = #{status}
        """)
    long countByCurrentStatus(@Param("status") String status);
}
