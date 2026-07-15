package com.qsd.admin.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.member.entity.MemberShipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MemberShipmentMapper extends BaseMapper<MemberShipment> {
    @Select("""
        select id, member_id, shipment_no, address_id, waybill_id, shipment_status, package_count, total_weight,
               recipient_name, recipient_phone, destination_country, destination_province, destination_city,
               destination_district, destination_address, postal_code, remark, created_at, updated_at
        from member_shipment
        where member_id = #{memberId}
        order by id desc
        """)
    List<MemberShipment> selectByMemberId(Long memberId);

    @Select("""
        <script>
        select id, member_id, shipment_no, address_id, waybill_id, shipment_status, package_count, total_weight,
               recipient_name, recipient_phone, destination_country, destination_province, destination_city,
               destination_district, destination_address, postal_code, remark, created_at, updated_at
        from member_shipment
        where 1 = 1
          <if test="status != null and status != ''">
            and shipment_status = #{status}
          </if>
          <if test="keyword != null and keyword != ''">
            and (
              shipment_no like concat('%', #{keyword}, '%')
              or exists (
                select 1
                from member_user mu
                where mu.id = member_shipment.member_id
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
    List<MemberShipment> selectAdminList(@Param("keyword") String keyword, @Param("status") String status);
}
