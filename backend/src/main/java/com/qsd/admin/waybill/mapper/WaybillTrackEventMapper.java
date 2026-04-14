package com.qsd.admin.waybill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.waybill.entity.WaybillTrackEvent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WaybillTrackEventMapper extends BaseMapper<WaybillTrackEvent> {

    @Select("""
        select id, waybill_id, leg_id, event_time, event_status, event_description,
               event_location, visible_to_customer, sort_no, created_at
        from waybill_track_event
        where waybill_id = #{waybillId}
        order by event_time asc, sort_no asc, id asc
        """)
    List<WaybillTrackEvent> selectByWaybillId(Long waybillId);

    @Select("""
        select id, waybill_id, leg_id, event_time, event_status, event_description,
               event_location, visible_to_customer, sort_no, created_at
        from waybill_track_event
        where waybill_id = #{waybillId}
          and visible_to_customer = 1
        order by event_time asc, sort_no asc, id asc
        """)
    List<WaybillTrackEvent> selectVisibleByWaybillId(Long waybillId);

    @Delete("""
        delete from waybill_track_event
        where waybill_id = #{waybillId}
        """)
    void deleteByWaybillId(Long waybillId);
}
