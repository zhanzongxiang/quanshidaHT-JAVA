package com.qsd.admin.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.member.entity.MemberAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MemberAddressMapper extends BaseMapper<MemberAddress> {

    @Select("""
        select id, member_id, contact_name, contact_phone, country, province, city, district, detail_address, postal_code, is_default, created_at, updated_at
        from member_address
        where member_id = #{memberId}
        order by is_default desc, id desc
        """)
    List<MemberAddress> selectByMemberId(Long memberId);

    @Update("update member_address set is_default = 0 where member_id = #{memberId}")
    void clearDefaultByMemberId(Long memberId);
}
