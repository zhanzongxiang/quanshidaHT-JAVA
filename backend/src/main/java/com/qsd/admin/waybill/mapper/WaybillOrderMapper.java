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
               destination_country, destination_city, route_type, current_status, current_node,
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
               destination_country, destination_city, route_type, current_status, current_node,
               cargo_description, package_count, weight_kg, remark, deleted, created_at, updated_at
        from waybill_order
        where id = #{id}
          and deleted = 0
        limit 1
        """)
    WaybillOrder selectActiveById(Long id);

    @Select("""
        select id, main_tracking_no, reference_no, customer_name, customer_phone, origin_warehouse,
               destination_country, destination_city, route_type, current_status, current_node,
               cargo_description, package_count, weight_kg, remark, deleted, created_at, updated_at
        from waybill_order
        where main_tracking_no = #{mainTrackingNo}
          and deleted = 0
        limit 1
        """)
    WaybillOrder selectActiveByMainTrackingNo(String mainTrackingNo);
}
