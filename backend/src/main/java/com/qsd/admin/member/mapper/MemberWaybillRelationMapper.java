package com.qsd.admin.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.member.entity.MemberWaybillRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MemberWaybillRelationMapper extends BaseMapper<MemberWaybillRelation> {

    @Delete("delete from member_waybill_relation where member_id = #{memberId}")
    void deleteByMemberId(Long memberId);

    @Select("select waybill_id from member_waybill_relation where member_id = #{memberId} order by waybill_id asc")
    List<Long> selectWaybillIdsByMemberId(Long memberId);
}
