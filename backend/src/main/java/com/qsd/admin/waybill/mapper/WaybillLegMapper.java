package com.qsd.admin.waybill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.waybill.entity.WaybillLeg;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WaybillLegMapper extends BaseMapper<WaybillLeg> {

    @Select("""
        select id, waybill_id, leg_no, leg_type, carrier_name, tracking_no, from_node, to_node,
               leg_status, transfer_flag, departure_time, arrival_time, remark, created_at, updated_at
        from waybill_leg
        where waybill_id = #{waybillId}
        order by leg_no asc, id asc
        """)
    List<WaybillLeg> selectByWaybillId(Long waybillId);

    @Select("""
        select id, waybill_id, leg_no, leg_type, carrier_name, tracking_no, from_node, to_node,
               leg_status, transfer_flag, departure_time, arrival_time, remark, created_at, updated_at
        from waybill_leg
        where tracking_no = #{trackingNo}
        limit 1
        """)
    WaybillLeg selectByTrackingNo(String trackingNo);

    @Delete("""
        delete from waybill_leg
        where waybill_id = #{waybillId}
        """)
    void deleteByWaybillId(Long waybillId);
}
