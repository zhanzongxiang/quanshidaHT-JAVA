package com.qsd.admin.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.member.entity.MemberShipmentPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MemberShipmentPackageMapper extends BaseMapper<MemberShipmentPackage> {
    @Select("select package_id from member_shipment_package where shipment_id = #{shipmentId} order by id asc")
    List<Long> selectPackageIdsByShipmentId(Long shipmentId);
}
