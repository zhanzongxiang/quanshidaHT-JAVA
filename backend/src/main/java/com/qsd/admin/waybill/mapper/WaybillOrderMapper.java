package com.qsd.admin.waybill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.waybill.entity.WaybillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WaybillOrderMapper extends BaseMapper<WaybillOrder> {

    @Select("""
        <script>
        select id, tenant_id, main_tracking_no, reference_no, customer_name, customer_phone, origin_warehouse,
               destination_country, destination_city, member_id, route_type, current_status, current_node,
               cargo_description, package_count, weight_kg, remark, deleted, created_at, updated_at
        from waybill_order
        where tenant_id = #{tenantId}
          and deleted = 0
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
        limit 500
        </script>
        """)
    List<WaybillOrder> selectActiveList(@Param("tenantId") Long tenantId, @Param("keyword") String keyword, @Param("status") String status);

    @Select("""
        select id, tenant_id, main_tracking_no, reference_no, customer_name, customer_phone, origin_warehouse,
               destination_country, destination_city, member_id, route_type, current_status, current_node,
               cargo_description, package_count, weight_kg, remark, deleted, created_at, updated_at
        from waybill_order
        where tenant_id = #{tenantId}
          and id = #{id}
          and deleted = 0
        limit 1
        """)
    WaybillOrder selectActiveById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
        select id, tenant_id, main_tracking_no, reference_no, customer_name, customer_phone, origin_warehouse,
               destination_country, destination_city, member_id, route_type, current_status, current_node,
               cargo_description, package_count, weight_kg, remark, deleted, created_at, updated_at
        from waybill_order
        where tenant_id = #{tenantId}
          and main_tracking_no = #{mainTrackingNo}
          and deleted = 0
        limit 1
        """)
    WaybillOrder selectActiveByMainTrackingNo(@Param("tenantId") Long tenantId, @Param("mainTrackingNo") String mainTrackingNo);

    @Select("""
        select id, tenant_id, main_tracking_no, deleted
        from waybill_order
        where tenant_id = #{tenantId}
          and main_tracking_no = #{mainTrackingNo}
        limit 1
        """)
    WaybillOrder selectByMainTrackingNoIncludingDeleted(@Param("tenantId") Long tenantId, @Param("mainTrackingNo") String mainTrackingNo);

    @Select("""
        <script>
        select id, tenant_id, main_tracking_no, reference_no, customer_name, customer_phone, origin_warehouse,
               destination_country, destination_city, member_id, route_type, current_status, current_node,
               cargo_description, package_count, weight_kg, remark, deleted, created_at, updated_at
        from waybill_order
        where tenant_id = #{tenantId}
          and deleted = 0
          and id in
          <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
          </foreach>
        </script>
        """)
    List<WaybillOrder> selectActiveByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

    @Select("""
        <script>
        select distinct wo.id, wo.tenant_id, wo.main_tracking_no, wo.reference_no, wo.customer_name, wo.customer_phone, wo.origin_warehouse,
               wo.destination_country, wo.destination_city, wo.member_id, wo.route_type, wo.current_status, wo.current_node,
               wo.cargo_description, wo.package_count, wo.weight_kg, wo.remark, wo.deleted, wo.created_at, wo.updated_at
        from waybill_order wo
        left join member_waybill_relation mwr
               on mwr.tenant_id = #{tenantId}
              and mwr.waybill_id = wo.id
              and mwr.member_id = #{memberId}
        where wo.tenant_id = #{tenantId}
          and wo.deleted = 0
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
    List<WaybillOrder> selectAccessibleByMember(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId, @Param("phone") String phone);

    @Select("""
        <script>
        select distinct wo.id, wo.tenant_id, wo.main_tracking_no, wo.reference_no, wo.customer_name, wo.customer_phone, wo.origin_warehouse,
               wo.destination_country, wo.destination_city, wo.member_id, wo.route_type, wo.current_status, wo.current_node,
               wo.cargo_description, wo.package_count, wo.weight_kg, wo.remark, wo.deleted, wo.created_at, wo.updated_at
        from waybill_order wo
        left join member_waybill_relation mwr
               on mwr.tenant_id = #{tenantId}
              and mwr.waybill_id = wo.id
              and mwr.member_id = #{memberId}
        where wo.tenant_id = #{tenantId}
          and wo.id = #{id}
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
        @Param("tenantId") Long tenantId,
        @Param("id") Long id,
        @Param("memberId") Long memberId,
        @Param("phone") String phone
    );

    @Select("""
        select count(1)
        from waybill_order
        where tenant_id = #{tenantId}
          and deleted = 0
        """)
    long countActive(@Param("tenantId") Long tenantId);

    @Select("""
        select count(1)
        from waybill_order
        where tenant_id = #{tenantId}
          and deleted = 0
          and current_status = #{status}
        """)
    long countByCurrentStatus(@Param("tenantId") Long tenantId, @Param("status") String status);

    @Select("""
        <script>
        select r.member_id as memberId, count(distinct w.id) as cnt
        from member_waybill_relation r
        join waybill_order w on w.id = r.waybill_id and w.deleted = 0 and w.tenant_id = #{tenantId}
        where r.tenant_id = #{tenantId}
          and r.member_id in
        <foreach collection="memberIds" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        group by r.member_id
        </script>
        """)
    List<Map<String, Object>> countAccessibleByMemberIds(@Param("tenantId") Long tenantId, @Param("memberIds") List<Long> memberIds);
}
