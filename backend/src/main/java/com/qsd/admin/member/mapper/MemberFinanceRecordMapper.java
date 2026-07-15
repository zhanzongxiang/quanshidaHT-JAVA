package com.qsd.admin.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.member.entity.MemberFinanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MemberFinanceRecordMapper extends BaseMapper<MemberFinanceRecord> {
    @Select("""
        select id, member_id, order_id, record_no, record_type, amount, currency_code, record_status, note, created_at
        from member_finance_record
        where member_id = #{memberId}
        order by id desc
        """)
    List<MemberFinanceRecord> selectByMemberId(Long memberId);

    @Select("""
        select id, member_id, order_id, record_no, record_type, amount, currency_code, record_status, note, created_at
        from member_finance_record
        where order_id = #{orderId}
        order by id desc
        limit 1
        """)
    MemberFinanceRecord selectLatestByOrderId(Long orderId);
}
